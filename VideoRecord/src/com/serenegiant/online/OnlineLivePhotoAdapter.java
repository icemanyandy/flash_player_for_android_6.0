package com.serenegiant.online;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspsine.multithreaddownload.RequestDownloadInfo;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.serenegiant.audiovideosample.R;

import java.util.ArrayList;
import java.util.List;

public class OnlineLivePhotoAdapter extends BaseAdapter {
	Context mContext;
 	private static final String DIR_NAME = "iLivePhoto";
	List<String> mPhotoImagePath = new ArrayList<String>();
	private LayoutInflater inflater = null;
	List<OnlineLivePhotoItem> itemList;
	boolean useDefaultImg = false;
	public OnlineLivePhotoAdapter(Context context,List<OnlineLivePhotoItem> list) {
		mContext = context;
		inflater = LayoutInflater.from(context);
		initImageLoader(context);
		itemList = list;
		useDefaultImg = true;
 	}

	public void setList(List<OnlineLivePhotoItem> list){
		itemList = list;
		useDefaultImg = true;
	}

	public List<OnlineLivePhotoItem> getItemList(){
		return itemList;
	}

	public static void initImageLoader(Context context) {
		BitmapFactory.Options ops = new BitmapFactory.Options();
		ops.inJustDecodeBounds = true;
		ops.inPreferredConfig = Bitmap.Config.ARGB_8888;
		DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
   		                     .imageScaleType(ImageScaleType.EXACTLY) // default
		                     .bitmapConfig(Bitmap.Config.ARGB_8888) // default
							.decodingOptions(ops)
				.build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().defaultDisplayImageOptions(displayImageOptions)
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
	public OnlineLivePhotoItem getItem(int arg0) {
		 if(itemList != null && arg0<itemList.size())
			 return itemList.get(arg0);
		return  null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Holder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.online_livephoto_item, null);
			holder = new Holder();
			holder.tv = (TextView) convertView.findViewById(R.id.itemName);
			holder.img = (ImageView) convertView.findViewById(R.id.itemImage);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.itemProgress);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		OnlineLivePhotoItem item = itemList.get(position);
  		holder.tv.setText(item.title);
		holder.progressBar.setProgress(item.download_progress);

		if(useDefaultImg) {
			holder.img.setImageResource(R.drawable.default_pic_nine);
		}
		String imgURl = item.picUrl;
		//ImageLoader.getInstance().displayImage(imgURl, holder.img);
		if(position == getCount()-1){
			useDefaultImg = false;
		}

		if(item.download_state == RequestDownloadInfo.STATUS_CONNECTING){
			holder.progressBar.setBackgroundColor(Color.YELLOW);
		}else if(item.download_state == RequestDownloadInfo.STATUS_DOWNLOAD_ERROR
				|| item.download_state == RequestDownloadInfo.STATUS_NOT_DOWNLOAD){
			holder.progressBar.setBackgroundColor(Color.RED);
		}else{
			holder.progressBar.setBackgroundColor(Color.TRANSPARENT);
		}
		ImageLoader.getInstance().loadImage(imgURl, new ImageLoadingListener() {

			@Override
			public void onLoadingStarted(String s, View view) {
			}

			@Override
			public void onLoadingFailed(String s, View view, FailReason failReason) {
			}

			@Override
			public void onLoadingComplete(String s, View view, Bitmap bitmap) {
				holder.img.setImageBitmap(bitmap);
			}

			@Override
			public void onLoadingCancelled(String s, View view) {

			}
		});
		return convertView;
	}

	private class Holder {
		TextView tv = null;
		ImageView img = null;
		ProgressBar progressBar;
	}

}
