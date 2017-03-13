package com.serenegiant.online;

import android.content.Context;
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
import com.serenegiant.audiovideosample.R;

import java.util.ArrayList;
import java.util.List;

public class OnlineLivePhotoAdapter extends BaseAdapter {
	Context mContext;
 	private static final String DIR_NAME = "iLivePhoto";
	List<String> mPhotoImagePath = new ArrayList<String>();
	private LayoutInflater inflater = null;
	List<OnlineLivePhotoItem> itemList;
	public OnlineLivePhotoAdapter(Context context,List<OnlineLivePhotoItem> list) {
		mContext = context;
		inflater = LayoutInflater.from(context);
		initImageLoader(context);
		itemList = list;

 	}

	public void setList(List<OnlineLivePhotoItem> list){
		itemList = list;
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
				.build();
		ImageLoader.getInstance().init(config);
	}

	public int getLivePhotoNums() {
		return mPhotoImagePath.size();
	}

	@Override
	public int getCount() {
		return itemList == null ?0:itemList.size();
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
  		holder.tv.setText(itemList.get(position).title);
		holder.img.setImageResource(R.drawable.default_pic_nine);
		ImageLoader.getInstance().displayImage(itemList.get(position).picUrl, holder.img);
		return convertView;
	}

	private class Holder {
		TextView tv = null;
		ImageView img = null;
	}

}
