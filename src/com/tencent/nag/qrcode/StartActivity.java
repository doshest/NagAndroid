package com.tencent.nag.qrcode;

import com.example.facedemo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends Activity {
	private TextView result ;
	private Button start;
	public static int QRCODE_RES =  0x124;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);
		initView();
	}
	
	private void initView(){
		start = (Button) findViewById(R.id.open_carema);
		result = (TextView) findViewById(R.id.result);
		start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(StartActivity.this , CaptureActivity.class);
				startActivityForResult(intent, QRCODE_RES);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode == RESULT_OK&&requestCode == QRCODE_RES){
			
			DecodeResult res = (DecodeResult) data.getSerializableExtra("result");
			String content = res.getText();
			int type = res.getFormat();
			result.setText("ɨ�������ͣ�"+type+"�����ݣ�"+content );
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
}
