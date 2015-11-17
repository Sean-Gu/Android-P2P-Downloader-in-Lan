package com.example.p2pfirstdemo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;

import com.example.p2pfirstdemo.Controller.dateofpeer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class SearchResourceActivity extends Activity {
	public static ArrayList<fileInfo> files=new ArrayList<fileInfo>();//按名字搜索的结果
	
	private Controller mController;
	private static Handler mHandler;
	private TextView inputView;
	private Button searchButton;
	private ListView showListView;
	myadapter adapter;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchresource);
		// TODO Auto-generated method stub
		inputView=(TextView)findViewById(R.id.searchEditText);
		searchButton=(Button)findViewById(R.id.searchButton);
		showListView=(ListView)findViewById(R.id.searchlistview);
		mController=Controller.getInstance(SearchResourceActivity.this);
		
		adapter=new myadapter(SearchResourceActivity.this);
		showListView.setAdapter(adapter);
		
		mHandler=new Handler(){
			public void handleMessage(Message msg){
				switch(msg.what){
				case 1:Toast.makeText(SearchResourceActivity.this, "not using a wifi network!", Toast.LENGTH_SHORT).show();break;
				case 2:adapter.notifyDataSetChanged();break;
				case 3:{progressDialog.dismiss();adapter.notifyDataSetChanged();break;}
				default: break;
			}
			}
		};		
				
		searchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String name=inputView.getText().toString();
				if(name.equals("")){ 
					Toast.makeText(SearchResourceActivity.this, "enter key words", Toast.LENGTH_SHORT).show();
					return;
				}
				
				try {
					files.clear();
					adapter.notifyDataSetChanged();
					//???????????要改
//					mController.sendqueryTHEfileinfo(InetAddress.getByName("192.168.191.2"), name);
					
					for(dateofpeer ip: mController.peers){
						mController.sendqueryTHEfileinfo(InetAddress.getByName(ip.peerip), name);
					}
					
					
//					new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							try {
//								Thread.sleep(3000);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//							Message msg=new Message();
//							msg.what=3;					
//							mHandler.sendMessage(msg);
//						}
//					}).start();
					
				} catch (Exception e) {					
					e.printStackTrace();
				}
				
			}
		});
		showListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				fileInfo download=(fileInfo)arg1.getTag();
				Mission mission=new Mission(download);
				mController.addMission(mission);
				for(String ip:download.peers){
					int nextseg=mission.getnextSEG();
					try {
						mController.queryThefile(InetAddress.getByName(ip), download.sha1, nextseg);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				Intent intent=new Intent(SearchResourceActivity.this, MissionsManagerActivity.class);
				startActivity(intent);
			}
		});
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
		mController=Controller.getInstance(this);
		MainServer.setContext(this);
		MainServer.setHandler(mHandler);
		mController.showNoWifi=true;
		super.onResume();
	}

	public class myadapter extends BaseAdapter{
		private Context mContext;
		
		public myadapter(Context context){
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
			TextView tv = new TextView(mContext);
			tv.setText(files.get(arg0).name+"   资源数："+files.get(arg0).peers.size());		
			tv.setTextSize(25);
			tv.setTextColor(Color.rgb(0, 0, 0));
			tv.setTag(files.get(arg0));
			return tv;
		}
		
	}
	
	public static void onRECinfo(String info, String ip){
		fileInfo fInfo=new fileInfo(info);
		for(fileInfo i: files){
			if(i.sha1.equals(fInfo.sha1)){
				i.peers.add(ip);
				return;
			}			
		}
		fInfo.peers.add(ip);
		files.add(fInfo);
		Message msg=new Message();
		msg.what=2;					
		mHandler.sendMessage(msg);		
	}
}
