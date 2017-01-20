package com.nhaarman.listviewanimations.example;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;

import java.util.ArrayList;

public class MyListActivity extends Activity {

	protected ListView mListView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mListView = new ListView(this);
		setContentView(mListView);
		MyListAdapter mAdapter = new MyListAdapter(this, getItems());
		mListView.setAdapter(mAdapter);
 		mListView.setDivider(null);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i = new Intent();
				if(position == 0){
					i = new Intent(MyListActivity.this,AnimateAdditionActivity.class);
				}else if(position == 1){
					i = new Intent(MyListActivity.this,AnimateDismissActivity.class);
				}else if(position == 2){
					i = new Intent(MyListActivity.this,SwipeDismissActivity.class);
				}
				startActivity(i);
			}
		});
	}

	public ListView getListView() {
		return mListView;
	}

	protected ArrayAdapter<Integer> createListAdapter() {
		return new MyListAdapter(this, getItems());
	}

	public static ArrayList<Integer> getItems() {
		ArrayList<Integer> items = new ArrayList<Integer>();
		for (int i = 0; i < 1000; i++) {
			items.add(i);
		}
		return items;
	}

	private class MyListAdapter extends ArrayAdapter<Integer> {

		private final Context mContext;

		public MyListAdapter(final Context context, final ArrayList<Integer> items) {
			super(items);
			mContext = context;
		}

		@Override
		public long getItemId(final int position) {
			return getItem(position).hashCode();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (tv == null) {
				tv = (TextView) new TextView(MyListActivity.this);
			}
			tv.setText("This is row number " + getItem(position));
			return tv;
		}
	}
}
