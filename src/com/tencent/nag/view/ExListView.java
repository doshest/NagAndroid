package com.tencent.nag.view;

import com.tencent.nag.utils.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ExListView extends ListView{
	
	private boolean canRebound = false;

	public boolean isCanRebound() {
		return canRebound;
	}
	public void setCanRebound(boolean canRebound) {
		this.canRebound = canRebound;
	}
	public ExListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public ExListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}	public ExListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	@Override
	 protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,

			 int scrollY, int scrollRangeX, int scrollRangeY,

			 int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
			if(canRebound){

			 return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,

			 scrollRangeX, scrollRangeY, maxOverScrollX,

			 100, isTouchEvent);
			}
			return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, 
					maxOverScrollX, maxOverScrollY, isTouchEvent);

	} 
	
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		// TODO Auto-generated method stub
//		//System.out.print(Utils.getScreenHeigth(getContext())-Utils.dip2px(getContext(), 80));
//		super.onMeasure(widthMeasureSpec,Utils.getScreenHeigth(getContext())-Utils.dip2px(getContext(), 80) );
//	}

	



}
