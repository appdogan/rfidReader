package com.example.pc2.rfidreader;

import android.content.Context;
import android.graphics.Color;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.pc2.rfidreader.Model.TagInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ListView interface display
 * 
 * @author sam
 * 
 */
@SuppressWarnings("deprecation")
public class DataAdapter extends BaseAdapter {

	private List<TagInfo> dataList = null;
	private LayoutInflater mLayoutInflater = null;
	private List<String> statusList = new ArrayList<String>();

	public DataAdapter(List<TagInfo> dataList, Context mContext,
                       List<String> statusList) {
		this.dataList = dataList;
		this.statusList = statusList;
		mLayoutInflater = LayoutInflater.from(mContext);
	}

	public void update(List<TagInfo> list) {
		dataList = list;
		//orderDataList();
		this.notifyDataSetChanged();
	}

	private void orderDataList() {
		Collections.sort(dataList, new Comparator<TagInfo>() {
			@Override
			public int compare(TagInfo tag1, TagInfo tag2) {
				if (tag1.getStatus().equals(statusList.get(0))) {
					if (!tag2.getStatus().equals(statusList.get(0))) {
						return -1;
					}
				} else if (tag1.getStatus().equals(statusList.get(1))) {
					if (tag2.getStatus().equals(statusList.get(0))) {
						return 1;
					} else if (tag2.getStatus().equals(statusList.get(2))) {
						return -1;
					}
				} else if (tag1.getStatus().equals(statusList.get(2))) {
					if (!tag2.getStatus().equals(statusList.get(2))) {
						return 1;
					}
				}
				return 0;
			}
		});
		/*
		ArrayList<TagInfo> waitingTags = new ArrayList<TagInfo>();
		ArrayList<TagInfo> undefinedTags = new ArrayList<TagInfo>();
		ArrayList<TagInfo> scannedTags = new ArrayList<TagInfo>();
		for (TagInfo tag : dataList) {
			if (tag.getStatus().equals(statusList.get(0))) {        // Waiting
				waitingTags.add(tag);
			} else if (tag.getStatus().equals(statusList.get(1))) { // Scanned
				scannedTags.add(tag);
			} else if (tag.getStatus().equals(statusList.get(2))) { // Undefined
				undefinedTags.add(tag);
			}
		}
		dataList.clear();
		dataList.addAll(waitingTags);
		dataList.addAll(undefinedTags);
		dataList.addAll(scannedTags);
		*/
	}

	public int getCount() {
		return dataList.size();
//		return dataList.size() > 250 ? 250 : dataList.size(); // dataList.size();
	}

	public Object getItem(int position) {
		return dataList.get(position);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public View getView(int position, View convertView, ViewGroup arg2) {
		TagInfo tag = dataList.get(position);
		ViewHolder vHolder = null;
		if (convertView == null) {
			vHolder = new ViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.data_item, null);
			vHolder.tvTagId = (TextView) convertView.findViewById(R.id.tagid);
			vHolder.tvStatus = (TextView) convertView.findViewById(R.id.status);
			vHolder.deleteView = (Button) convertView.findViewById(R.id.copy);

			convertView.setTag(vHolder);
		} else {
			vHolder = (ViewHolder) convertView.getTag();
		}
		vHolder.tvTagId.setText(tag.getTagUid());
		vHolder.tvStatus.setText(tag.getStatus());
		vHolder.deleteView.setVisibility(View.GONE);
		// status[0] not scan status[1] OK status[2] Undefine
		if (tag.getStatus().equals(statusList.get(1))) {
			convertView.setBackgroundColor(Color.GREEN);
		} else if (tag.getStatus().equals(statusList.get(2))) {
			convertView.setBackgroundColor(Color.RED);
		} else if (tag.getStatus().equals(statusList.get(0))) {
			convertView.setBackgroundColor(position % 2 == 0 ? Color.WHITE : Color.parseColor("#EEEEEE"));
		}
		return convertView;
	}

	/**
	 *  cop data
	 */
	public void copy(String content, Context context) {
		try {
			ClipboardManager cmb = (ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			cmb.setText(content.trim());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private class ViewHolder {
		private TextView tvTagId;
		private TextView tvStatus;
		private Button deleteView;
	}

}
