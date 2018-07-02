package com.cbk.ask.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cbk.ask.R;
import com.cbk.ask.ui.SplashActivity;

public class FxService extends Service 
{

	//定义浮动窗口布局
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
	WindowManager mWindowManager;
	float mStartX = 0;
	float mStartY = 0;
	private static final String TAG = "FxService";
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		Log.i(TAG, "oncreat");
		createFloatView();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	private void createFloatView()
	{
		wmParams = new WindowManager.LayoutParams();
		//获取WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		//设置window type
		wmParams.type = LayoutParams.TYPE_PHONE; 
		//设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888; 
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = 
//          LayoutParams.FLAG_NOT_TOUCH_MODAL |
          LayoutParams.FLAG_NOT_FOCUSABLE
//          LayoutParams.FLAG_NOT_TOUCHABLE
          ;
        
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM; 
        
        // 以屏幕左上角为原点，设置x、y初始值
        wmParams.x = 0;
        wmParams.y = 0;

        /*// 设置悬浮窗口长宽数据
        wmParams.width = 200;
        wmParams.height = 80;*/
        
        //设置悬浮窗口长宽数据  
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        Log.i(TAG, "Width/2--->" + mFloatLayout.getMeasuredWidth()/2);
        Log.i(TAG, "Height/2--->" + mFloatLayout.getMeasuredHeight()/2);
        //设置监听浮动窗口的触摸移动
        mFloatLayout.setOnTouchListener(new OnTouchListener() 
        {
        	private float downX = 0;
			private float downY = 0;
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					downX = event.getRawX();
					downY = event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					//加一个值,更方便点击
					mStartX = event.getRawX();
					mStartY = event.getRawY();
					if(Math.abs(mStartX - downX) > 40 && Math.abs(mStartY - downY) > 40){
						wmParams.x = (int) (mStartX - mFloatLayout
								.getMeasuredWidth() / 2);
						wmParams.y = (int) (mStartY - mFloatLayout
								.getMeasuredHeight() / 2);
						mWindowManager.updateViewLayout(mFloatLayout, wmParams);
					}
					break;
				case MotionEvent.ACTION_UP:
					if(Math.abs(mStartX - downX) < 40 && Math.abs(mStartY - downY) < 40){
						Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
						getApplicationContext().startActivity(intent);
					}
					break;
				}
				return true;
			}
		});	
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		if(mFloatLayout != null)
		{
			mWindowManager.removeView(mFloatLayout);
		}
	}
	
}
