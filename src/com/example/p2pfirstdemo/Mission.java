package com.example.p2pfirstdemo;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import com.example.p2pfirstdemo.Controller.dateofpeer;
import android.util.Log;

public class Mission {
	public fileInfo fileinfo;
	public boolean finished;
	public boolean ifpause;
	public int[] segsREC;
	public int[] segsSND;
	private String tmpfilepath;
	private String recordpath;
	private File tmpFile;
	private File record;
	private Controller mController;
	private DataOperater mDataOperater;
	
	public Mission(){}
	
	public Mission(fileInfo info){
		ifpause=false;
		finished=false;
		fileinfo=info;
		segsREC=new int[fileinfo.segs];
		segsSND=new int[fileinfo.segs];
		tmpfilepath="/mnt/sdcard/p2pTransfer/"+fileinfo.name+".tmp";
		recordpath="/mnt/sdcard/p2pTransfer/"+fileinfo.name+".rcd";
		tmpFile=new File(tmpfilepath);
		record=new File(recordpath);
		mController=Controller.getInstance();
		mDataOperater=DataOperater.getInstance(null);
		IOperation.setFileSize(tmpfilepath, Integer.parseInt(fileinfo.size));
	}
	
	public synchronized int getnextSEG(){
		int i;
		for(i=0;i<segsSND.length;i++){
			if(segsSND[i]==0){
				segsSND[i]=1;
				return i+1;
			}
		}
		for(i=0;i<segsREC.length;i++){
			if(segsREC[i]==0){
				return i+1;
			}
		}
		return -1;
	}
	//验证文件的正确性
	private boolean testFile(){
		String tmpfilesha1=SHA1.getFileSha1(tmpfilepath);
		if(fileinfo.sha1.equals(tmpfilesha1)) return true;
		else return false;
	}
	//收到问文件分片的回调函数
	public synchronized void onRECseg(byte[] buf, InetAddress ip, int seg){
		if(ifpause||(!mController.missions.containsValue(this))) return;//暂停或者任务已经被删除了，直接返回
		Log.v("download", ip.toString()+" "+fileinfo.name+"/"+seg);
		if (segsREC[seg - 1] ==0) {
			int start = 1024 * 16 * (seg - 1);
			segsREC[seg - 1] = 1;
			IOperation.writeFileSeq(tmpFile, start, buf);
			int nextseg;
			//test need
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			//test need
			if ((nextseg = getnextSEG()) != -1) {
				//progressbar stimulate
//				Message msg=new Message();
//				msg.what=3;
//				Bundle bundle=new Bundle();
//				bundle.putString("sha1", fileinfo.sha1);
//				msg.setData(bundle);
//				MissionsManagerActivity.mHandler.sendMessage(msg);
				
				String cmdString = "query:file:" + fileinfo.sha1 + ":"
						+ nextseg + ":";
				new Thread(new querySeg(ip, cmdString.getBytes())).start();
				
//				Message msg=new Message();
//				msg.what=2;
//				MissionsManagerActivity.mHandler.sendMessage(msg);
			} else {				
				if (testFile()) {
//				if (true) {
					finished = true;
					Log.v("down", "download done");
//					mController.rmMission(this);
					tmpFile.renameTo(new File("/mnt/sdcard/p2pTransfer/"
							+ fileinfo.name));
					IOperation.delFile(recordpath);
					fileinfo.path="/mnt/sdcard/p2pTransfer/"+ fileinfo.name;
					//添加下载的文件到共享列表
					mDataOperater.writeFileInfo(fileinfo.getFileInfo());
					
					//更新界面
//					Message msg=new Message();
//					msg.what=3;
//					Bundle bundle=new Bundle();
//					bundle.putString("name", fileinfo.name);
//					msg.setData(bundle);
//					MissionsManagerActivity.mHandler.sendMessage(msg);
//					msg.what=2;
//					MissionsManagerActivity.mHandler.sendMessage(msg);
				}
				else{
					segsREC=new int[fileinfo.segs];
					segsSND=new int[fileinfo.segs];
					IOperation.delFile(tmpfilepath);
					//重新请求下载
					for(dateofpeer dp: mController.peers){
						if ((nextseg = getnextSEG()) != -1) {
							String cmdString = "query:file:" + fileinfo.sha1 + ":"
									+ nextseg + ":";
							try {
								new Thread(new querySeg(InetAddress.getByName(dp.peerip), cmdString.getBytes())).start();
							} catch (Exception e) {								
								e.printStackTrace();
							}
						}
					}
					
//					Message msg=new Message();
//					msg.what=2;
//					MissionsManagerActivity.mHandler.sendMessage(msg);
				}
				
			}
		}
	}
	//任务暂停
	public synchronized void onPause(){
		ifpause=true;
		String buffer=fileinfo.sha1+","+fileinfo.name+","+fileinfo.segs+","+fileinfo.size+",";
		for(int i:segsREC){
			buffer+=i;
		}
		if(record.exists()) record.delete();
		IOperation.writeBytes(buffer.getBytes(), buffer.length(), record);
	}
	//任务继续
	public synchronized void onResume(){
		ifpause=false;
		for(dateofpeer ip:mController.peers){
			int nextseg=getnextSEG();
			try {
				mController.queryThefile(InetAddress.getByName(ip.peerip), fileinfo.sha1, nextseg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void delMission(){
		IOperation.delFile(tmpfilepath);
		IOperation.delFile(recordpath);
		if(mController.missions.containsValue(this))
			mController.missions.remove(fileinfo.sha1);
	}
	
	class querySeg implements Runnable{
		InetAddress aimipAddress;
		byte[] send;
		
		public querySeg(InetAddress ip, byte[] buf){
			aimipAddress=ip;
			send=buf;
		}
		
		public void run() {
			try {
				DatagramSocket sendDatagramSocket=new DatagramSocket();
				DatagramPacket sendDatagramPacket=new DatagramPacket(send, send.length, aimipAddress, 22222);
				sendDatagramSocket.send(sendDatagramPacket);
				sendDatagramSocket.close();
				} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}
}
