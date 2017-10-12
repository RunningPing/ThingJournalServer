package com.ping.thingsjournalclient.model;

import java.io.Serializable;

public class TransMessage implements Serializable{
	
	private String messageType = null;//发送消息类型
	private String messageContent = null;//发送消息内容
	private String sender = null;//消息发送者
	private String receiver = null;//消息接收者
	private int receiverCount = 0;//接收查询的人的数量
	private String queryType = null;//查询类型
	private String area = null;//被查询者具体区域
	
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getMessageContent() {
		return messageContent;
	}
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public int getReceiverCount() {
		return receiverCount;
	}
	public void setReceiverCount(int receiverCount) {
		this.receiverCount = receiverCount;
	}
	
	
}
