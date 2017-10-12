package com.ping.thingsjournalclient.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

	public static String getValue(String key) throws FileNotFoundException{
		Properties prop=new Properties();
//		InputStream in=new FileInputStream("D:\\javaworkproject\\ThingJournalServer\\src\\server.properties");
		InputStream in=new FileInputStream("C:\\server.properties");
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (String)prop.get(key);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println(PropertiesUtil.getValue("dbUrl"));
	}
}
