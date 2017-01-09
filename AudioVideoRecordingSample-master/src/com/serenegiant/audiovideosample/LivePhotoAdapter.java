package com.serenegiant.audiovideosample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LivePhotoAdapter extends BaseAdapter {
	Context mContext;
	final File iLivePhotoDir;
	String unitstring = null;
	private static final String DIR_NAME = "iLivePhoto";
	List<String> mPhotoImagePath = new ArrayList<String>();
	private LayoutInflater inflater = null;

	public LivePhotoAdapter(Context context) {
		mContext = context;
		inflater = LayoutInflater.from(context);
		initImageLoader(context);
		iLivePhotoDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), DIR_NAME);

		File[] files = iLivePhotoDir.listFiles();
		if (files == null || files.length < 1) {
			return;
		}
		mPhotoImagePath.clear();
		for (File f : files) {
			String name = f.getName().toLowerCase();
			if (f.isDirectory()) {
				continue;
			} else if (name.endsWith(".jpg")) {
				mPhotoImagePath.add(name);
			}
		}
	}
	
	public String getVideoPath(int postion){
 		 String defText =  iLivePhotoDir.getPath() + "/"+mPhotoImagePath.get(postion);
		 String name1 = defText.replace(".jpg", ".mp4");
		 File defFile = new File(defText.replace(".jpg", ".mov"));
		 if(defFile.exists()){
			 return defFile.getPath();
		 }
		 return name1;
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove
				.build();
		ImageLoader.getInstance().init(config);
	}

	public int getLivePhotoNums() {
		return mPhotoImagePath.size();
	}

	@Override
	public int getCount() {
		return mPhotoImagePath.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.livephoto_item, null);
			holder = new Holder();
			holder.tv = (TextView) convertView.findViewById(R.id.itemName);
			holder.img = (ImageView) convertView.findViewById(R.id.itemImage);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		String fileN = mPhotoImagePath.get(position);
		fileN = fileN.substring(0, fileN.indexOf(".jpg"));
		holder.tv.setText(fileN);
		String dirFull = iLivePhotoDir.getPath() + "/" + mPhotoImagePath.get(position);
		Log.e("yangdi", "tv " + mPhotoImagePath.get(position));
		Log.e("yangdi", "getView " + dirFull);
		ImageLoader.getInstance().displayImage("file:///" + dirFull, holder.img);
		return convertView;
	}

	private class Holder {
		TextView tv = null;
		ImageView img = null;
	}

}
