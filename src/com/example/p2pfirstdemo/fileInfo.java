package com.example.p2pfirstdemo;

import java.util.ArrayList;

import android.R.array;

public class fileInfo {
	public String sha1;
	public String name;
	public int segs;
	public String path;
	public String size;	
	public ArrayList<String> peers;//拥有这个资源的节点
	
	public fileInfo(){}
	
	public fileInfo(String info){
		String[] infos=info.split(",");
		sha1=infos[0];
		name=infos[1];
		path=infos[2];
		segs=Integer.parseInt(infos[3]);
		size=infos[4];	
		peers=new ArrayList<String>();
	}
	
	public ArrayList<String> getFileInfo(){
		ArrayList<String> info=new ArrayList<String>();
		info.add(sha1);
		info.add(name);
		info.add(path);
		info.add(String.valueOf(segs));
		info.add(size);
		return info;
	}
	
}
