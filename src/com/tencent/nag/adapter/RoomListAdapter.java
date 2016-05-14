package com.tencent.nag.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.facedemo.R;

public class RoomListAdapter extends BaseAdapter{
	
	private List<RoomModel> mListData;
	private Context mContext;
	
	public RoomListAdapter(Context context,List<RoomModel> data){
		mListData = data;
		mContext = context;
	}
	
	   @Override
       public int getCount() {
           return mListData.size();
       }

       @Override
       public RoomModel getItem(int position) {
           return mListData.get(position);
       }

       @Override
       public long getItemId(int position) {
           return position;
       }

//       @Override
//       public int getViewTypeCount() {
//           // menu type count
//           return 3;
//       }

//       @Override
//       public int getItemViewType(int position) {
//           // current menu type
//           return position % 3;
//       }

       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
           if (convertView == null) {
               convertView = View.inflate(mContext,
                       R.layout.item_list_app, null);
               new ViewHolder(convertView);
           }
           ViewHolder holder = (ViewHolder) convertView.getTag();
           RoomModel item = getItem(position);
           holder.iv_icon.setImageDrawable(item.roomDrawable);
           holder.tv_name.setText(item.roomName );
           holder.tv_id.setText(" 房间号: "+item.roomId);
           return convertView;
       }

       class ViewHolder {
           ImageView iv_icon;
           TextView tv_name;
           TextView tv_id;

           public ViewHolder(View view) {
               iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
               tv_name = (TextView) view.findViewById(R.id.tv_name);
               tv_id = (TextView) view.findViewById(R.id.tv_id);
               view.setTag(this);
           }
       }
}
