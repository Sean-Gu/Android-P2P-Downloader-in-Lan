package com.example.p2pfirstdemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

import android.R.bool;

public class SHA1 {
	public static String getFileSha1(String path) {  
		  File file=new File(path);  
		  FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			MessageDigest messagedigest;  
	 
		    messagedigest = MessageDigest.getInstance("SHA-1"); 
		    byte[] buffer = new byte[1024 * 1024 * 10];  
		    int len = 0;  
		      
		    while ((len = in.read(buffer)) >0) {  
		   //�ö���ͨ��ʹ�� update����������������  
		     messagedigest.update(buffer, 0, len);  
		    }  
		     
		  //���ڸ��������ĸ������ݣ�digest ����ֻ�ܱ�����һ�Ρ��ڵ��� digest ֮��MessageDigest �����������ó����ʼ״̬��  
		    return byte2hex(messagedigest.digest());  
		}   catch (Exception e) {  
		        e.printStackTrace();  
		    }  
		finally{  
		     try {
		    	 if(in!=null){
		    		 in.close();
		    	 }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}  
		    return null;  
		 }  

//	public static String getFileseqSha1(byte[] buf) {   
//		  	try {
//			MessageDigest messagedigest;  
//		    messagedigest = MessageDigest.getInstance("SHA-1"); 
//		    byte[] buffer = buf;  
//		   	     
//		  //���ڸ��������ĸ������ݣ�digest ����ֻ�ܱ�����һ�Ρ��ڵ��� digest ֮��MessageDigest �����������ó����ʼ״̬��  
//		    return byte2hex(messagedigest.digest(buffer));  
//		  	}catch (Exception e) {  
//		        e.printStackTrace();  
//			}
//		  	return null;
//		 }  

	public static String byte2hex(byte[] b){
		String hsString="";
		String stmpString="";
		for(int n=0;n<b.length;n++){
			stmpString=(Integer.toHexString(b[n]&0xFF));
			if(stmpString.length()==1) hsString+="0"+stmpString;
			else hsString+=stmpString;
		}
		return hsString.toUpperCase();
	}
	
	public static boolean cmpSHA1(String src, String des)
	{
		if(src==null||des==null) return false;
		else return src.equals(des);
	}
}

