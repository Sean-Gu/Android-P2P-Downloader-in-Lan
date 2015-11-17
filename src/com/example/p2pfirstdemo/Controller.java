package com.example.p2pfirstdemo;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.net.SocketException;
import java.sql.Date;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.p2pfirstdemo.PeerResourceActivity.peerResourceAdapter;

import android.R.integer;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;


public class Controller {
	//节点信息类
	public class dateofpeer{
		public String peerip;
		public Date date;
		
		public dateofpeer(String p, Date d){
			peerip=p;
			date=d;
		}
	}
	//节点间的网络通信命令
	private static String[] instructions=new String[] {
		"ping","pong",
		"query","queryhit"
	};
	
	private static Controller controller;
	
	private NetworkInfo mWiFiNetworkInfo;	
	private Context context;
	
	public HashMap<String, Mission> missions;
	public String[] localFileInfo;
	public static DataOperater mDataOperater;
	public String ipString;
	public String maskString;	
	public ArrayList<dateofpeer> peers;
	public WifiManager mWifiManager;
	public boolean showNoWifi;
	
	private Controller(Context con)
	{
		context=con;
		showNoWifi=true;
		missions=new HashMap<String, Mission>();
		mDataOperater=DataOperater.getInstance(con);		
		peers=new ArrayList<dateofpeer>();
		mWifiManager=(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		localFileInfo=mDataOperater.allfileinfo();
		
		//统计未完成的任务
		missions.putAll(IOperation.getSuspendedMissions());
	}
	
	public static Controller getInstance(Context con){
		if(null==controller){
			controller=new Controller(con);
		}
		return controller;
	}
	
	public static Controller getInstance(){
		return controller;
	}
	
	 //获取本机ip
	 public String getLocalIpAddress() {  		 
		 return Formatter.formatIpAddress(mWifiManager.getDhcpInfo().ipAddress); 
	    }
	 
	//获得子网掩码
	 public String getMask(){
		 return Formatter.formatIpAddress(mWifiManager.getDhcpInfo().netmask);
	 }
	
	//check network state
	public boolean checkNetwork()
	{
		if (context != null) 
		{ 
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context 
			.getSystemService(Context.CONNECTIVITY_SERVICE); 
			mWiFiNetworkInfo = mConnectivityManager 
			.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
			if (mWiFiNetworkInfo != null) 
			{ 
				ipString=getLocalIpAddress();
				maskString=getMask();
				return mWiFiNetworkInfo.isAvailable(); 
			} 
		} 
		return false; 
	}
	
	//用于初次查询对等点以及询问现存对对等待点是否还在线
	public class PeerFindThread implements Runnable{
		private DatagramSocket datagramSocket;
		private DatagramPacket datagramPacket;
		private InetAddress ipInetAddress;
		private byte[] data;
		
		public PeerFindThread(){
			try {
				datagramSocket=new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		@Override
		public void run() {
			Log.v("test", "findpeers");
			while (checkNetwork()) {
				// TODO Auto-generated method stub
				try {
					String IPstr = new String(ipString);
					String MASKstr=new String(maskString);
					String[] ipsegs = IPstr.split("\\.");
					String[] masksegs= MASKstr.split("\\.");
					
					String tmpString =ipsegs[0]+"."+ipsegs[1]+"."+ipsegs[2]+".";
					//Log.v("b", tmpString);
					
					//获取局域网中的所有ip
					int affix=1;
					int start=Integer.parseInt(masksegs[3]);
					int sum=256-Integer.parseInt(masksegs[3])-2;						
					
					for (; affix<=sum; affix++) {
						String tmpIP=new String(tmpString + (start+affix));
						
						if (!tmpIP.equals(ipString)) {
							Log.v("b" + affix, tmpIP);
							data = ("ping:" + (new Date(
									System.currentTimeMillis())).getTime()+":")
									.getBytes();
							ipInetAddress = InetAddress.getByName(tmpIP);
							datagramPacket = new DatagramPacket(data,
									data.length, ipInetAddress, 22222);
							datagramSocket.send(datagramPacket);
						}
					}
					Thread.sleep(1000 * 30);
					int count = 0;
					while (checkNetwork()) {
						for (dateofpeer tmp : peers) {
							data = ("ping:" + (new Date(
									System.currentTimeMillis())).getTime()+":")
									.getBytes();
							ipInetAddress = InetAddress.getByName(tmp.peerip
									);
							datagramPacket = new DatagramPacket(data,
									data.length, ipInetAddress, 22222);
							datagramSocket.send(datagramPacket);
						}
						Thread.sleep(1000 * 30);
						if (count != 0) {
							//如果一分钟内没有收到pong信息，则认为节点失效
							count = 0;
							for (dateofpeer dop : peers) {
								if (System.currentTimeMillis()
										- dop.date.getTime() > 1000 * 60) {
									peers.remove(dop);
								}
							}
						} else
							count++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	//控制指令服务线程
	public class ControlServer implements Runnable
	{
		//利用udp协议对网络中的对等点进行控制信息的交换
		private final int port = 22222;
		private DatagramSocket datagramSocket;
		private int count;
		
		public ControlServer(){
			count = 0;
			try {
				datagramSocket = new DatagramSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
 
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.v("test", "controlserver");
			while (checkNetwork())
			{
				try {
					byte[] b=new byte[1024*200];
					DatagramPacket datagramPacket;					
					datagramPacket = new DatagramPacket(b,b.length);
					
					datagramSocket.receive(datagramPacket);
//					count ++;
					ControlServerRec csRec=new ControlServerRec(datagramPacket);
					new Thread(csRec).start();
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	//控制指令解析线程******
	public class ControlServerRec implements Runnable
	{
		private DatagramSocket datagramSocket;
		private DatagramPacket receivedatagramPacket,sendDatagramPacket;
		
		public ControlServerRec(DatagramPacket dp){
			try {
				datagramSocket=new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
			}
			receivedatagramPacket=dp;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			try {	
				byte[] buf = receivedatagramPacket.getData();
				byte[] send=null;
				int len=buf.length;
				String ip = receivedatagramPacket.getAddress().toString();	
				ip = ip.substring( ip.lastIndexOf("/") + 1 );
				int port = receivedatagramPacket.getPort();
				String msgString=new String(buf);
				Log.v("test", "resolvecommand:"+msgString);
				//对命令分段
				String[] msgSeg=msgString.split(":");
				//从对端发送的时间
				String dateString=msgSeg[1];
				//Date sendedDate=new Date(Long.parseLong(dateString));
				//对命令进行解析
				if(msgString.startsWith(instructions[0])/*msgString.startsWith("1234")*/)
				{
					//Log.v("test", msgString);
					int i=0;
					for(i=0;i<peers.size();i++){
						String tmpip=peers.get(i).peerip;
						if(tmpip.equals(ip)) break;
					}
					if(i<peers.size()){
						peers.get(i).date=new Date(System.currentTimeMillis());
					}else{
						peers.add(new dateofpeer(ip, new Date(System.currentTimeMillis())));
						
					}
					
					send=("pong:"+dateString+":").getBytes();
					sendDatagramPacket=new DatagramPacket(send, send.length, receivedatagramPacket.getAddress(), 22222);
					datagramSocket.send(sendDatagramPacket);
					datagramSocket.close();
				}
				else if(msgString.startsWith(instructions[1]))
				{
					int i=0;
					for(i=0;i<peers.size();i++){
						String tmpip=peers.get(i).peerip;
						if(tmpip.equals(ip)) break;
					}
					if(i<peers.size()){
						peers.get(i).date=new Date(System.currentTimeMillis());
					}else{
						peers.add(new dateofpeer(ip, new Date(System.currentTimeMillis())));
						
					}
					
				}
				else if (msgSeg[0].equals((instructions[2]))) 
				{
					Log.v("test", msgString);
					//两种query请求，请求节点文件信息，请求传输文件
					String keymsg=msgSeg[1];
					if(keymsg.equals("info")){
						String filename=msgSeg[2];
						if(filename.equals("*")){
							String[] fileinfo=mDataOperater.allfileinfo();
							
							String cmdstr=instructions[3]+":info:*:";
							if (fileinfo==null||fileinfo.length==0) {
								cmdstr+="null;";
							}							
							else{
								for (String s : fileinfo) {
									cmdstr += s + ";";
								}
							}
							
							cmdstr=cmdstr.substring(0, cmdstr.length()-1);
							cmdstr+=":";
							send=cmdstr.getBytes();
							//假设send的长度小于65535
							sendDatagramPacket=new DatagramPacket(send, send.length, receivedatagramPacket.getAddress(), 22222);
							datagramSocket.send(sendDatagramPacket);
							datagramSocket.close();
							
						}
						else{
							String[] fileinfo=mDataOperater.fileNameExists(filename);
							String cmdstr=instructions[3]+":info:"+filename+":";
							
							if (fileinfo==null||fileinfo.length==0) {
								cmdstr+="null;";
							}							
							else{
								for (String s : fileinfo) {
									cmdstr += s + ";";
								}
							}
							cmdstr=cmdstr.substring(0, cmdstr.length()-1);
							cmdstr+=":";
							send=cmdstr.getBytes();
							//假设send的长度小于65535
							sendDatagramPacket=new DatagramPacket(send, send.length, receivedatagramPacket.getAddress(), 22222);
							datagramSocket.send(sendDatagramPacket);
							datagramSocket.close();
						}
					}
					else if(keymsg.equals("file")){
						String filesha1=msgSeg[2];
						int seg=Integer.parseInt(msgSeg[3]);
						File reqfile=mDataOperater.getFile(filesha1);
						if(reqfile==null) return; 
						byte[] fileseg=IOperation.getFileSeq(reqfile, (seg-1)*1024*16);
						
						String cmdstr=instructions[3]+":file:"+filesha1+":"+seg+":"+fileseg.length+":";
						byte[] cmdbytes=cmdstr.getBytes();
						int lenofcmd=cmdbytes.length+String.valueOf(cmdbytes.length).getBytes().length+
								":".getBytes().length;
						cmdstr+=String.valueOf(lenofcmd)+":";
						cmdbytes=cmdstr.getBytes();
						
						send=new byte[lenofcmd+fileseg.length];
						for(int i=0;i<lenofcmd;i++) send[i]=cmdbytes[i];
						for(int i=0;i<fileseg.length;i++) send[i+cmdbytes.length]=fileseg[i];
						
						sendDatagramPacket=new DatagramPacket(send, send.length, receivedatagramPacket.getAddress(), 22222);
						datagramSocket.send(sendDatagramPacket);
						datagramSocket.close();
					}
					
				}
				else if(msgString.startsWith(instructions[3]))
				{
					Log.v("test1", msgString);
					String keymsg=msgSeg[1];
					if(keymsg.equals("info")){
						String filename=msgSeg[2];
						if(filename.equals("*")){
							if (!msgSeg[3].equals("null")) {								
								PeerResourceActivity.onRECinfo(msgSeg[3],ip);							
							}
							else {
								PeerResourceActivity.onRECinfo(null,ip);
							}
						}
						else{
							if (!msgSeg[3].equals("null")) {
								String[] fileinfo = msgSeg[3].split(";");
								for(String tmp:fileinfo){
									SearchResourceActivity.onRECinfo(tmp, ip);
								}
							}							
						}
					}
					else{//收到文件分片
						String filesha1=msgSeg[2];
						int seg=Integer.parseInt(msgSeg[3]);
						Mission recMission=missions.get(filesha1);
						
						if(recMission==null) return;
						
						int filebyteslen=Integer.parseInt(msgSeg[4]);//获得文件分片字节数
						int cmdbyteslen=Integer.parseInt(msgSeg[5]);//获得控制命令字节数					
						
						byte[] filebuf=new byte[filebyteslen];
						for(int i=0;i<filebyteslen;i++){
							filebuf[i]=buf[i+cmdbyteslen];
						}
						
						recMission.onRECseg(filebuf, receivedatagramPacket.getAddress(), seg);
					}
					
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	}
	
}
	public void addMission(Mission mission){
		missions.put(mission.fileinfo.sha1, mission);
	}
	
	public void rmMission(Mission mission){
		missions.remove(mission.fileinfo.sha1);
	}
	public Mission getMission(String key){
		return missions.get(key);
	}
	
	public void sendqueryALLfileinfo(final InetAddress target){
		new Thread(new Runnable() {
			
			public void run() {
			try {
				DatagramSocket datagramSocket=new DatagramSocket();
				byte [] buf=(instructions[2]+":info:*:").getBytes();
				DatagramPacket datagramPacket=new DatagramPacket(buf, buf.length, target, 22222);
				datagramSocket.send(datagramPacket);
				datagramSocket.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			}
		}).start();
		
	}
	
	public void sendqueryTHEfileinfo(final InetAddress target, final String filename){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					try {
						DatagramSocket datagramSocket=new DatagramSocket();
						byte [] buf=(instructions[2]+":info:"+filename+":").getBytes();
						DatagramPacket datagramPacket=new DatagramPacket(buf, buf.length, target, 22222);
						datagramSocket.send(datagramPacket);
						datagramSocket.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			
	}
	
	public void queryThefile(final InetAddress target, final String sha1ofthefile, final int seg){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
				DatagramSocket datagramSocket=new DatagramSocket();
				byte [] buf=(instructions[2]+":file:"+sha1ofthefile+":"+seg+":").getBytes();
				DatagramPacket datagramPacket=new DatagramPacket(buf, buf.length, target, 22222);
				datagramSocket.send(datagramPacket);
				datagramSocket.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			}
		}).start();
		
	}
}
