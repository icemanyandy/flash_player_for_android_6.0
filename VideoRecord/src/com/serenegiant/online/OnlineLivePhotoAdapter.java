package com.serenegiant.online;

import android.content.Context;
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
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.serenegiant.audiovideosample.R;
import com.serenegiant.glutils.FileNameUtils;

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

	public List<OnlineLivePhotoItem> getItemList(){
		return itemList;
	}
	DisplayImageOptions options;
	public void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()//.defaultDisplayImageOptions(displayImageOptions)
				.discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
				.build();
		ImageLoader.getInstance().init(config);

		options = new DisplayImageOptions.Builder() //.showImageOnLoading(R.drawable.default_pic_nine)
				.showImageForEmptyUri(R.drawable.default_pic_nine) // 设置图片Uri为空或是错误的时候显示的图片
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
		return itemList == null ?0:itemList.size();
	}

	@Override
	public OnlineLivePhotoItem getItem(int arg0) {
		 if(itemList != null && arg0<itemList.size())
			 return itemList.get(arg0);
		return  null;
	}

	public void updateFileState(){
		if(itemList != null){
			for(OnlineLivePhotoItem item : itemList){
				String videoPath = FileNameUtils.getVideoPathByName(item.title);
				if(videoPath != null){
					item.download_progress = 100;
					item.download_state = RequestDownloadInfo.STATUS_COMPLETE;
				}
			}
		}
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
			holder.price = (TextView) convertView.findViewById(R.id.itemPrice);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		OnlineLivePhotoItem item = itemList.get(position);
  		holder.tv.setText(item.title);
		holder.progressBar.setProgress(item.download_progress);

		String imgURl = item.picUrl;
		//ImageLoader.getInstance().displayImage(imgURl, holder.img);

		holder.img.setBackgroundResource(R.drawable.default_pic_nine);
		if(item.download_state == RequestDownloadInfo.STATUS_CONNECTING){
			holder.progressBar.setBackgroundColor(Color.YELLOW);
		}else if(item.download_state == RequestDownloadInfo.STATUS_DOWNLOAD_ERROR
				|| item.download_state == RequestDownloadInfo.STATUS_NOT_DOWNLOAD){
			holder.progressBar.setBackgroundColor(Color.RED);
		}else{
			holder.progressBar.setBackgroundColor(Color.TRANSPARENT);
		}

		Float money = item.getFloatMoney();
		if( money == 0f || money == -1){
			holder.price.setText("免费");
			if(money == -1) {
				holder.price.setVisibility(View.INVISIBLE);
			}else{
				holder.price.setVisibility(View.VISIBLE);
			}
			holder.price.setBackgroundResource(R.drawable.price_tag_unselect);
		}else if(money == -2f){
			holder.price.setText("限免");
			holder.price.setVisibility(View.VISIBLE);
			holder.price.setBackgroundResource(R.drawable.price_tag_unselect);
		}else {
			holder.price.setText("¥"+money);
			holder.price.setVisibility(View.VISIBLE);
			holder.price.setBackgroundResource(R.drawable.price_tag_select);
		}
		if(item.download_state == RequestDownloadInfo.STATUS_COMPLETE) {
			holder.price.setText("已下载");
			holder.price.setVisibility(View.VISIBLE);
			holder.price.setBackgroundResource(R.drawable.price_tag_yellow);
		}

		ImageLoader.getInstance().displayImage(imgURl, holder.img, options);
		return convertView;
	}

	private class Holder {
		TextView tv = null;
		ImageView img = null;
		ProgressBar progressBar;
		TextView price;
	}

}
