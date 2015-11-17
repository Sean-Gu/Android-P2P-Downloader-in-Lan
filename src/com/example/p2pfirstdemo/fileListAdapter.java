package com.example.p2pfirstdemo;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class fileListAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private Bitmap directory,fileBitmap;
	//	names of stored file
	private ArrayList<String> names = null;
	//	paths of stored file
	private ArrayList<String> paths =null;
	public fileListAdapter(Context context, ArrayList<String> na, ArrayList<String> pa) {
		// TODO Auto-generated constructor stub
		names=na;
		paths=pa;
		directory=BitmapFactory.decodeResource(context.getResources(), R.drawable.path);
		fileBitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.file);
		inflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return names.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return names.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		viewholder holder;
		if(null == arg1){
			arg1=inflater.inflate(R.layout.activity_filelistitem, null);
			holder=new viewholder();
			holder.image=(ImageView) arg1.findViewById(R.id.filetypeimage);
			holder.text=(TextView) arg1.findViewById(R.id.filename);
			arg1.setTag(holder);
		}
		else{
			holder=(viewholder)arg1.getTag();
		}
		
		File f= new File(paths.get(arg0));
		if(".."==names.get(arg0)){
			holder.text.setText("..");
			holder.image.setImageBitmap(directory);
			
		}
		else{
			holder.text.setText(f.getName());
			if(f.isDirectory())
				holder.image.setImageBitmap(directory);
			else holder.image.setImageBitmap(fileBitmap);
		}
		return arg1;
	}
	
	public void setNames(ArrayList<String> na){
//		names.clear();
//		names.addAll(na);
		
	}
	
	public void setPaths(ArrayList<String> pa){
//		paths.clear();
//		paths.addAll(pa);
		
	}
	private class viewholder{
		private TextView text;
		private ImageView image;
	}
}
