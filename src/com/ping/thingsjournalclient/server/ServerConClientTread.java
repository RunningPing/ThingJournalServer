package com.ping.thingsjournalclient.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Set;

import com.ping.thingsjournalclient.dao.UserDao;
import com.ping.thingsjournalclient.model.MessageType;
import com.ping.thingsjournalclient.model.TransMessage;
import com.ping.thingsjournalclient.model.User;
import com.ping.thingsjournalclient.util.SMUtils;

public class ServerConClientTread extends Thread{

	private Socket socket = null;
	private User user =null;
	private Connection con = null;
	private PrintStream ps = null;
	

	public ServerConClientTread(Socket socket, User user, Connection con, PrintStream ps){
		this.socket = socket;
		this.user = user;
		this.con = con;
		this.ps = ps;
	}
	
	
	public Socket getSocket() {
		return socket;
	}


	public void setSocket(Socket socket) {
		this.socket = socket;
	}


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	@Override
	public void run() {
		int tempCount = 0;
		
		while(true){
			try{
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				TransMessage  tm = (TransMessage) ois.readObject();
				String type = tm.getMessageType();
				if(type.equals(MessageType.GET_FRIENDS)){//处理获得好友列表的请求
					String friendsName = user.getFriendsName();
					String k_QU = user.getKey_QU();
					String friendsNameEncrypt = SMUtils.encryptBySm4(friendsName, k_QU);
					TransMessage tmSend = new TransMessage();
					tmSend.setMessageType(MessageType.RET_FRIENDS);
					tmSend.setMessageContent(friendsNameEncrypt);
					tmSend.setSender(user.getUserName());
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(tmSend);
					System.out.println("返回好友列表");
					ps.println("返回好友列表");
				}else if(type.equals(MessageType.SEND_QUERY)){//处理查询好友位置的请求
					dealSendQueary(tm);
				}else if(type.equals(MessageType.SEND_RESULT)){//处理好友返回的查询结果
					transpondMessage(tm);
				}else if(type.equals(MessageType.ADDFRIEND)){//处理添加的好友请求
					addFriend(tm.getReceiver());
				}else if(type.equals(MessageType.LOCATIONFAILED)){//处理好友定位失败的返回请求
					TransMessage tmSend = new TransMessage();
					tmSend.setMessageType(MessageType.LOCATIONFAILED);
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(tmSend);
				}else if(type.equals(MessageType.ON)){//检测好友是否在线
					tempCount++;
					if(tempCount%3 == 0){
						System.out.println(user.getUserName()+" is On");
						ps.println(user.getUserName()+" is On");
					}
				}
			}catch(NullPointerException e){
				System.out.println(user.getUserName());
				ps.println(user.getUserName());
			}
			catch(Exception e){//好友离线，从map中移除该好友，关闭他的socket
				ManageStatic.move(user.getUserName());
				
				try {
					if(socket != null){
							socket.close();
					}
					
				} catch (IOException e1) {
					System.out.println("socket closed failed");
					ps.println("socket closed failed");
				}
				System.out.println("socket closed2");
				ps.println("socket closed2");
				e.printStackTrace(ps);
				e.printStackTrace();
				System.out.println(user.getUserName()+" is disconnected"+" The count of online user is "+ManageStatic.manageMap.size());
				ps.println(user.getUserName()+" is disconnected"+" The count of online user is "+ManageStatic.manageMap.size());
				Set<String> tempSet = ManageStatic.manageMap.keySet();
				System.out.println("当前在线用户有： ");
				ps.println("当前在线用户有： ");
				for(String tempString: tempSet){
					System.out.println(tempString);
					ps.println(tempString);
				}
				break;
			}
		}
		
	}
	/**
	 * 处理添加好友的请求
	 * @param receiver
	 */
	
