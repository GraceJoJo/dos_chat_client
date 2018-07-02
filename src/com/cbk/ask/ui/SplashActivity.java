package com.cbk.ask.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.cbk.ask.AskHelper;
import com.cbk.ask.R;
import com.cbk.ask.domain.Ask.MatchDoctorRespones;
import com.cbk.ask.domain.User.User;
import com.cbk.ask.domain.User.UserResponse;
import com.cbk.ask.net.AppCallback;
import com.cbk.ask.net.CGNetworkHelper;
import com.cbk.ask.net.UrlPaths;
import com.cbk.ask.utils.ChoseTipDialog;
import com.cbk.ask.utils.ChoseTipDialog.ChoseTipDialogListener;
import com.cbk.ask.utils.TipDialog;
import com.cbk.ask.utils.TipDialog.TipDialogListener;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 开屏页
 * 
 */
public class SplashActivity extends BaseActivity implements
		AppCallback<JSONObject>,TipDialogListener,ChoseTipDialogListener{
	private SharedPreferences mConfig;
	private Boolean isFirst;
	private String macAddress;
	private final int InitUserFlag = 10;
	private final int MatchDoctorFlag = 11;
	private int type=0;//0:视频 1：语音
	private boolean isChosed = false;//
	private int recLen = 0; 
	private ChoseTipDialog choseTipDialog;
	
	Handler handler = new Handler();    

	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.em_activity_splash);
		super.onCreate(arg0);
		macAddress = getId();
		// 检查是否已经初始化过，未初始化时进行初始化操作
		mConfig = getSharedPreferences("appConfig", MODE_PRIVATE);
		isFirst = mConfig.getBoolean("isFirst", true);
	}
	
	private String getId(){
		TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
		String szImei = TelephonyMgr.getDeviceId(); 
		String m_szDevIDShort = "35" + //we make this look like a valid IMEI 
		Build.BOARD.length()%10 + 
		Build.BRAND.length()%10 + 
		Build.CPU_ABI.length()%10 + 
		Build.DEVICE.length()%10 + 
		Build.DISPLAY.length()%10 + 
		Build.HOST.length()%10 + 
		Build.ID.length()%10 + 
		Build.MANUFACTURER.length()%10 + 
		Build.MODEL.length()%10 + 
		Build.PRODUCT.length()%10 + 
		Build.TAGS.length()%10 + 
		Build.TYPE.length()%10 + 
		Build.USER.length()%10 ; //13 digits
		
		String m_szAndroidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		
		BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter      
		m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();      
		String m_szBTMAC = null;
		if(m_BluetoothAdapter!=null) 
			m_szBTMAC = m_BluetoothAdapter.getAddress();
		
		WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();


		
		String m_szLongID = szImei + m_szDevIDShort 
			    + m_szAndroidID+ m_szWLANMAC + m_szBTMAC;      
			// compute md5     
			 MessageDigest m = null;   
			try {
			 m = MessageDigest.getInstance("MD5");
			 } catch (NoSuchAlgorithmException e) {
			 e.printStackTrace();   
			}    
			m.update(m_szLongID.getBytes(),0,m_szLongID.length());   
			// get md5 bytes   
			byte p_md5Data[] = m.digest();   
			// create a hex string   
			String m_szUniqueID = new String();   
			for (int i=0;i<p_md5Data.length;i++) {   
			     int b =  (0xFF & p_md5Data[i]);    
			// if it is a single digit, make sure it have 0 in front (proper padding)    
			    if (b <= 0xF) 
			        m_szUniqueID+="0";    
			// add number to string    
			    m_szUniqueID+=Integer.toHexString(b); 
			   }   // hex string to uppercase   
			m_szUniqueID = m_szUniqueID.toUpperCase();
			return m_szUniqueID;
	}

	/**
	 * 首次打开初始化系统
	 */
	private void RegisterToServer() {
		showProgress("系统初始化中……");
		CGNetworkHelper http = new CGNetworkHelper(this, InitUserFlag);
		http.setUiDataListener(this);
		User user = new User();
		user.setUserName(macAddress);
		http.sendPostRequest(UrlPaths.Register_User_Url, user);
	}

	@Override
	public void callback(int Flag, JSONObject jsonobj) {
		switch (Flag) {
		case InitUserFlag:
			UserResponse response = JSONObject.toJavaObject(jsonobj,
					UserResponse.class);
			if (response.getCode() == 0) {
				Editor editor = mConfig.edit();
				editor.putBoolean("isFirst", false);
				editor.commit();
				// 注册成功登录环信，发起匹配请求
				LoginWithUserName();
			} else {
				dismissProgress();
				TipDialog dialog = new TipDialog(this,this);
				dialog.show("提示","系统初始化失败，请联系管理员！");
			}
			break;
		case MatchDoctorFlag:
			//
			MatchDoctorRespones respones = JSONObject.toJavaObject(jsonobj,
					MatchDoctorRespones.class);
			if (respones.getCode() == 0) {
				String toChatUsername = String.format("%s_doctor",
						respones.getPhoneNumber());
				if (type==0) {
					// 发起视频通话
					startActivity(new Intent(this, VideoCallActivity.class)
							.putExtra("username", toChatUsername).putExtra(
									"isComingCall", false));
				}else{
					startActivity(new Intent(this, VoiceCallActivity.class)
					.putExtra("username", toChatUsername).putExtra(
							"isComingCall", false));
				}
				finish();
			} else {
				dismissProgress();
				TipDialog dialog = new TipDialog(this,this);
				dialog.show("提示",respones.getMsg());
			}
			break;
		default:
			break;
		}
	}

	private void LoginWithUserName() {
		EMClient.getInstance().login(macAddress, "1qaz2wsx", new EMCallBack() {
			@Override
			public void onSuccess() {
				dismissProgress();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
//						type = 1;
//						MatchDoctor();
						//弹出选择框，选择语音或者视频
						choseTipDialog = new ChoseTipDialog(SplashActivity.this,SplashActivity.this);
						choseTipDialog.showDialog();
						isChosed = false;
						handler.postDelayed(runnable, 1000); 
					}
				});
			}

			@Override
			public void onProgress(int progress, String status) {
			}

			@Override
			public void onError(final int code, final String message) {
				runOnUiThread(new Runnable() {
					public void run() {
						dismissProgress();
						showToast(getString(R.string.Login_failed) + message);
						finish();
					}
				});
			}
		});
	}

	@Override
	public void onError(int Flag, String errorCode, String errorMessage) {
		dismissProgress();
		switch (Flag) {
		case InitUserFlag:
			showToast("系统初始化失败，请联系管理员！");
			finish();
			break;
		case MatchDoctorFlag:
			showToast("匹配医生失败，请联系管理员！");
			finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 匹配医生
	 */
	private void MatchDoctor() {
		showProgress("正在匹配医生……");
		CGNetworkHelper http = new CGNetworkHelper(this, MatchDoctorFlag);
		http.setUiDataListener(this);
		User user = new User();
		user.setUserName(macAddress);
		http.sendPostRequest(UrlPaths.Macth_Doctor_Url, user);
	}


	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (isFirst) {
			RegisterToServer();
		} else {
			if (AskHelper.getInstance().isLoggedIn()) {
				// 匹配医生
				//弹出选择框，选择语音或者视频
//				type = 1;
//				MatchDoctor();
				choseTipDialog = new ChoseTipDialog(this, this);
				choseTipDialog.showDialog();
				isChosed = false;
				handler.postDelayed(runnable, 1000); 
				//30秒后没有选择，那么退出界面
			} else {
				LoginWithUserName();
				// 发起登录
			}
		}
	}

	 Runnable runnable = new Runnable() {    
	        @Override    
	        public void run() {    
	            recLen++;   
	            if(recLen >= 30)
	            {
	            	choseTipDialog.dismiss();
	            	SplashActivity.this.finish();
	            }else{
	            	if(!isChosed)
	            		handler.postDelayed(this, 1000);  
	            }
	        }    
	    };    

	@Override
	public void SetOnClick(View view) {
		isChosed = true;
		finish();
	}

	@Override
	public void SetOnClick(Boolean ok, int type) {
		isChosed = true;
		if (ok) {
			this.type = type;
			MatchDoctor();
		}else{
			finish();
		}
	}
}
