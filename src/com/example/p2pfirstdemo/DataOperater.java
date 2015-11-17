package com.example.p2pfirstdemo;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataOperater{

	private static boolean created=false;
	private static DataOperater mDataOperater;
	private Context context;
	private SQLiteDatabase myDatabase;
	
	
	private DataOperater(Context con){
		context=con;
		File file=new File(context.getResources().getString(R.string.home_path));
		if(file.exists())
			myDatabase=SQLiteDatabase.openOrCreateDatabase(context.getResources().getString(R.string.home_path)+"/p2pdb.db3", null);
		else myDatabase=SQLiteDatabase.openOrCreateDatabase(context.getResources().getString(R.string.p2ptransfer_storage)+"/p2pdb.db3", null);
		
		String sql = "SELECT COUNT(*) FROM sqlite_master where type='table' and name='fileInfo'";
        Cursor cursor = myDatabase.rawQuery(sql, null);
        if(cursor.moveToNext()){
        	int count = cursor.getInt(0);
            if(count>0){
                    created = true;
            }
        }
		if(!created){
			String tmpString="create table fileInfo(sha1 varchar(255) primary key, name varchar(255), path varchar(255), segs integer ,size varchar(255))";
			myDatabase.execSQL(tmpString);
			
			created=true;
		}
	}
	
	public static DataOperater getInstance(Context con){
		if(null==mDataOperater) mDataOperater = new DataOperater(con);
		return mDataOperater;
	}
	
	public synchronized void writeFileInfo(ArrayList<String> info){
		//Log.v("c", ""+info.size());
		if(info.size()==5){			
			if(fileExists(info.get(0))==null){
			String tmpString="insert into fileInfo values('"+info.get(0)+"','"+info.get(1)+"','"+info.get(2)+"',"+info.get(3)+",'"+info.get(4)+"');";
			myDatabase.execSQL(tmpString);
			}
		}
	}
	
	public synchronized void delFile(String sha1code){
		myDatabase.execSQL("delete from fileInfo where sha1 = '"+sha1code+"';");
	}
	
	public synchronized String fileExists(String sha1code)
	{
		Cursor cursor = myDatabase.rawQuery("select * from fileInfo where sha1 = '"+sha1code+"';", null);
		if(cursor!=null&&cursor.getCount()>0){
			cursor.moveToNext();
			StringBuilder stringBuilder=new StringBuilder();
			stringBuilder.append(cursor.getString(0)+",");
			stringBuilder.append(cursor.getString(1)+",");
			stringBuilder.append(cursor.getString(2)+",");
			stringBuilder.append(cursor.getInt(3)+",");
			stringBuilder.append(cursor.getString(4));
			
			return stringBuilder.toString();
		}
		else return null ;
	}
	
	public synchronized String[] fileNameExists(String name){
		String[] keys=name.split(" ");
		Cursor cursor;
		if(keys.length==1){
			cursor= myDatabase.rawQuery("select * from fileInfo where name like '%"+keys[0]+"%' ;", null);
		}else{
			cursor= myDatabase.rawQuery("select * from fileInfo where name like '%"+keys[0]+"%"+keys[1]+"%' OR name='%"+keys[1]+"%"+keys[0]+"%' ;", null);
		}
		if(cursor!=null&&cursor.getCount()>=0){
			ArrayList<String> infoArrayList=new ArrayList<String>();
			while(cursor.moveToNext()){
				StringBuilder stringBuilder=new StringBuilder();
				
				stringBuilder.append(cursor.getString(cursor.getColumnIndex("sha1"))+",");
				stringBuilder.append(cursor.getString(cursor.getColumnIndex("name"))+",");
				stringBuilder.append(cursor.getString(cursor.getColumnIndex("path"))+",");
				stringBuilder.append(cursor.getInt(cursor.getColumnIndex("segs"))+",");
				stringBuilder.append(cursor.getString(cursor.getColumnIndex("size")));
				infoArrayList.add(stringBuilder.toString());
				
			}		
			String[] infoString = {};
			return infoArrayList.toArray(infoString);
		}
		else return null;
	}
	
	public synchronized int fileSegsNum(String sha1code){
		Cursor cursor = myDatabase.rawQuery("select segs from fileInfo where sha1 = '"+sha1code+"';", null);
		if(cursor.moveToNext())
			return cursor.getInt(0);
		return -1;
	}
	
	public synchronized File getFile(String sha1){
		File targetFile;
		Cursor cursor = myDatabase.rawQuery("select * from fileInfo where sha1 = '"+sha1+"';", null);
		if(!(cursor!=null&&cursor.getCount()>0)) return null;
		cursor.moveToNext();
		targetFile=new File(cursor.getString(2));
		return targetFile;
	}

	public synchronized int fileCount(){
		Cursor cursor = myDatabase.rawQuery("select COUNT(*) from fileInfo;", null);
		cursor.moveToNext();
		return cursor.getInt(0);
	}
	
	public synchronized String[] allfileinfo(){
		Cursor cursor = myDatabase.rawQuery("select * from fileInfo;", null);
		String[] allfileinfos={};
		if(cursor!=null&&cursor.getCount()>0){
			ArrayList<String> infoArrayList=new ArrayList<String>();
			while(cursor.moveToNext()){
				StringBuilder stringBuilder=new StringBuilder();
				stringBuilder.append(cursor.getString(cursor.getColumnIndex("sha1"))+",");
				stringBuilder.append(cursor.getString(cursor.getColumnIndex("name"))+",");
				stringBuilder.append(cursor.getString(cursor.getColumnIndex("path"))+",");
				stringBuilder.append(cursor.getInt(cursor.getColumnIndex("segs"))+",");
				stringBuilder.append(cursor.getString(cursor.getColumnIndex("size")));
				infoArrayList.add(stringBuilder.toString());
				
			}		
			allfileinfos=new String[infoArrayList.size()];
			for(int i=0;i<infoArrayList.size();i++){
				allfileinfos[i]=infoArrayList.get(i);
			}
			
		}
		
		return allfileinfos;
	}
	
	
}