	private void addFriend(String receiver) {
		TransMessage tm = new TransMessage();
		try {
			if(UserDao.userNameExisted(con, receiver)){
				String result = UserDao.addFriends(con, user, receiver);
				if(result != null){
					tm.setMessageType(MessageType.ADDFRIEND_SUCCESS);
					tm.setMessageContent(result);
				}else{
					tm.setMessageType(MessageType.ADDFRIEND_FAIL);
				}
			}else{
				tm.setMessageType(MessageType.ADDFRIEND_FAIL);
			}
			ObjectOutputStream oos = new ObjectOutputStream(getSocket().getOutputStream());
			oos.writeObject(tm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 转发处理请求
	 * @param tm
	 */
	private void transpondMessage(TransMessage tm) {
		String sender = tm.getSender();
		String receiver = tm.getReceiver();
		String queryType = tm.getQueryType();
		String resultMessage = tm.getMessageContent();
		String[] resultMessages = resultMessage.trim().split(MessageType.symbol2);
		ServerConClientTread tempscct = null;
		TransMessage tmTranspond = new TransMessage();
		tmTranspond.setMessageType(MessageType.BACK_RESULT);
		tmTranspond.setReceiver(receiver);
		tmTranspond.setSender(sender);
		if((tempscct = ManageStatic.get(receiver)) != null){
			if(queryType.equals(MessageType.QUERY_P)){
				String H_Sender = resultMessages[3];
				String H_SenderDe = SMUtils.decryptBySm4(H_Sender, user.getKey_QU());
				String H_Receiver = SMUtils.encryptBySm4(H_SenderDe, tempscct.getUser().getKey_QU());
				StringBuilder sb = new StringBuilder();			
				for(int i = 0;i<3;i++){
					sb.append(resultMessages[i]+MessageType.symbol2);
				}			
				String temp = sb.toString();
				String result = temp.substring(0, temp.lastIndexOf(MessageType.symbol2))+MessageType.symbol2+H_Receiver;
				tmTranspond.setMessageContent(result);
				tmTranspond.setQueryType(queryType);
			}else if(queryType.equals(MessageType.QUERY_C)){
				String H_Sender = resultMessages[4];
				String H_SenderDe = SMUtils.decryptBySm4(H_Sender, user.getKey_QU());
				String H_Receiver = SMUtils.encryptBySm4(H_SenderDe, tempscct.getUser().getKey_QU());
				StringBuilder sb = new StringBuilder();			
				for(int i = 0;i<4;i++){
					sb.append(resultMessages[i]+MessageType.symbol2);
				}			
				String temp = sb.toString();
				String result = temp.substring(0, temp.lastIndexOf(MessageType.symbol2))+MessageType.symbol2+H_Receiver;
				tmTranspond.setMessageContent(result);
				tmTranspond.setQueryType(queryType);
			}
			tmTranspond.setArea(tm.getArea());
			try {
				new ObjectOutputStream(tempscct.getSocket().getOutputStream()).writeObject(tmTranspond);
				System.out.println("向 "+receiver+" 转发 "+sender+" 查询结果");
				System.out.println(tmTranspond.getMessageContent());
				ps.println("向 "+receiver+" 转发 "+sender+" 查询结果");
				ps.println(tmTranspond.getMessageContent());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}


	/**
	 * 处理查询好友位置的请求
	 * @param tm
	 */
	
	private void dealSendQueary(TransMessage tm) {
		String[] friendsName = user.getFriendsName().trim().split(User.separator);
		ArrayList<ServerConClientTread> friendsList = ManageStatic.getOnlineFriendsList(friendsName);
		try {
			TransMessage tmSender = new TransMessage();
			tmSender.setSender(user.getUserName());
			tmSender.setMessageType(MessageType.BACK_QUERY);
			tmSender.setReceiverCount(friendsList.size());
			new ObjectOutputStream(getSocket().getOutputStream()).writeObject(tmSender);
			System.out.println("返回查询请求 "+user.getUserName());
			ps.println("返回查询请求 "+user.getUserName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		String sendMessage = tm.getMessageContent();
		String key_Sender = user.getKey_QU();
		String queryType = tm.getQueryType();
		String key_Receiver = null;
		for(ServerConClientTread tempscct : friendsList){
			key_Receiver = tempscct.getUser().getKey_QU();
			TransMessage tmReceiver = new TransMessage();
			tmReceiver.setMessageType(MessageType.DEAL_MESSAGE);
			tmReceiver.setSender(user.getUserName());
			tmReceiver.setReceiver(tempscct.getUser().getUserName());
			tmReceiver.setQueryType(queryType);
			tmReceiver.setMessageContent(transformMessage(sendMessage, key_Sender, key_Receiver));
			ObjectOutputStream oostemp;
			try {
				oostemp = new ObjectOutputStream(tempscct.getSocket().getOutputStream());
				oostemp.writeObject(tmReceiver);
				System.out.println("向 "+tempscct.getUser().getUserName()+"发送查询请求");
				ps.println("向 "+tempscct.getUser().getUserName()+"发送查询请求");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
	}
	
	/**
	 * 将好友返回的查询数据转换成QU端可识别的数据
	 * @param sendMessage
	 * @param key_Sender
	 * @param key_Receiver
	 * @return
	 */
	public String transformMessage(String sendMessage , String key_Sender, String key_Receiver){
		String[] messages = sendMessage.trim().split(MessageType.symbol2);
		String result = null;
		String H_Sender = messages[5];
		String H_SenderDe = SMUtils.decryptBySm4(H_Sender, key_Sender);
		String H_Receiver = SMUtils.encryptBySm4(H_SenderDe, key_Receiver);
		StringBuilder sb = new StringBuilder();			
		for(int i = 0;i<5;i++){
			sb.append(messages[i]+MessageType.symbol2);
		}			
		String temp = sb.toString();
		result = temp.substring(0, temp.lastIndexOf(MessageType.symbol2))+MessageType.symbol2+H_Receiver;
		return result;
	}
	
	
}
