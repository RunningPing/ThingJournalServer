package com.ping.thingsjournalclient.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;

import com.ping.thingsjournalclient.dao.UserDao;
import com.ping.thingsjournalclient.model.MessageType;
import com.ping.thingsjournalclient.model.TransMessage;
import com.ping.thingsjournalclient.model.User;
import com.ping.thingsjournalclient.util.DateUtil;
import com.ping.thingsjournalclient.util.DbUtil;
import com.ping.thingsjournalclient.util.SMUtils;

public class Server {

	private static int SERVER_PORT = 30001;
	private DbUtil dbUtil = new DbUtil();
	private Connection con = null;
	private File fileLog = null;
	public Server(){
		ServerSocket ss = null;
		
		//日志文件
		fileLog = new File("C:\\"+DateUtil.getCurrentDate());
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream(fileLog));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		
		//连接数据库
		try {
			con = dbUtil.getCon();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		//连接客户端
		try {
			ss = new ServerSocket(SERVER_PORT);
			System.out.println("Server is running");
			ps.println("Server is running");
			while(true){
				Socket socket;
				try {
					socket = ss.accept();
					socket.setSoTimeout(1000*100);
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					User user = (User) ois.readObject();
					TransMessage tm = new TransMessage();
					ObjectOutputStream oos =new ObjectOutputStream(socket.getOutputStream());
					if(user.getType().equals(MessageType.LOGIN)){
						User userLogin = UserDao.logUser(con, user);
						if(userLogin != null){//登录成功
							String key_QU = SMUtils.sm4generateKey();
							String key_QU_Encrypt = SMUtils.encryptBySm2(key_QU, user.getPublicKey());
							userLogin.setKey_QU(key_QU);
							tm.setMessageType(MessageType.LOGIN_SUCCESS);
							tm.setMessageContent(key_QU_Encrypt);
							oos.writeObject(tm);
							ServerConClientTread scct = new ServerConClientTread(socket, userLogin, con, ps);
							scct.start();
							ManageStatic.put(userLogin.getUserName(), scct);
							System.out.println(userLogin.getUserName()+"上线了! The count of online user is "+ManageStatic.manageMap.size());
							ps.println(userLogin.getUserName()+"上线了! The count of online user is "+ManageStatic.manageMap.size());
						}else{//登录失败
							tm.setMessageType(MessageType.LOGIN_FAIL);
							oos.writeObject(tm);
						}
					}else if(user.getType().equals(MessageType.REGISTER)){//处理注册消息
						String userName = user.getUserName();
						String password = SMUtils.decryptBySm2(user.getPassword(), ManageStatic.privateKey);
						if(!UserDao.userNameExisted(con, userName)){
							User userRegister = new User();
							userRegister.setUserName(userName);
							userRegister.setPassword(password);
							if(UserDao.addUser(con, userRegister) == 1){
								tm.setMessageType(MessageType.REGISTER_SUCCESS);
								System.out.println(userName+" 注册成功");
								ps.println(userName+" 注册成功");
							}else{
								tm.setMessageType(MessageType.REGISTER_FAIL);
								System.out.println(userName+" 注册失败");
								ps.println(userName+" 注册失败");
							}
						}else{
							tm.setMessageType(MessageType.REGISTER_FAIL);
							System.out.println(userName+" 注册失败");
							ps.println(userName+" 注册失败");
						}
						oos.writeObject(tm);
						oos.close();
						socket.close();
					}
				} catch (IOException e) {
					System.out.println("socket failed");
					ps.println("socket failed");
					e.printStackTrace(ps);
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (Exception e) {
					System.out.println("");
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.out.println("server start failed");
			ps.println("server start failed");
		}
			
		
	}
	
	
	public static void main(String[] args) {
		new Server();
	}
}
