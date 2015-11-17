package com.example.p2pfirstdemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import android.R.array;
import android.R.integer;


public class IOperation {
	// final static File files=null;
	private final static String ROOTPATH_STRING = "/";

	// private final static String PARENTPATH_STRING="..";

	// fill two ArrayList(paths,names) in terms of path
	public static synchronized void getDir(ArrayList<String> names, ArrayList<String> paths,
			String path) {
		File file = new File(path);
		File[] files = file.listFiles();
		// 这是一个bug点，对于每一次对sd卡的读取无干扰，必须保证传入的Arraylist空
		names.clear();
		paths.clear();
		// is current directory is not root
		if (!ROOTPATH_STRING.equals(path)) {
			names.add("..");
			paths.add(file.getParent());
		}

		for (File f : files) {
			names.add(f.getName());
			paths.add(f.getPath());
		}
	}
	
	public static synchronized HashMap<String, Mission> getSuspendedMissions(){
		ArrayList<String> names=new ArrayList<String>();
		ArrayList<String> paths=new ArrayList<String>();
		HashMap<String, Mission> missions=new HashMap<String, Mission>();
		
		IOperation.getDir(names, paths, "/mnt/sdcard/p2pTransfer");
		for(int i=0;i<names.size();i++){
			String[] segs=names.get(i).split("\\.");
			if(segs!=null&&segs.length>=3&&"rcd".equals(segs[segs.length-1])){
				byte[] buf=IOperation.readBytes(new File(paths.get(i)));
				String info=new String(buf);
				String[] infos=info.split(",");
				
				String fileinfo=infos[0]+","+infos[1]+","+"/mnt/sdcard/p2pTransfer/"+names.get(i)+
						","+infos[2]+","+infos[3];
				int[] send=new int[Integer.parseInt(infos[2])];
				int[] rec=new int[Integer.parseInt(infos[2])];			
				for(int j=0;j<infos[4].length();j++){
					int tmp=Integer.parseInt(infos[4].charAt(j)+"");
					send[j]=tmp;
					rec[j]=tmp;
				}
				Mission mission=new Mission(new fileInfo(fileinfo));
				mission.ifpause=true;
				mission.finished=false;
				mission.segsREC=rec;
				mission.segsSND=send;
				
				missions.put(mission.fileinfo.sha1, mission);
			}
		}
		return missions;
	}

	public static synchronized void writeBytes(byte[] buf,int len, File f){
		File file=f;
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream outputStream=new FileOutputStream(file);
			outputStream.write(buf, 0, len);
			outputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static synchronized byte[] readBytes(File f){
		File file=f;
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			FileInputStream in=new FileInputStream(file);
			byte[] buf=new byte[(int)file.length()];
			in.read(buf);
			in.close();
			return buf;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static synchronized void writeString(String buf, File f) {
		File file = f;
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			// append to the end of the file
			// FileOutputStream outputStream=new FileOutputStream(file);
//			append to the end of file
			FileWriter fileWriter = new FileWriter(file, true);
			// outputStream.write(buf);
			fileWriter.write(buf);
			fileWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static synchronized void setFileSize(String filePath, long size){
		File file=new File(filePath);
		try {
			RandomAccessFile randomAccessFile=new RandomAccessFile(file, "rw");
			randomAccessFile.setLength(size);
			randomAccessFile.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void writeFileSeq(File file, int begin, byte[] buf){
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			RandomAccessFile randomAccessFile=new RandomAccessFile(file, "rw");
			randomAccessFile.seek(begin);
			randomAccessFile.write(buf);
			randomAccessFile.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static synchronized byte[] getFileSeq(File file, int begin) {
		byte[] segs=new byte[1024*16];
		int byteread=0;
		try {
			RandomAccessFile randomAccessFile=new RandomAccessFile(file, "r");
			randomAccessFile.seek(begin);
			byteread=randomAccessFile.read(segs);
			randomAccessFile.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(byteread!=1024*16){
			byte[] read=new byte[byteread];
			for(int i=0;i<byteread;i++)
				read[i]=segs[i];
			return read;
		}
		
		return segs;
	}
	

	public static synchronized void makeFileInfo(File f, String desPath, ArrayList<String> infoArrayList) {
		int byteRead = 0;
		int seqCount = 0;
		//ArrayList<String> infoArrayList=new ArrayList<String>();
		String tmp = null;
		File file = f;
		File infoFile = new File(desPath);
		
//		create infoFile
		if (infoFile.exists())
			return ;
		try {
			infoFile.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		byte[] buf = new byte[1024 * 16];
		// ArrayList<byte[]> bufs=new ArrayList<byte[]>();
		FileInputStream inputStream = null;

		if (!file.exists()) {
			return ;
		}
		infoArrayList.add(SHA1.getFileSha1(file.getPath()));
		IOperation.writeString("sha1:"+infoArrayList.get(0), infoFile);
		try {
			inputStream = new FileInputStream(file);
			while ((byteRead = inputStream.read(buf)) != -1) {
				// bufs.add(buf);
				seqCount++;
//				IOperation.writeFile("seq"+seqCount+":"+SHA1.getFileseqSha1(buf), infoFile);
			}
			inputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		infoArrayList.add(file.getName());
		infoArrayList.add(file.getAbsolutePath());
		infoArrayList.add(""+seqCount);
		infoArrayList.add(""+file.length());		
	}
	
	public static synchronized void delFile(String path){
		File file=new File(path);
		if (!file.exists())
			return ;
		else
		{
		try {
			file.delete();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
	}
}
