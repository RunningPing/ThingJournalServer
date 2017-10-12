package com.ping.thingsjournalclient.model;

import java.io.OutputStream;
import java.io.Serializable;

public class User implements Serializable{

	private int id = 0;//用户ID
	private String userName = null;//用户名
	private String password = null;//用户登录口令
	private String friendsName = null;//用户好友名
	public  static String separator = "△△";//用户好友分隔符
	private String key_QU = null;//用户会话密钥
	private String publicKey = null;//用户公钥
	private String privateKey = null;//用户私钥
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	private String type = null;
	private OutputStream os = null;
	
	
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public OutputStream getOs() {
		return os;
	}
	public void setOs(OutputStream os) {
		this.os = os;
	}

	public String getKey_QU() {
		return key_QU;
	}
	public void setKey_QU(String key_QU) {
		this.key_QU = key_QU;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFriendsName() {
		return friendsName;
	}
	public void setFriendsName(String friendsName) {
		this.friendsName = friendsName;
	}
}
