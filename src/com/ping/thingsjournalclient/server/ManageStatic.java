package com.ping.thingsjournalclient.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ManageStatic {
	public static Map<String, ServerConClientTread> manageMap = new HashMap<>();//保存当前在线好友的map
	public static String privateKey = "00C41A58D69C958D17DE1625A6FE8AD744485CA052549D7E2E112E055F290B22E5";//服务器私钥
	
	public static void put(String userName, ServerConClientTread scct){
		manageMap.put(userName, scct);
	}
	
	public static ServerConClientTread get(String userName){
		if(manageMap.containsKey(userName)){
			return manageMap.get(userName);
		}else{
			return null;
		}
	}
	
	public static ArrayList<ServerConClientTread> getOnlineFriendsList(String[] friendsName){
		ArrayList<ServerConClientTread> onLineFriends = new ArrayList<>();
		ServerConClientTread scct = null;
		for(String name: friendsName){
			if((scct = get(name)) != null){
				onLineFriends.add(scct);
			}
		}
		return onLineFriends;
	}
	
	public static void move(String userName){
		if(manageMap.containsKey(userName)){
			manageMap.remove(userName);
		}
	}
}
