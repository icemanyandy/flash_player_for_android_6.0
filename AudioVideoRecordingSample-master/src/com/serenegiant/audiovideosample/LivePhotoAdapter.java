package com.serenegiant.audiovideosample;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

		loadImageList();
	}

	public void loadImageList(){

		File[] files = iLivePhotoDir.listFiles();
		if (files == null || files.length < 1) {
			return;
		}
		mPhotoImagePath.clear();
		List<File> listFile = Arrays.asList(files);
		Collections.sort(listFile, new Comparator<File>() {
			public int compare(File file, File newFile) {
				if (file.lastModified() < newFile.lastModified()) {
					return 1;
				} else if (file.lastModified() == newFile.lastModified()) {
					return 0;
				} else {
					return -1;
				}

			}
		});

		for (File f : listFile) {
			String name = f.getName().toLowerCase();
			if (f.isDirectory()) {
				continue;
			} else if (name.endsWith(".jpg")) {
				mPhotoImagePath.add(name);
			}
		}
	}

	public String getImagePath(int postion){
		return iLivePhotoDir.getPath() + "/" + mPhotoImagePath.get(postion);
	}

	public String getVideoPath(int postion) {
		String defText = iLivePhotoDir.getPath() + "/" + mPhotoImagePath.get(postion);
		String defaultName = defText.replace(".jpg", ".mp4");
		File defFile = new File(defaultName);
		if (defFile.exists()) {
			return defFile.getPath();
		}
		List<String> supportVideo = new ArrayList<>();
		supportVideo.add(".mov");
		supportVideo.add(".mkv");
		supportVideo.add(".wmv");
		supportVideo.add(".avi");
		supportVideo.add(".mpg");
		supportVideo.add(".mpeg");
		supportVideo.add(".dat");
		supportVideo.add(".3gp");
		for(String sv:supportVideo) {
			defFile = new File(defText.replace(".jpg", sv));
			if (defFile.exists()) {
				return defFile.getPath();
			}
		}
		return defaultName;
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
