package com.tencent.nag;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.facedemo.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.nag.adapter.ChatMsgAdapter;
import com.tencent.nag.db.ChatDataModel;
import com.tencent.nag.db.DataHelper;
import com.tencent.nag.net.HttpUtil;
import com.tencent.nag.utils.Utils;
import com.tencent.nag.view.ExListView;


public class ChatActivity extends Activity implements OnClickListener {

	private Button mBtnSend;
	 private String mChatRoomId = "0001";
	 
	 private String mChatRoomName = "Nag";


	private EditText mEditTextContent;

	private ExListView mListView;
	private View container;

	private ChatMsgAdapter mAdapter;
	private RelativeLayout mTitleBar;
	FaceRelativeLayout chatPanel;
	private RelativeLayout mTitleBarRelativeLayout ;
	private TextView title;
	private View back;
	
	private ImageView loadingImg;
	

	private List<ChatMsgEntity> mDataArrays;// = new ArrayList<ChatMsgEntity>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_chat);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		initView();
		initData();
	}

	public void initView() {
		mTitleBarRelativeLayout = (RelativeLayout) findViewById(R.id.title_bar_layout);
		loadingImg = (ImageView) findViewById(R.id.loading_img);
		
		mListView = (ExListView) findViewById(R.id.listview);
		title = (TextView) findViewById(R.id.title_name);
		chatPanel = (FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout);
		back = findViewById(R.id.title_bar_left_button);
		
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if(view.getCount()<6){
					return;
				}
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {  
		            // 判断是否滚动到底部  
		            if (view.getLastVisiblePosition() == view.getCount() - 1) {  
		            	if(chatPanel.hideFaceView()){
		            		return;
		            	}
		                Utils.showKeyboard(ChatActivity.this, chatPanel.getEditView());
		            }else if(scrollState == OnScrollListener.SCROLL_STATE_FLING){
		            	if(chatPanel.hideFaceView()){
		            		return;
		            	}
		            	Utils.hideInput(ChatActivity.this, view);
		            }
		            
		            
		        }  
				
			}
			
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
			//	Utils.hideInput(ChatActivity.this, arg0);
				
			}
		});
		
		back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
	

		
		mBtnSend = (Button) findViewById(R.id.btn_send);
		mTitleBar = (RelativeLayout) findViewById(R.id.chat_title_bar);
		mTitleBar.bringToFront();
		mBtnSend.setOnClickListener(this);
		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
		container =  findViewById(R.id.list_container);
		container.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Utils.hideInput(ChatActivity.this, arg0);
				chatPanel.hideFaceView();
			}
		});
		
		

	}

	private String[] msgArray = new String[] {};

	private String[] dataArray = new String[] {};

	private final static int COUNT = 0;
	private DataHelper helper;
	public void initData() {
		Intent intent = getIntent();
		mChatRoomId = intent.getStringExtra(MainActivity.ROOM_ID_LABEL);
		mChatRoomName = intent.getStringExtra(MainActivity.ROOM_NAME_LABEL);
		if(TextUtils.isEmpty(mChatRoomName)){
			mChatRoomName = "Nag";
		}
		if(!TextUtils.isEmpty(mChatRoomId)){
			title.setText(mChatRoomName);
		}
	
		 helper = new DataHelper(this);
		ChatDataModel model = helper.getChatDataModelById(mChatRoomId);
		if(model != null){
			Gson g = new Gson();
			mDataArrays = g.fromJson(model.getChatMsgEntryJsonString(), 
					new TypeToken<List<ChatMsgEntity>>() {  
            }.getType());
			
			
		}else{
			mDataArrays = new ArrayList<ChatMsgEntity>();
		}
		if(mDataArrays ==null){
			mDataArrays = new ArrayList<ChatMsgEntity>();
		}
		mAdapter = new ChatMsgAdapter(this, mDataArrays,chatPanel,mChatRoomId
				,mChatRoomName,helper,loadingImg);
		mListView.setAdapter(mAdapter);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:
			send();
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& chatPanel.hideFaceView()
				) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void send() {
		String contString = mEditTextContent.getText().toString();
		if (contString.length() > 0) {
			ChatMsgEntity entity = new ChatMsgEntity();
			entity.setDate(getDate());
			entity.setMsgType(false);
			entity.setText(contString);
			entity.setMsgId(Utils.getPhoneId(this)+System.currentTimeMillis());

			mDataArrays.add(entity);
			mAdapter.notifyDataSetChanged();
			mEditTextContent.setText("");
			mListView.setSelection(mListView.getCount() - 1);
		}
		if(mDataArrays.size()>5){
			mListView.setCanRebound(true);
		}else{
			mListView.setCanRebound(false);
		}
		Gson g = new Gson();
		String data = g.toJson(mDataArrays);
		System.out.println(data);
		
		
		ChatDataModel chatData = new ChatDataModel();
		chatData.setRoomId(mChatRoomId);
		chatData.setRoomName(mChatRoomName);
		chatData.setChatMsgEntryJsonString(data);
		if(helper.HaveChatDataModel(mChatRoomId)){
			helper.UpdateChatDataModel(chatData);
		}else{
			helper.SaveChatDataModel(chatData);
		}
		ChatMsgEntity entry = mDataArrays.get(mDataArrays.size()-1);
		String msg =entry.getMsgId()+"-"+entry.getDate() +"-"+entry.getText();
		
		msg = URLEncoder.encode(msg);
		msg = HttpUtil.BASE_URL+mChatRoomId+"/"+msg;
		
		System.out.println("send---->"+msg);
		
		try {
			//s = HttpUtil.getRequest();
			new SendMSG().execute(msg);
			//System.out.println("dddd   "+s);
		} catch (Exception e) {
			System.out.println(e.toString()+" error");
			e.printStackTrace();
		}
	
	}

	private String getDate() {
		Calendar c = Calendar.getInstance();

		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH));
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH) + 1);
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		String mins = String.valueOf(c.get(Calendar.MINUTE));

		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":"
				+ mins);

		return sbBuffer.toString();
	}
	
	@Override
	public void onBackPressed() {
		overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
		super.onBackPressed();
		enterMainActivity();
	}
	
	private void enterMainActivity(){
		Intent i = new Intent(this,MainActivity.class);
		finish();
		startActivity(i);
		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
	}
	
	
	class SendMSG extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String res = "";
			try {
				res = HttpUtil.getRequest(params[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println(e.toString());
			}
			return res;
		}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			System.out.println("ddddd rescode "+result);
			if(result.equals("-1")){
				Toast.makeText(ChatActivity.this, "房间已失效", Toast.LENGTH_LONG).show();
			}else if(result.equals("0")){
				//System.out.println("send OK");
			}else{
			//	System.out.println("send fail");
				Toast.makeText(ChatActivity.this, "消息发送失败", Toast.LENGTH_LONG).show();
			}
			super.onPostExecute(result);
		}
		
	}
}