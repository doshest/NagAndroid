
package com.tencent.nag;

public class ChatMsgEntity {

    private String msgId;

    private String date;

    private String text;

    private boolean isComMeg = true;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String id) {
        this.msgId = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getMsgType() {
        return isComMeg;
    }

    public void setMsgType(boolean isComMsg) {
    	isComMeg = isComMsg;
    }

    public ChatMsgEntity() {
    }

    public ChatMsgEntity(String id, String date, String text, boolean isComMsg) {
        super();
        this.msgId = id;
        this.date = date;
        this.text = text;
        this.isComMeg = isComMsg;
    }

}
