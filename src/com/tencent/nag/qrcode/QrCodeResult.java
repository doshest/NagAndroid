package com.tencent.nag.qrcode;

import java.io.Serializable;
import java.util.List;

public class QrCodeResult implements Serializable {

    private static final long serialVersionUID = -6540713902239285954L;

    private String action;
    private String url;
    private String des;
    private String qr;
    private int unAlert;// �Ƿ񵯳���ʾ    1��ʾ������   0��ʾ����
    


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    

    public int getUnAlert() {
        return unAlert;
    }

    public void setUnAlert(int unAlert) {
        this.unAlert = unAlert;
    }


    @Override
    public String toString() {
        return "QrCodeResult [action=" + action + ", url=" + url + ", des=" + des + ", qr=" + qr
                + ", unAlert=" + unAlert + "]";
    }

}