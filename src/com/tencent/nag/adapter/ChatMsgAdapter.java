package com.tencent.nag.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.facedemo.R;
import com.google.gson.Gson;
import com.tencent.nag.ChatMsgEntity;
import com.tencent.nag.FaceRelativeLayout;
import com.tencent.nag.db.ChatDataModel;
import com.tencent.nag.db.DataHelper;
import com.tencent.nag.net.HttpUtil;
import com.tencent.nag.utils.FaceConversionUtil;
import com.tencent.nag.utils.Utils;
import com.tencent.nag.view.RetrivePopupWindow;


@SuppressLint("NewApi") public class ChatMsgAdapter extends BaseAdapter {

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	private List<ChatMsgEntity> coll;
	FaceRelativeLayout facePanel;
	private LayoutInflater mInflater;
	private Context context;
	private String roomId,roomName;
	private DataHelper helper;
	private ImageView loadImg;
	private int delPos = 0;
	public ChatMsgAdapter(Context context, List<ChatMsgEntity> coll,FaceRelativeLayout facePanel,String id,String name
			, DataHelper h,ImageView v) {
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.facePanel = facePanel;
		this.roomId = id;
		this.roomName = name;
		this.helper = h;
		this.loadImg = v;
	}

	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		ChatMsgEntity entity = coll.get(position);

		if (entity.getMsgType()) {
			return IMsgViewType.IMVT_COM_MSG;
		} else {
			return IMsgViewType.IMVT_TO_MSG;
		}

	}

	public int getViewTypeCount() {
		return 2;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		final ChatMsgEntity entity = coll.get(position);
		boolean isComMsg = entity.getMsgType();

		ViewHolder viewHolder = null;
		if (convertView == null) {
			if (isComMsg) {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_left, null);
			} else {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_right, null);
			}

			viewHolder = new ViewHolder();
			
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.tvSendTime.setText(entity.getDate());
		SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context, entity.getText());
		viewHolder.tvContent.setText(spannableString);
		
		viewHolder.tvContent.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				final RetrivePopupWindow p = new RetrivePopupWindow(context);
				p.showAsDropDown(arg0, -150, -100,Gravity.TOP);
				
				p.setItemClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						System.out.println("gone ---retrive");
						String msgId = entity.getMsgId();
						delPos = position;
						new RetriveTask().execute(msgId);
						p.dismiss();
					}
				});
				return false;
			}
		});
		
		convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				facePanel.hideFaceView();
				Utils.hideInput(context, v);
				
			}
		});
		
		return convertView;
	}

	class ViewHolder {
		public TextView tvSendTime;
		public TextView tvContent;
		public boolean isComMsg = false;
	}
	
	class RetriveTask extends AsyncTask<String,Integer,String>{
		AnimationDrawable aniDraw;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			loadImg.setVisibility(View.VISIBLE);
			 aniDraw = (AnimationDrawable) loadImg.getDrawable();
			 aniDraw.start();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			System.out.println("del msg id "+arg0[0]);
			String res ="";
			try {
				res = HttpUtil.getRequest(HttpUtil.BASE_URL_RETRIVE+roomId+"/"+arg0[0]);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
				System.out.println(e1.toString());
			}
			System.out.println("res "+res);
			return res;
		}
		
		@Override
		protected void onPostExecute(String res) {
			aniDraw.stop();
			loadImg.setVisibility(View.GONE);
			// TODO Auto-generated method stub
			if(TextUtils.isEmpty(res)){
				Toast.makeText(context, "撤回失败", Toast.LENGTH_SHORT).show();
			}else if(res.equals("0")){
				coll.remove(delPos);
				notifyDataSetChanged();
				updateDatabase();
			
				
			}else if(res.equals("-1")){
				Toast.makeText(context, "房间已失效", Toast.LENGTH_SHORT).show();;
			}
			
			super.onPostExecute(res);
		}
		
	}
	
	void updateDatabase(){
		Gson g = new Gson();
		String data = g.toJson(coll);
		System.out.println(data);
		
		
		ChatDataModel chatData = new ChatDataModel();
		chatData.setRoomId(roomId);
		chatData.setRoomName(roomName);
		chatData.setChatMsgEntryJsonString(data);
		if(helper.HaveChatDataModel(roomId)){
			helper.UpdateChatDataModel(chatData);
		}else{
			helper.SaveChatDataModel(chatData);
		}
	}
}
