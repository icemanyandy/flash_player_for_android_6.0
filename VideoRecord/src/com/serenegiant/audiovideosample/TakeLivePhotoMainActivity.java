package com.serenegiant.audiovideosample;
/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: TakeLivePhotoMainActivity.java
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

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class TakeLivePhotoMainActivity extends Activity {
	 private static final int CAMERA_REQUEST_CODE = 1;
	 private static final int  WRITE_EXTERNAL_STORAGE = 2;
	 @Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 if(Build.VERSION.SDK_INT >= 23) {
			 requestPermission();
		 }

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new CameraFragment()).commit();
		}
	}

	private void requestPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			// 第一次请求权限时，用户如果拒绝，下一次请求shouldShowRequestPermissionRationale()返回true
			// 向用户解释为什么需要这个权限
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

								//申请相机权限
								ActivityCompat.requestPermissions(TakeLivePhotoMainActivity.this,
										new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

			} else {
				//申请相机权限
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
			}
		}

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			// 第一次请求权限时，用户如果拒绝，下一次请求shouldShowRequestPermissionRationale()返回true
			// 向用户解释为什么需要这个权限
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

								ActivityCompat.requestPermissions(TakeLivePhotoMainActivity.this,
										new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);

			} else {
				//申请相机权限
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
			}
		}

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
				!= PackageManager.PERMISSION_GRANTED) {
			// 第一次请求权限时，用户如果拒绝，下一次请求shouldShowRequestPermissionRationale()返回true
			// 向用户解释为什么需要这个权限
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)) {

				ActivityCompat.requestPermissions(TakeLivePhotoMainActivity.this,
						new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, WRITE_EXTERNAL_STORAGE);

			} else {
				//申请相机权限
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, WRITE_EXTERNAL_STORAGE);
			}
		}

	}

}
