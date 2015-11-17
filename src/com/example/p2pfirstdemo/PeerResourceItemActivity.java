package com.example.p2pfirstdemo;

import java.net.InetAddress;
import java.util.ArrayList;

import com.example.p2pfirstdemo.PeerResourceActivity.peerResourceAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PeerResourceItemActivity extends Activity {
	public static ArrayList<fileInfo> files=new ArrayList<fileInfo>();
	
	private Controller mController;
	private static Handler mHandler;
	private ListView listView;
	private peerResourceItemAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peerresource);
		
		listView=(ListView)findViewById(R.id.peerresourcelistview);
		mAdapter=new peerResourceItemAdapter(PeerResourceItemActivity.this);
		listView.setAdapter(mAdapter);
		mController=Controller.getInstance(PeerResourceItemActivity.this);
		mHandler=new Handler(){
			public void handleMessage(Message msg){
				switch(msg.what){
				case 1:Toast.makeText(PeerResourceItemActivity.this, "not using a wifi network!", Toast.LENGTH_SHORT).show();break;
				case 2:mAdapter.notifyDataSetChanged();break;				
				default: break;
			}
			}
		};
		
		listView.setOnItemClickListener(new OnItemClickListener() {

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
				Intent intent=new Intent(PeerResourceItemActivity.this, MissionsManagerActivity.class);
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
		mController=Controller.getInstance(PeerResourceItemActivity.this);
		MainServer.setContext(this);
		MainServer.setHandler(mHandler);
		mController.showNoWifi=true;
		super.onResume();		
	}

	public class peerResourceItemAdapter extends BaseAdapter{
		private Context mContext;
		
		public peerResourceItemAdapter(Context context){
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
			tv.setText(files.get(arg0).name+"   ×ÊÔ´Êý£º"+files.get(arg0).peers.size());		
			tv.setTextSize(25);
			tv.setTextColor(Color.rgb(0, 0, 0));
			tv.setTag(files.get(arg0));
			return tv;
		}		
	}
}
