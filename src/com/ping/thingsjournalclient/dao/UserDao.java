package com.ping.thingsjournalclient.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ping.thingsjournalclient.model.User;
import com.ping.thingsjournalclient.util.DbUtil;
import com.ping.thingsjournalclient.util.SMUtils;


public class UserDao {
	/**
	 * 查询用户名是否存在数据库中
	 * @param con
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public static boolean userNameExisted(Connection con, String userName) throws Exception{
		String sql = "select * from t_user where userName"
				+ "=?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, userName);
		ResultSet rs = pstmt.executeQuery();
		if(rs.next()){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 添加用户
	 * @param con
	 * @param user
	 * @return 1success 0failed
	 * @throws Exception
	 */
	public static int addUser(Connection con, User user) throws Exception{
		String sql = " insert into t_user values(null,?,?,?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, user.getUserName());
		pstmt.setString(2, SMUtils.encryptBySm3(user.getPassword()));
		pstmt.setString(3, "△△");
		return pstmt.executeUpdate();
	}
	/**
	 * 登录后重新设置好友，用户名，用户ID
	 * @param con
	 * @param user
	 * @return 登录成功返回UserBean对象，登录失败返回null
	 * @throws Exception
	 */
	public static User logUser(Connection con, User user) throws Exception{
		User resultUser = null;
		String sql = "select * from t_user where userName=? and password=?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, user.getUserName());
		pstmt.setString(2, user.getPassword());
		ResultSet rs = pstmt.executeQuery();
		if(rs.next()){
			resultUser = new User();
			resultUser.setId(rs.getInt("id"));
			resultUser.setUserName(rs.getString("userName"));
			resultUser.setFriendsName(rs.getString("friendsName"));
		}
		return resultUser;
	}
	/**
	 * 添加好友
	 * @param con
	 * @param user
	 * @param friendName
	 * @return 1success 0failed
	 * @throws Exception
	 */
	public static String  addFriends(Connection con, User user,  String friendName) throws Exception{
		String friendsName = user.getFriendsName();
		String[] friends = friendsName.trim().split(User.separator);
		for(int i = 0;i<friends.length;i++){
			if(friends[i].equals(friendName)){
				return friendsName;
			}
		}
		
		String sql0 = "select friendsName from t_user where userName=? ";
		PreparedStatement pstmt0 = con.prepareStatement(sql0);
		pstmt0.setString(1, friendName);
		ResultSet rs = pstmt0.executeQuery();
		String friendssName = null;
		if(rs.next()){
			friendssName = rs.getString("friendsName");
		}
		friendssName = friendssName+User.separator+user.getUserName();
		
		friendsName = friendsName+User.separator+friendName;
		user.setFriendsName(friendsName);
		String sql = "update t_user set friendsName=? where userName =?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, friendsName);
		pstmt.setString(2, user.getUserName());
		
		PreparedStatement pstmtSec = con.prepareStatement(sql);
		pstmtSec.setString(1, friendssName);
		pstmtSec.setString(2, friendName);
		if((pstmt.executeUpdate() == 1) && (pstmtSec.executeUpdate() == 1)){
			return friendsName;
		}
		return null;
	}
	
	
	public static void main(String[] args) throws Exception {
		User user = new User();
		user.setUserName("nihao");
		user.setPassword(SMUtils.encryptBySm3("12345"));
		Connection con = new DbUtil().getCon();
		user = UserDao.logUser(con, user);
		System.out.println(user.getUserName());
		System.out.println(UserDao.addFriends(con, user, "nihaoaa"));
	}
}
