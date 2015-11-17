package com.example.p2pfirstdemo;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Controller mController;	
	private String[] nameStrings={"资源查询","按节点查询资源","任务管理","添加本地共享"};
	private int[] bitmaps={R.drawable.search,R.drawable.jiediansearch,R.drawable.mission,R.drawable.add};
	private TextView t1,t2;
	private GridView myGridView ;
	private Handler mHandler;
	private String sdPath;
	private Intent mainServerIntent;
	File rootDir;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		rootDir=new File(getResources().getString(R.string.home_path));
		if(!rootDir.exists()){
			rootDir.mkdir();
			new File(rootDir.getPath()+"/fileInfo").mkdir();
			new File(getResources().getString(R.string.p2ptransfer_storage)).mkdir();
		}
		
		mController=Controller.getInstance(MainActivity.this);		
		myGridView=(GridView)findViewById(R.id.gridview1);
		t1=(TextView)findViewById(R.id.textview1);
		t2=(TextView)findViewById(R.id.textview2);
		
		mHandler=new Handler(){
			public void handleMessage(Message msg){
				switch(msg.what){
					case 1:{
						Toast.makeText(MainActivity.this, "not using a wifi network!", Toast.LENGTH_SHORT).show();
						t1.setText(mController.ipString);
						t2.setText(mController.maskString);
						break;
						}
					case 2:{							
						t1.setText(mController.ipString);
						t2.setText(mController.maskString);
						break;
					}
					default: break;
				}
			}
		};

		myGridView.setAdapter(new myadapter(MainActivity.this, myGridView));
		myGridView.setOnItemClickListener(new OnItemClickListener(){
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
//				Toast.makeText(MainActivity.this, ""+arg2, Toast.LENGTH_SHORT).show();
				switch(arg2){
				case 0:{
					Intent searchresearchIntent=new Intent(MainActivity.this,SearchResourceActivity.class);
					startActivity(searchresearchIntent);
					break;
				}
				case 1:{
//					//db test
//					File infoshow=new File(getResources().getString(com.example.p2pfirstdemo.R.string.p2ptransfer_storage)+"/sqlitedate");					
//					if(mController.mDataOperater.allfileinfo()!=null)
//						IOperation.writeFile(mController.mDataOperater.allfileinfo()[0], infoshow);
					
					Intent peerresourchsearchIntent=new Intent(MainActivity.this,PeerResourceActivity.class);
					startActivity(peerresourchsearchIntent);
					break;
				}
				case 2:{
					Intent missionsmanageIntent=new Intent(MainActivity.this,MissionsManagerActivity.class);
					startActivity(missionsmanageIntent);
					break;
				}
				case 3:{					
//					Intent addLocalResouceIntent=new Intent(MainActivity.this,fileListActivity.class);					
					Intent addLocalResouceIntent=new Intent(MainActivity.this,SharedFilesListActivity.class);
					startActivity(addLocalResouceIntent);
					break;
				}
				}
			}
		});
		mainServerIntent=new Intent(MainActivity.this, MainServer.class);
		startService(mainServerIntent);

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
		mController=Controller.getInstance(MainActivity.this);
		MainServer.setContext(this);
		MainServer.setHandler(mHandler);
		mController.showNoWifi=true;
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		stopService(mainServerIntent);
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class myadapter extends BaseAdapter{
		private Context mContext;
		private View  mView;
		
		public myadapter(Context context, View v){
			mContext=context;
			mView=v;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return bitmaps.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return bitmaps[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			LayoutInflater inflater=getLayoutInflater();
			View view=inflater.inflate(R.layout.firstpage, null);
			
			ImageView image=(ImageView)view.findViewById(R.id.image);
			TextView text=(TextView)view.findViewById(R.id.text);
			image.setImageBitmap(((BitmapDrawable)mContext.getResources().getDrawable(bitmaps[arg0])).getBitmap());
			text.setText(nameStrings[arg0]);			
			view.setTag(nameStrings[arg0]);
			
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(
					mView.getWidth()/2-1-mView.getPaddingBottom()-mView.getPaddingTop(), mView.getHeight()/2-1-mView.getPaddingBottom()-mView.getPaddingTop());
			view.setLayoutParams(params);
			
			return view;
		}
		
	}

}
