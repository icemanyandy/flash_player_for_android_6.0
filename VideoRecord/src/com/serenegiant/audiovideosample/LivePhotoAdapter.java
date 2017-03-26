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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.serenegiant.glutils.FileNameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LivePhotoAdapter extends BaseAdapter {
	private static DisplayImageOptions options;
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

		File[] files = iLivePhotoDir.listFiles(new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				String path = getVideoPath(pathname.getPath());
				if(path != null)
					return true;
				return false;
			}
		});
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
				String tname = name.substring(0, name.indexOf(".jpg"));
				boolean ret = SettingTool.getInstance().getData(tname,false);
				if(ret) {
					mPhotoImagePath.add(name);
				}
			}
		}
	}

	public String getImagePath(int postion){
		return iLivePhotoDir.getPath() + "/" + mPhotoImagePath.get(postion);
	}
	public String getVideoPath(int postion){
		return getVideoPath(getImagePath(postion));
	}

	public String getVideoPath(String path) {
		return FileNameUtils.getVideoPath(path);
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
				//.writeDebugLogs() // Remove
				.build();
		ImageLoader.getInstance().init(config);
		options = new DisplayImageOptions.Builder()
				//.showImageForEmptyUri(R.drawable.default_pic_nine) // 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.default_pic_nine) // 设置图片加载或解码过程中发生错误显示的图片
				.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
				.build();
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
		ImageLoader.getInstance().displayImage("file:///" + dirFull, holder.img,options);
		return convertView;
	}

	private class Holder {
		TextView tv = null;
		ImageView img = null;
	}

}
