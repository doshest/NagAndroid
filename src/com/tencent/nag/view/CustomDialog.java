package com.tencent.nag.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.facedemo.R;

public class CustomDialog extends Dialog {  
	
	
  
    public CustomDialog(Context context) {  
        super(context);  
    }  
  
    public CustomDialog(Context context, int theme) {  
        super(context, theme);  
    }  
  
    public static class Builder {  
        private Context context;  
        private String title;  
        private String positiveButtonText;  
        private String negativeButtonText;  
        private DialogInterface.OnClickListener positiveButtonClickListener;  
        private DialogInterface.OnClickListener negativeButtonClickListener; 
        private TextView mTitle;
    	private Button mPossitiveBtn,mNagtiveBtn;
    	private EditText mInput;
  
        public Builder(Context context) {  
            this.context = context;  
        }  
  
    
  
        /** 
         * Set the Dialog message from resource 
         *  
         * @param title 
         * @return 
         */  
     
  
        /** 
         * Set the Dialog title from resource 
         *  
         * @param title 
         * @return 
         */  
        public Builder setTitle(int title) {  
            this.title = (String) context.getText(title);  
            return this;  
        }  
  
        /** 
         * Set the Dialog title from String 
         *  
         * @param title 
         * @return 
         */  
  
        public Builder setTitle(String title) {  
            this.title = title;  
            return this;  
        }  
  
       
  
        /** 
         * Set the positive button resource and it's listener 
         *  
         * @param positiveButtonText 
         * @return 
         */  
        public Builder setPositiveButton(int positiveButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.positiveButtonText = (String) context  
                    .getText(positiveButtonText);  
            this.positiveButtonClickListener = listener;  
            return this;  
        }  
  
        public Builder setPositiveButton(String positiveButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.positiveButtonText = positiveButtonText;  
            this.positiveButtonClickListener = listener;  
            return this;  
        }  
  
        public Builder setNegativeButton(int negativeButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.negativeButtonText = (String) context  
                    .getText(negativeButtonText);  
            this.negativeButtonClickListener = listener;  
            return this;  
        }  
  
        public Builder setNegativeButton(String negativeButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.negativeButtonText = negativeButtonText;  
            this.negativeButtonClickListener = listener;  
            return this;  
        }  
  
        public CustomDialog create() {  
            LayoutInflater inflater = (LayoutInflater) context  
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            // instantiate the dialog with the custom Theme  
            final CustomDialog dialog = new CustomDialog(context,R.style.Dialog);  
            View layout = inflater.inflate(R.layout.custom_dialog, null);  
            dialog.addContentView(layout, new LayoutParams(  
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));  
            
            // set the dialog title  
           mTitle = ((TextView) layout.findViewById(R.id.dialog_title)); 
           mTitle.setText(title);
           mInput = (EditText) layout.findViewById(R.id.dialog_room_input);
            // set the confirm button  
            if (positiveButtonText != null) {  
               mPossitiveBtn = ((Button) layout.findViewById(R.id.dialog_Send_btn)) ; 
               mPossitiveBtn.setText(positiveButtonText); 
               
                if (positiveButtonClickListener != null) {  
                    mPossitiveBtn 
                            .setOnClickListener(new View.OnClickListener() {  
                                public void onClick(View v) {  
                                    positiveButtonClickListener.onClick(dialog,  
                                            DialogInterface.BUTTON_POSITIVE);  
                                }  
                            });  
                }  
            } else {  
                // if no confirm button just set the visibility to GONE  
               mPossitiveBtn.setVisibility(  
                        View.GONE);  
            }  
            // set the cancel button  
            if (negativeButtonText != null) {  
               mNagtiveBtn =  ((Button) layout.findViewById(R.id.dialog_Cancle_btn)) ; 
               mNagtiveBtn.setText(negativeButtonText);  
                if (negativeButtonClickListener != null) {  
                    mNagtiveBtn 
                            .setOnClickListener(new View.OnClickListener() {  
                                public void onClick(View v) {  
                                    negativeButtonClickListener.onClick(dialog,  
                                            DialogInterface.BUTTON_NEGATIVE);  
                                }  
                            });  
                }  
            } else {  
                // if no confirm button just set the visibility to GONE  
               mNagtiveBtn.setVisibility(  
                        View.GONE);  
            }  
           
            dialog.setContentView(layout);  
            return dialog;  
        } 
        
        public String getRoomId(){
        	return mInput.getText().toString();
        }
        
        public void setErroMsg(){
        	mInput.setText("请输入正确房间号");
        	mInput.setTextColor(Color.RED);
        }
        
        
    }  
  
}  