package com.example.p2pfirstdemo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.MissingFormatArgumentException;

import android.R.bool;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MissionsManagerActivity extends Activity {
	public static Handler mHandler;
	
	private Controller mController;	
	private ListView listView;
	private myadapter adapter;
	private String[] items1;
	private String[] items2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_missionsmanager);
		items1=new String[]{"暂停","删除"};
		items2=new String[]{"继续","删除"};
		mController=Controller.getInstance();
		listView=(ListView)findViewById(R.id.missionsmanagerlistview);
		adapter=new myadapter(this);
		listView.setAdapter(adapter);
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {				
				final Mission m=(Mission)arg1.getTag();
				boolean issuspended=m.ifpause;
				
				if (issuspended) {
					new AlertDialog.Builder(MissionsManagerActivity.this)
							.setTitle("对Item进行操作")
							.setItems(items2,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											switch (which) {
											case 0:
//												Toast.makeText(
//														MissionsManagerActivity.this,
//														items2[which],
//														Toast.LENGTH_SHORT)
//														.show();
												m.onResume();
												adapter.resetinfo();
												adapter.notifyDataSetChanged();
												break;
											case 1:
												Toast.makeText(
														MissionsManagerActivity.this,
														items2[which],
														Toast.LENGTH_SHORT)
														.show();
												m.delMission();
												adapter.resetinfo();
												adapter.notifyDataSetChanged();
												break;
											default:
												break;
											}
										}
									})
							.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {											
										}
									}).show();
				}
				else{
					new AlertDialog.Builder(MissionsManagerActivity.this)
					.setTitle("对Item进行操作")
					.setItems(items1,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
//										Toast.makeText(
//												MissionsManagerActivity.this,
//												items1[which],
//												Toast.LENGTH_SHORT)
//												.show();
										m.onPause();
										adapter.resetinfo();
										adapter.notifyDataSetChanged();
										break;
									case 1:
										Toast.makeText(
												MissionsManagerActivity.this,
												items1[which],
												Toast.LENGTH_SHORT)
												.show();
										m.delMission();
										adapter.resetinfo();										
										adapter.notifyDataSetChanged();
										break;
									default:
										break;
									}
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub 

								}
							}).show();
				}
				return false;
			}
			
		});
		
		mHandler=new Handler(){
			public void handleMessage(Message msg){
				switch(msg.what){
				case 1:Toast.makeText(MissionsManagerActivity.this, 
						"not using a wifi network!", Toast.LENGTH_SHORT).show();break;
				case 2:adapter.notifyDataSetChanged();break;
				
				default: break;
			}
			}
		};		
		
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				try {
//					Thread.sleep(500);
//					Message msg=new Message();
//					msg.what=2;
//					mHandler.sendMessage(msg);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}).start();
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
		private ArrayList<fileInfo> infos; 
		
		public myadapter(Context context){
			mContext=context;
			infos=new ArrayList<fileInfo>();
			Iterator iterator=mController.missions.keySet().iterator();
			while(iterator.hasNext()){
				String key=iterator.next().toString();
				infos.add(mController.missions.get(key).fileinfo);
			}
					
		}
		
		public void resetinfo(){
			infos=new ArrayList<fileInfo>();
			Iterator iterator=mController.missions.keySet().iterator();
			while(iterator.hasNext()){
				String key=iterator.next().toString();
				infos.add(mController.missions.get(key).fileinfo);
			}
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return infos.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return infos.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub		
			boolean issuspended=mController.missions.get(infos.get(arg0).sha1
					).ifpause;
			LayoutInflater inflater=(LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			arg1=inflater.inflate(R.layout.missionsmanagerlistviewitem, null);
			ProgressBar progressBar=(ProgressBar)arg1.findViewById(R.id.progressBar1);
			TextView tv =(TextView)arg1.findViewById(R.id.missionsmanagerlistviewitemtextview);	
			
			int[] segs = mController.missions.get(infos.get(arg0).sha1).segsREC;
			int progress = 0;
			for (int i : segs) {
				if (i == 1)
					progress++;
			}
			progressBar.setMax(segs.length);
			progressBar.setProgress(progress);
			if(progress==segs.length){
				
				tv.setText(infos.get(arg0).name + "   资源数："
						+ infos.get(arg0).peers.size() + "\n"
						+ "已完成");
			}else{
				tv.setText(infos.get(arg0).name + "   资源数："
						+ infos.get(arg0).peers.size() + "\n"
						+ (issuspended ? "暂停中" : "下载中"));
			}
			
			tv.setTextSize(25);
			tv.setTextColor(Color.rgb(0, 0, 0));
			
			arg1.setTag(mController.missions.get(infos.get(arg0).sha1));
		
			return arg1;
		}
		
	}
}
