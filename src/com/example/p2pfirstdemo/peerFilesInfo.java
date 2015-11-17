package com.example.p2pfirstdemo;

import java.util.ArrayList;

public class peerFilesInfo{
	public String ip;
	public ArrayList<fileInfo> fileInfos;
	
	public peerFilesInfo(String infos, String ipAdd){
		ip=ipAdd;
		if(infos==null){
			fileInfos=new ArrayList<fileInfo>();
		}
		else{
			fileInfos=new ArrayList<fileInfo>();
			String[] fileinfos=infos.split(";");
			
			for(String tmp:fileinfos){
				fileInfos.add(new fileInfo(tmp));
			} 
		}
		
	}
}