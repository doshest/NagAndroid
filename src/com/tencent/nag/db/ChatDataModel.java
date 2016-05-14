package com.tencent.nag.db;

import java.io.Serializable;

public class ChatDataModel implements Serializable{
	
	public final static String ID = "_room_id";
	
	public final static String DATA = "data";
	
	public final static String NAME = "name";
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String roomId;
	private String roomName;
	
	private String chatMsgEntryJsonString;
	
	
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	public String getChatMsgEntryJsonString() {
		return chatMsgEntryJsonString;
	}
	public void setChatMsgEntryJsonString(String chatMsgEntryJsonString) {
		this.chatMsgEntryJsonString = chatMsgEntryJsonString;
	}
	
	
	
}
