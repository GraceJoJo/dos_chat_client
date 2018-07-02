package com.cbk.ask.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cbk.ask.R;

/**
 * Created by sunyc
 */
public class TipDialog extends Dialog implements android.view.View.OnClickListener{

	public interface TipDialogListener {
		void SetOnClick(View view);
	}

	private View view;
	private TextView tv_title_dialog;
	private TextView tv_content_dialog;
	private Button btn_ok;
	private Button btn_cancel;
	
	private TipDialogListener dialogListener;

	// 构造方法
	public TipDialog(Context context,TipDialogListener tipDialogListener) {
		super(context);
		initView(context);
		this.dialogListener = tipDialogListener;
	}

	public TipDialog(Context context, int theme) {
		super(context, theme);
		initView(context);
	}

	protected TipDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		initView(context);
	}

	// 初始化dialog界面
	private void initView(Context context) {
		view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_tip,
				null);
		tv_title_dialog = (TextView) view.findViewById(R.id.tv_title_dialog);
		tv_content_dialog = (TextView) view
				.findViewById(R.id.tv_content_dialog);
		btn_ok = (Button) view.findViewById(R.id.btn_dialog_ok);
		btn_cancel = (Button) view.findViewById(R.id.btn_dialog_cancel);
		
		btn_ok.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
	}

	public Button getBtn_ok() {
		return btn_ok;
	}

	public Button getBtn_cancel() {
		return btn_cancel;
	}

	public void SetContentTextSize(int size) {
		tv_content_dialog.setTextSize(size);
	}

	public void show(String title, String content) {
		tv_title_dialog.setText(title);
		tv_content_dialog.setText(content);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(view);
		this.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		this.show();
	}

	@Override
	public void onClick(View view) {
		this.dismiss();
		this.dialogListener.SetOnClick(view);
	}

}
