package com.tencent.nag.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataHelper {
	 // 数据库名称
    private static String DB_NAME = "nag_chat.db";
   
    // 数据库版本
    private static int DB_VERSION = 2;
    private SQLiteDatabase db;
    private SqliteHelper dbHelper;

    public DataHelper(Context context) {
          dbHelper = new SqliteHelper(context, DB_NAME, null, DB_VERSION );
          db = dbHelper.getWritableDatabase();
    }

    public void Close() {
          db.close();
          dbHelper.close();
    }

    public ChatDataModel getChatDataModelById(String id){
    	ChatDataModel model = null;
    	Cursor cursor = db.query(SqliteHelper.TB_NAME, null, 
    			ChatDataModel.ID+"=?", new String[]{id}, null, null, null);
    	if(cursor !=null && cursor.moveToFirst()){
    		model = new ChatDataModel();
    		model.setRoomId(cursor.getString(0));
    		model.setRoomName(cursor.getString(1));
    		model.setChatMsgEntryJsonString(cursor.getString(2));
    	}
    	cursor.close();
    	return model;
    }
    public List<ChatDataModel> getchatDataList() {
         List<ChatDataModel> chatDataList = new ArrayList<ChatDataModel>();
         Cursor cursor = db.query(SqliteHelper.TB_NAME, null, null , null, null,
                   null, null);
         cursor.moveToFirst();
          while (!cursor.isAfterLast() && (cursor.getString(1) != null )) {
        	  System.out.println(" nums -----");
             ChatDataModel chatData = new ChatDataModel();
             chatData.setRoomId(cursor.getString(0));
             chatData.setRoomName(cursor.getString(1));
             chatData.setChatMsgEntryJsonString(cursor.getString(2));
             chatDataList.add(chatData);
             cursor.moveToNext();
         }
         cursor.close();
          return chatDataList;
    }

    // 判断chatDatas表中的是否包含某个chatDataID的记录
    public Boolean HaveChatDataModel(String chatDataId) {
         Boolean b = false;
         Cursor cursor = db.query(SqliteHelper. TB_NAME, null, ChatDataModel.ID
                  + "=?", new String[]{chatDataId}, null, null, null );
         b = cursor.moveToFirst();
         Log. e("HaveChatDataModel", b.toString());
         cursor.close();
          return b;
    }

    

    // 更新chatDatas表的记录
    public int UpdateChatDataModel(ChatDataModel chatData) {
         ContentValues values = new ContentValues();
         values.put(ChatDataModel.ID, chatData.getRoomId());
         values.put(ChatDataModel.NAME, chatData.getRoomName());
         values.put(ChatDataModel.DATA, chatData.getChatMsgEntryJsonString());
       
          int id = db.update(SqliteHelper.TB_NAME, values, ChatDataModel.ID + "="
                  + chatData.getRoomId(), null);
         Log. e("UpdateChatDataModel", id + "");
          return id;
    }

    // 添加chatDatas表的记录
    public Long SaveChatDataModel(ChatDataModel chatData) {
         ContentValues values = new ContentValues();
         values.put(ChatDataModel.ID, chatData.getRoomId());
         values.put(ChatDataModel.NAME, chatData.getRoomName());
         values.put(ChatDataModel.DATA, chatData.getChatMsgEntryJsonString());
         Long uid = db.insert(SqliteHelper.TB_NAME, ChatDataModel.ID, values);
         Log. e("SaveChatDataModel", uid + "");
          return uid;
    }
    
    // 删除chatDatas表的记录
    public int delChatDataModel(String chatDataId) {
          int id = db.delete(SqliteHelper.TB_NAME,
                  ChatDataModel.ID + "=?", new String[]{chatDataId});
         Log. e("DelChatDataModel", id + "");
          return id;
    }
    
     
}
