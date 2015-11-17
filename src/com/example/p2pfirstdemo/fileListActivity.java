package com.example.p2pfirstdemo;

import java.io.File;
import java.util.ArrayList;

import android.R;
import android.R.string;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class fileListActivity extends ListActivity {
	private Controller mController;
	private ListView listView;
	private fileListAdapter myFileListAdapter;
	private ArrayList<String> names,paths;
	private static String pwd="/mnt/sdcard";
	private DataOperater mDataOperater;
	private Handler mHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(com.example.p2pfirstdemo.R.layout.activity_filelist);
//		initialize
		mController=Controller.getInstance(fileListActivity.this);
		mDataOperater=DataOperater.getInstance(fileListActivity.this);
		names=new ArrayList<String>();
		paths=new ArrayList<String>();
		listView=(ListView)findViewById(R.id.list);
		mHandler=new Handler(){
			public void handleMessage(Message msg){
				switch(msg.what){
				case 1:Toast.makeText(fileListActivity.this, "not using a wifi network!", Toast.LENGTH_SHORT).show();break;
				
				default: break;
			}
			}
		};		
		
//		get directory for root
		IOperation.getDir(names, paths, pwd);
		
		myFileListAdapter=new fileListAdapter(fileListActivity.this, names, paths);
		listView.setAdapter(myFileListAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				String path= paths.get(arg2).toString();
				File file=new File(path);
				
				if(file.exists()&&file.canRead()){
					if(file.isDirectory()){	
//						ArrayList<String> n,p;
//						n=new ArrayList<String>();
//						p=new ArrayList<String>();
						IOperation.getDir(names, paths, path);
						Toast.makeText(fileListActivity.this, path, Toast.LENGTH_SHORT).show();
//						reload listview
						myFileListAdapter.setNames(names);
						myFileListAdapter.setPaths(paths);
						
						myFileListAdapter.notifyDataSetChanged();
						
						pwd=path;
							
					}
					else{ 
						onFileItemClick(file);
						mController.localFileInfo=mController.mDataOperater.allfileinfo();
					}
				}
				else
				{
					new AlertDialog.Builder(fileListActivity.this)
					.setTitle("Message")
					.setMessage("no permission")
					.setPositiveButton("OK", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					}).show();
				}
				
				
			}
		});
		
	}
	

	private void onFileItemClick(File file){
		ArrayList<String> info=new ArrayList<String>();
		IOperation.makeFileInfo(file, getResources().getString(com.example.p2pfirstdemo.R.string.p2ptransfer_storage)+"/"+file.getName()+".sha1",info);
		//Log.v("c", (info==null)+"");
		mDataOperater.writeFileInfo(info);
		
		Toast.makeText(fileListActivity.this, "this is a file, make info done", Toast.LENGTH_SHORT).show();
	}

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
	
}
