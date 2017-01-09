package com.serenegiant.audiovideosample;
/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: MainActivity.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;

public class LivePhotosActivity extends Activity {
	GridView mGridView;
	LivePhotoAdapter mLivePhotoAdapter;
	SelectPicPopupWindow dddd = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.livephoto_mainlayout);
		mGridView = (GridView) this.findViewById(R.id.gridview_photos);
		mLivePhotoAdapter = new LivePhotoAdapter(this);
		mGridView.setAdapter(mLivePhotoAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, final int position, long id) {
				    SelectPicPopupWindow menuWindow = new SelectPicPopupWindow(LivePhotosActivity.this,
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								dddd.dismiss();
								if(v.getId() ==  R.id.btn_take_photo){
									Intent i=new Intent();
									ComponentName com= new ComponentName( "com.wass08.vlcsimpleplayer" , "com.wass08.vlcsimpleplayer.FullscreenVlcPlayer");  
									i.putExtra("url", mLivePhotoAdapter.getVideoPath(position));
									i.setComponent(com);  
									startActivity(i);  
									 
								}
							}
						});
				    dddd = menuWindow;
				menuWindow.showAtLocation(LivePhotosActivity.this.findViewById(R.id.container),
						Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			}
		});
	}

	public void onClick(View v) {
		if (v.getId() == R.id.backimage) {
			this.finish();
		}
	}

	public class SelectPicPopupWindow extends PopupWindow {

		private Button btn_take_photo, btn_pick_photo, btn_cancel;
		private View mMenuView;

		public SelectPicPopupWindow(Activity context, OnClickListener itemsOnClick) {
			super(context);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mMenuView = inflater.inflate(R.layout.livephoto_pop_menu, null);
			btn_take_photo = (Button) mMenuView.findViewById(R.id.btn_take_photo);
			btn_pick_photo = (Button) mMenuView.findViewById(R.id.btn_pick_photo);
			btn_cancel = (Button) mMenuView.findViewById(R.id.btn_cancel);
			btn_cancel.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					dismiss();
				}
			});
			// 设置按钮监听
			btn_pick_photo.setOnClickListener(itemsOnClick);
			btn_take_photo.setOnClickListener(itemsOnClick);
			// 设置SelectPicPopupWindow的View
			this.setContentView(mMenuView);
			// 设置SelectPicPopupWindow弹出窗体的宽
			this.setWidth(LayoutParams.FILL_PARENT);
			// 设置SelectPicPopupWindow弹出窗体的高
			this.setHeight(600);
			// 设置SelectPicPopupWindow弹出窗体可点击
			this.setFocusable(true);
			// 设置SelectPicPopupWindow弹出窗体动画效果
			this.setOutsideTouchable(false);
		}

	}
}
