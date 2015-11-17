package com.example.p2pfirstdemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MainServer extends Service {
	private static Context mContext;
	private static Handler mHandler;
	private Controller mController;	
	private Controller.PeerFindThread peerFindThread;
	private Controller.ControlServer controlServerThread;
	
	
	public static void setContext(Context context){
		mContext=context;
	}
	
	public static void setHandler(Handler handler){
		mHandler=handler;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		mController=Controller.getInstance();
		peerFindThread=mController.new PeerFindThread();
		controlServerThread=mController.new ControlServer();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mController=Controller.getInstance();		
				
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Log.v("test", "checknetwork");
				
				// TODO Auto-generated method stub
				while(mController.showNoWifi){
					if(!mController.checkNetwork()){
						mController.ipString = "";
						mController.maskString= "";
						Message msg=new Message();
						msg.what=1;					
						mHandler.sendMessage(msg);
						
					}
					else{
						mController.ipString = mController.getLocalIpAddress();
						mController.maskString= mController.getMask();
						Message msg=new Message();
						msg.what=2;
						mHandler.sendMessage(msg);
					}
					try {
						Thread.sleep(2000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();		
		
		//Log.v("a", "control");
		new Thread(peerFindThread).start();
		new Thread(controlServerThread).start();		
		
		return super.onStartCommand(intent, flags, startId);
	}

	

}
