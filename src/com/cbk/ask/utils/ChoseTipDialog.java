package com.cbk.ask.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cbk.ask.R;

/**
 * Created by sunyc
 */
public class ChoseTipDialog extends Dialog implements android.view.View.OnClickListener{

	public interface ChoseTipDialogListener {
		void SetOnClick(Boolean ok,int type);
	}

	private View view;
	private RelativeLayout rl_video_call;
	private RelativeLayout rl_voice_call;
	
	private ChoseTipDialogListener dialogListener;
	private boolean isNeedSend = true;

	// 构造方法
	public ChoseTipDialog(Context context,ChoseTipDialogListener tipDialogListener) {
		super(context,R.style.iphone_progress_dialog);
		initView(context);
		this.dialogListener = tipDialogListener;
	}
	// 初始化dialog界面
	private void initView(Context context) {
		view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_chose,
				null);
		rl_video_call = (RelativeLayout) view.findViewById(R.id.rl_video_call);
		rl_voice_call = (RelativeLayout) view.findViewById(R.id.rl_voice_call);
		
		rl_video_call.setOnClickListener(this);
		rl_voice_call.setOnClickListener(this);
	}
	
	

	public void showDialog() {
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(view);
		this.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		this.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				if(isNeedSend)
					dialogListener.SetOnClick(false,0);
			}
		});
		super.show();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.rl_video_call:
			isNeedSend = false;
			this.dialogListener.SetOnClick(true,0);
			this.dismiss();
			break;
		case R.id.rl_voice_call:
			isNeedSend = false;
			this.dialogListener.SetOnClick(true,1);
			this.dismiss();
			break;
		default:
			break;
		}
	}

}
