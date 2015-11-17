package com.example.p2pfirstdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SharedFilesListActivity extends Activity {
	private Controller mController;
	private static Handler mHandler;
	private ListView listView;
	private myadapter adapter;
	private String[] items={"移除任务","移除任务及文件"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activiry_sharedfileslist);
		
		mController=Controller.getInstance(SharedFilesListActivity.this);		
		mHandler=new Handler(){
			public void handleMessage(Message msg){
				switch(msg.what){
				case 1:Toast.makeText(SharedFilesListActivity.this, "not using a wifi network!", Toast.LENGTH_SHORT).show();break;
				
				default: break;
			}
			}
		};		
		listView=(ListView)findViewById(R.id.sharedfileslistview);
		adapter=new myadapter(SharedFilesListActivity.this);
		listView.setAdapter(adapter);
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0,final View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				Toast.makeText(SharedFilesListActivity.this, ((TextView)arg1).getText(), Toast.LENGTH_SHORT).show();
				
				new AlertDialog.Builder(SharedFilesListActivity.this) 
                .setTitle("对Item进行操作") 
                .setItems(items, new DialogInterface.OnClickListener() { 
                                        public void onClick(DialogInterface dialog, int which) { 
                                               switch (which) {
											case 0:{
												//delete shared file info
												fileInfo info=(fileInfo)arg1.getTag();
												mController.mDataOperater.delFile(info.sha1);
												mController.localFileInfo=mController.mDataOperater.allfileinfo();
												adapter.notifyDataSetChanged();
												break;
											}
											case 1:{
												//delete shared file info and the file
												fileInfo info=(fileInfo)arg1.getTag();
												mController.mDataOperater.delFile(info.sha1);
												IOperation.delFile(info.path);
												mController.localFileInfo=mController.mDataOperater.allfileinfo();
												adapter.notifyDataSetChanged();
												break;
											}
											default:break;
											} 
                                        } 
                                }) 
                .setNegativeButton("取消", 
                                new DialogInterface.OnClickListener() { 
                                        public void onClick(DialogInterface dialog, 
                                                        int which) { 
                                                // TODO Auto-generated method stub 

                                        } 
                                }).show(); 
				
				return false;
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
		adapter.notifyDataSetChanged();
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
			return mController.localFileInfo.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return mController.localFileInfo[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub		
			fileInfo info=new fileInfo(mController.localFileInfo[arg0]);
			TextView tv = new TextView(mContext);
			tv.setText(info.name+"   size："+info.size+"B");		
			tv.setTextSize(25);
			tv.setTextColor(Color.rgb(0, 0, 0));
			tv.setTag(info);
			return tv;
		}
		
	}

	@SuppressLint("NewApi")
	@Override
	//添加actionbar中的菜单项
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem menuItem=menu.add(0, 0, 0, "添加共享文件");
		menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	//添加共享文件
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
//		Toast.makeText(SharedFilesListActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
		Intent addLocalResouceIntent=new Intent(SharedFilesListActivity.this,fileListActivity.class);
		startActivity(addLocalResouceIntent);
		return super.onMenuItemSelected(featureId, item);
	}
	
	
}
