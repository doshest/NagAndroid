package com.tencent.nag.qrcode;

import java.io.Serializable;

/**
 * ���������
 * */
public class DecodeResult implements Serializable {
    
	private static final long serialVersionUID = -8651548619481830908L;
	public static final int RESULT_FORMATE_QRCODE = 1;
    public static final int RESULT_FORMATE_BARCODE = 2;

    private String mText;
    
    private int mResultFormate;
    
    public DecodeResult(String text, int format) {
        this.mText = text;
        this.mResultFormate = format;
    }
    
    public String getText() {
        return mText;
    }
    
    public int getFormat() {
        return mResultFormate;
    }
}
