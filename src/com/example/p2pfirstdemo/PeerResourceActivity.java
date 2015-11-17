package com.example.p2pfirstdemo;

import java.net.InetAddress;
import java.util.ArrayList;

import com.example.p2pfirstdemo.Controller.dateofpeer;
import com.example.p2pfirstdemo.PeerResourceItemActivity.peerResourceItemAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PeerResourceActivity extends Activity {
	public static ArrayList<peerFilesInfo> files=new ArrayList<peerFilesInfo>();

	private Controller mController;
	private static Handler mHandler;
	private ListView listView;
	private peerResourceAdapter mAdapter;
	private ProgressDialog mProgressDialog;
	//发送对节点信息的请求包 
	private void sendRequest(){
		try {
			//??????????要改
//			mController.sendqueryALLfileinfo(InetAddress.getByName("192.168.191.2"));
			
			for(dateofpeer ip: mController.peers){
			mController.sendqueryALLfileinfo(InetAddress.getByName(ip.peerip));
			}
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Message msg=new Message();
					msg.what=3;					
					mHandler.sendMessage(msg);
				}
			}).start();
			
		} catch (Exception e) {					
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peerresource);
		
		listView=(ListView)findViewById(R.id.peerresourcelistview);
		mAdapter=new peerResourceAdapter(PeerResourceActivity.this);
		listView.setAdapter(mAdapter);
		mController=Controller.getInstance(PeerResourceActivity.this);
		
		mHandler=new Handler(){
			public void handleMessage(Message msg){
				switch(msg.what){
				case 1:Toast.makeText(PeerResourceActivity.this, "not using a wifi network!", Toast.LENGTH_SHORT).show();break;
				case 2:mAdapter.notifyDataSetChanged();break;
				case 3:{mProgressDialog.dismiss();mAdapter.notifyDataSetChanged();break;}
				default: break;
			}
			}
		};	
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				peerFilesInfo peerinfos=(peerFilesInfo)arg1.getTag();
				Intent intent =new Intent(PeerResourceActivity.this,PeerResourceItemActivity.class);
				for(peerFilesInfo pfi: files){					
					for(fileInfo fi:peerinfos.fileInfos){
						for(fileInfo tmp:pfi.fileInfos){
							if(fi.sha1.equals(tmp.sha1)&&(!fi.peers.contains(pfi.ip))){
								fi.peers.add(pfi.ip);
								break;
							}
						}
					}					
				}
				PeerResourceItemActivity.files=peerinfos.fileInfos;
				
				startActivity(intent);
			}
		});
		
		mProgressDialog=ProgressDialog.show(PeerResourceActivity.this, "please wait", "querying", true);
		
		sendRequest();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mController.showNoWifi=false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mController=Controller.getInstance(PeerResourceActivity.this);
		MainServer.setContext(this);
		MainServer.setHandler(mHandler);
		mController.showNoWifi=true;
		super.onResume();
	}
		
	public static void onRECinfo(String info, String ip){
		for(peerFilesInfo p:files){
			if(p.ip.equals(ip)) return;
		}
		files.add(new peerFilesInfo(info, ip));
		
		Message msg=new Message();
		msg.what=2;					
		mHandler.sendMessage(msg);
	}
	
	public class peerResourceAdapter extends BaseAdapter{
		private Context mContext;
		
		public peerResourceAdapter(Context context){
			mContext=context;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return files.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return files.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			TextView tv=new TextView(mContext);
			tv.setText(files.get(arg0).ip+"   资源数："+files.get(arg0).fileInfos.size());		
			tv.setTextSize(25);
			tv.setTextColor(Color.rgb(0, 0, 0));
			tv.setTag(files.get(arg0));
			return tv;
		}
		
	}
	
	@SuppressLint("NewApi")
	@Override
	//添加actionbar中的菜单项
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem menuItem=menu.add(0, 0, 0, "刷新");
		menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	//添加共享文件
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
//		Toast.makeText(SharedFilesListActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
		this.onCreate(null);
		return super.onMenuItemSelected(featureId, item);
	}
}
