/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cbk.ask.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.List;
import java.util.UUID;

import android.R.integer;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.cbk.ask.AskHelper;
import com.cbk.ask.R;
import com.cbk.ask.domain.Prescription;
import com.cbk.ask.net.UrlPaths;
import com.cbk.ask.utils.TipDialog;
import com.cbk.ask.utils.TipDialog.TipDialogListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMCallManager.EMCameraDataProcessor;
import com.hyphenate.chat.EMCallManager.EMVideoCallHelper;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMLocalSurfaceView;
import com.hyphenate.media.EMOppositeSurfaceView;
import com.printsdk.cmd.PrintCmd;

public class VideoCallActivity extends CallActivity implements OnClickListener,
		TipDialogListener {

	private boolean isMuteState;
	private boolean isHandsfreeState;
	private boolean isAnswered;
	private boolean endCallTriggerByMe = false;

	private TextView callStateTextView;

	private LinearLayout comingBtnContainer;
	private Button refuseBtn;
	private Button answerBtn;
	private Button hangupBtn;
	private ImageView muteImage;
	private ImageView handsFreeImage;
	private Chronometer chronometer;
	private LinearLayout voiceContronlLayout;
	private RelativeLayout rootContainer;
	private LinearLayout topContainer;
	private LinearLayout bottomContainer;
	private TextView netwrokStatusVeiw;

	private Handler uiHandler;

	private boolean isInCalling;
	boolean isRecording = false;
	private EMVideoCallHelper callHelper;
	private Boolean isShowPre = false;
	private RelativeLayout rl_pre;

	private WebView webView;
	private Prescription prescription;
	com.printsdk.usbsdk.UsbDriver mUsbDriver;
	private Bitmap image2 = null, image4 = null;
	final int SERIAL_BAUDRATE = com.printsdk.usbsdk.UsbDriver.BAUD115200;
	private Boolean isPringting = false;

	private BrightnessDataProcess dataProcessor = new BrightnessDataProcess();

	// dynamic adjust brightness
	class BrightnessDataProcess implements EMCameraDataProcessor {
		byte yDelta = 0;

		synchronized void setYDelta(byte yDelta) {
			Log.d("VideoCallActivity", "brigntness uDelta:" + yDelta);
			this.yDelta = yDelta;
		}

		// data size is width*height*2
		// the first width*height is Y, second part is UV
		// the storage layout detailed please refer 2.x demo
		// CameraHelper.onPreviewFrame
		@Override
		public synchronized void onProcessData(byte[] data, Camera camera,
				final int width, final int height, final int rotateAngel) {
			int wh = width * height;
			for (int i = 0; i < wh; i++) {
				int d = (data[i] & 0xFF) + yDelta;
				d = d < 16 ? 16 : d;
				d = d > 235 ? 235 : d;
				data[i] = (byte) d;
			}
		}
	}

	EMMessageListener messageListener = new EMMessageListener() {
		@Override
		public void onMessageReceived(List<EMMessage> messages) {
		}

		@Override
		public void onCmdMessageReceived(List<EMMessage> messages) {
			for (EMMessage message : messages) {
				EMCmdMessageBody cmdMsgBody = (EMCmdMessageBody) message
						.getBody();
				final String action = cmdMsgBody.action();// 获取自定义action
				if (isShowPre)
					break;
				if (action.equals("prescription")) {// 收到处方
					isShowPre = true;
					// 获取文件名
					try {
						final String str = message
								.getStringAttribute("prescription");
						prescription = JSON
								.parseObject(str, Prescription.class);
						// 弹出提示框
						runOnUiThread(new Runnable() {
							public void run() {
								rl_pre.setVisibility(View.VISIBLE);
								webView.loadUrl(UrlPaths.IMAGEHOST
										+ "/prescription/"
										+ prescription.getNo() + ".html");
							}
						});

					} catch (HyphenateException e) {
						e.printStackTrace();
					}
					//
				}
			}
		}

		@Override
		public void onMessageChanged(EMMessage message, Object change) {
		}

		@Override
		public void onMessageDelivered(List<EMMessage> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMessageRead(List<EMMessage> arg0) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			finish();
			return;
		}
		setContentView(R.layout.em_activity_video_call);

		AskHelper.getInstance().isVideoCalling = true;
		callType = 1;

		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		uiHandler = new Handler();

		callStateTextView = (TextView) findViewById(R.id.tv_call_state);
		comingBtnContainer = (LinearLayout) findViewById(R.id.ll_coming_call);
		rootContainer = (RelativeLayout) findViewById(R.id.root_layout);
		refuseBtn = (Button) findViewById(R.id.btn_refuse_call);
		answerBtn = (Button) findViewById(R.id.btn_answer_call);
		hangupBtn = (Button) findViewById(R.id.btn_hangup_call);
		muteImage = (ImageView) findViewById(R.id.iv_mute);
		handsFreeImage = (ImageView) findViewById(R.id.iv_handsfree);
		callStateTextView = (TextView) findViewById(R.id.tv_call_state);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		voiceContronlLayout = (LinearLayout) findViewById(R.id.ll_voice_control);
		RelativeLayout btnsContainer = (RelativeLayout) findViewById(R.id.ll_btns);
		topContainer = (LinearLayout) findViewById(R.id.ll_top_container);
		bottomContainer = (LinearLayout) findViewById(R.id.ll_bottom_container);
		netwrokStatusVeiw = (TextView) findViewById(R.id.tv_network_status);

		refuseBtn.setOnClickListener(this);
		answerBtn.setOnClickListener(this);
		hangupBtn.setOnClickListener(this);
		muteImage.setOnClickListener(this);
		handsFreeImage.setOnClickListener(this);
		rootContainer.setOnClickListener(this);

		msgid = UUID.randomUUID().toString();
		isInComingCall = getIntent().getBooleanExtra("isComingCall", false);
		username = getIntent().getStringExtra("username");

		// local surfaceview
		localSurface = (EMLocalSurfaceView) findViewById(R.id.local_surface);
		localSurface.setZOrderMediaOverlay(true);
		localSurface.setZOrderOnTop(true);

		// remote surfaceview
		oppositeSurface = (EMOppositeSurfaceView) findViewById(R.id.opposite_surface);

		// set call state listener
		addCallStateListener();
		if (!isInComingCall) {// outgoing call
			soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
			outgoing = soundPool.load(this, R.raw.em_outgoing, 1);

			comingBtnContainer.setVisibility(View.INVISIBLE);
			hangupBtn.setVisibility(View.VISIBLE);
			String st = getResources().getString(
					R.string.Are_connected_to_each_other);
			callStateTextView.setText(st);
			EMClient.getInstance().callManager()
					.setSurfaceView(localSurface, oppositeSurface);
			handler.sendEmptyMessage(MSG_CALL_MAKE_VIDEO);
		} else { // incoming call
			voiceContronlLayout.setVisibility(View.INVISIBLE);
			localSurface.setVisibility(View.INVISIBLE);
			Uri ringUri = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			audioManager.setMode(AudioManager.MODE_RINGTONE);
			audioManager.setSpeakerphoneOn(true);
			ringtone = RingtoneManager.getRingtone(this, ringUri);
			ringtone.play();
			EMClient.getInstance().callManager()
					.setSurfaceView(localSurface, oppositeSurface);
		}

		// get instance of call helper, should be called after setSurfaceView
		// was called
		callHelper = EMClient.getInstance().callManager().getVideoCallHelper();

		EMClient.getInstance().callManager()
				.setCameraDataProcessor(dataProcessor);
		EMClient.getInstance().chatManager()
				.addMessageListener(messageListener);

		rl_pre = (RelativeLayout) findViewById(R.id.rl_pre);

		webView = (WebView) findViewById(R.id.webview);
		LayoutParams params = webView.getLayoutParams();
		// 设定webview高度
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		params.width = width;
		params.height = width * 148 / 210;
		webView.setLayoutParams(params);
		WebSettings wSet = webView.getSettings();
		wSet.setJavaScriptEnabled(true);
		getUsbDriverService();
	}

	class YDeltaSeekBarListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			dataProcessor.setYDelta((byte) (20.0f * (progress - 50) / 50.0f));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}

	/**
	 * set call state listener
	 */
	void addCallStateListener() {
		callStateListener = new EMCallStateChangeListener() {

			@Override
			public void onCallStateChanged(CallState callState,
					final CallError error) {
				switch (callState) {

				case CONNECTING: // is connecting
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							callStateTextView
									.setText(R.string.Are_connected_to_each_other);
						}

					});
					break;
				case CONNECTED: // connected
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							callStateTextView
									.setText(R.string.have_connected_with);
						}
					});
					break;

				case ACCEPTED: // call is accepted
					handler.removeCallbacks(timeoutHangup);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							try {
								if (soundPool != null)
									soundPool.stop(streamID);
							} catch (Exception e) {
							}
							openSpeakerOn();
							((TextView) findViewById(R.id.tv_is_p2p))
									.setText(EMClient.getInstance()
											.callManager().isDirectCall() ? R.string.direct_call
											: R.string.relay_call);
							handsFreeImage
									.setImageResource(R.drawable.em_icon_speaker_on);
							isHandsfreeState = true;
							isInCalling = true;
							chronometer.setVisibility(View.VISIBLE);
							chronometer.setBase(SystemClock.elapsedRealtime());
							// call durations start
							chronometer.start();
							callStateTextView.setText(R.string.In_the_call);
							callingState = CallingState.NORMAL;
						}

					});
					break;
				case NETWORK_UNSTABLE:
					runOnUiThread(new Runnable() {
						public void run() {
							netwrokStatusVeiw.setVisibility(View.VISIBLE);
							if (error == CallError.ERROR_NO_DATA) {
								netwrokStatusVeiw
										.setText(R.string.no_call_data);
							} else {
								netwrokStatusVeiw
										.setText(R.string.network_unstable);
							}
						}
					});
					break;
				case NETWORK_NORMAL:
					runOnUiThread(new Runnable() {
						public void run() {
							netwrokStatusVeiw.setVisibility(View.INVISIBLE);
						}
					});
					break;
				case VIDEO_PAUSE:
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(),
									"VIDEO_PAUSE", Toast.LENGTH_SHORT).show();
						}
					});
					break;
				case VIDEO_RESUME:
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(),
									"VIDEO_RESUME", Toast.LENGTH_SHORT).show();
						}
					});
					break;
				case VOICE_PAUSE:
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(),
									"VOICE_PAUSE", Toast.LENGTH_SHORT).show();
						}
					});
					break;
				case VOICE_RESUME:
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(),
									"VOICE_RESUME", Toast.LENGTH_SHORT).show();
						}
					});
					break;
				case DISCONNECTED: // call is disconnected
					handler.removeCallbacks(timeoutHangup);
					@SuppressWarnings("UnnecessaryLocalVariable")
					final CallError fError = error;
					runOnUiThread(new Runnable() {
						private void postDelayedCloseMsg() {
							uiHandler.postDelayed(new Runnable() {
								@Override
								public void run() {
									saveCallRecord();
									Animation animation = new AlphaAnimation(
											1.0f, 0.0f);
									animation.setDuration(800);
									rootContainer.startAnimation(animation);
									finish();
								}

							}, 200);
						}

						@Override
						public void run() {
							chronometer.stop();
							callDruationText = chronometer.getText().toString();
							String s1 = getResources().getString(
									R.string.The_other_party_refused_to_accept);
							String s2 = getResources().getString(
									R.string.Connection_failure);
							String s3 = getResources().getString(
									R.string.The_other_party_is_not_online);
							String s4 = getResources().getString(
									R.string.The_other_is_on_the_phone_please);
							String s5 = getResources().getString(
									R.string.The_other_party_did_not_answer);

							String s6 = getResources().getString(
									R.string.hang_up);
							String s7 = getResources().getString(
									R.string.The_other_is_hang_up);
							String s8 = getResources().getString(
									R.string.did_not_answer);
							String s9 = getResources().getString(
									R.string.Has_been_cancelled);

							if (fError == CallError.REJECTED) {
								callingState = CallingState.BEREFUESD;
								callStateTextView.setText(s1);
							} else if (fError == CallError.ERROR_TRANSPORT) {
								callStateTextView.setText(s2);
							} else if (fError == CallError.ERROR_UNAVAILABLE) {
								callingState = CallingState.OFFLINE;
								callStateTextView.setText(s3);
							} else if (fError == CallError.ERROR_BUSY) {
								callingState = CallingState.BUSY;
								callStateTextView.setText(s4);
							} else if (fError == CallError.ERROR_NORESPONSE) {
								callingState = CallingState.NORESPONSE;
								callStateTextView.setText(s5);
							} else if (fError == CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED
									|| fError == CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED) {
								callingState = CallingState.VERSION_NOT_SAME;
								callStateTextView
										.setText(R.string.call_version_inconsistent);
							} else {
								if (isAnswered) {
									callingState = CallingState.NORMAL;
									if (endCallTriggerByMe) {
										// callStateTextView.setText(s6);
									} else {
										callStateTextView.setText(s7);
									}
								} else {
									if (isInComingCall) {
										callingState = CallingState.UNANSWERED;
										callStateTextView.setText(s8);
									} else {
										if (callingState != CallingState.NORMAL) {
											callingState = CallingState.CANCED;
											callStateTextView.setText(s9);
										} else {
											callStateTextView.setText(s6);
										}
									}
								}
							}
							postDelayedCloseMsg();
						}

					});

					break;

				default:
					break;
				}

			}
		};
		EMClient.getInstance().callManager()
				.addCallStateChangeListener(callStateListener);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_refuse_call: // decline the call
			refuseBtn.setEnabled(false);
			handler.sendEmptyMessage(MSG_CALL_REJECT);
			break;

		case R.id.btn_answer_call: // answer the call
			answerBtn.setEnabled(false);
			openSpeakerOn();
			if (ringtone != null)
				ringtone.stop();

			callStateTextView.setText("answering...");
			handler.sendEmptyMessage(MSG_CALL_ANSWER);
			handsFreeImage.setImageResource(R.drawable.em_icon_speaker_on);
			isAnswered = true;
			isHandsfreeState = true;
			comingBtnContainer.setVisibility(View.INVISIBLE);
			hangupBtn.setVisibility(View.VISIBLE);
			voiceContronlLayout.setVisibility(View.VISIBLE);
			localSurface.setVisibility(View.VISIBLE);
			break;

		case R.id.btn_hangup_call: // hangup
			hangupBtn.setEnabled(false);
			chronometer.stop();
			endCallTriggerByMe = true;
			callStateTextView.setText(getResources().getString(
					R.string.hanging_up));
			if (isRecording) {
				callHelper.stopVideoRecord();
			}
			handler.sendEmptyMessage(MSG_CALL_END);
			break;

		case R.id.iv_mute: // mute
			if (isMuteState) {
				// resume voice transfer
				muteImage.setImageResource(R.drawable.em_icon_mute_normal);
				try {
					EMClient.getInstance().callManager().resumeVoiceTransfer();
				} catch (HyphenateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				isMuteState = false;
			} else {
				// pause voice transfer
				muteImage.setImageResource(R.drawable.em_icon_mute_on);
				try {
					EMClient.getInstance().callManager().pauseVoiceTransfer();
				} catch (HyphenateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				isMuteState = true;
			}
			break;
		case R.id.iv_handsfree: // handsfree
			if (isHandsfreeState) {
				// turn off speaker
				handsFreeImage
						.setImageResource(R.drawable.em_icon_speaker_normal);
				closeSpeakerOn();
				isHandsfreeState = false;
			} else {
				handsFreeImage.setImageResource(R.drawable.em_icon_speaker_on);
				openSpeakerOn();
				isHandsfreeState = true;
			}
			break;
		case R.id.root_layout:
			if (callingState == CallingState.NORMAL) {
				if (bottomContainer.getVisibility() == View.VISIBLE) {
					bottomContainer.setVisibility(View.GONE);
					topContainer.setVisibility(View.GONE);

				} else {
					bottomContainer.setVisibility(View.VISIBLE);
					topContainer.setVisibility(View.VISIBLE);

				}
			}
			break;
		default:
			break;
		}

	}

	@Override
	protected void onDestroy() {
		AskHelper.getInstance().isVideoCalling = false;
		if (isRecording) {
			callHelper.stopVideoRecord();
			isRecording = false;
		}
		localSurface = null;
		oppositeSurface = null;
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		callDruationText = chronometer.getText().toString();
		super.onBackPressed();
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		if (isInCalling) {
			try {
				EMClient.getInstance().callManager().pauseVideoTransfer();
			} catch (HyphenateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isInCalling) {
			try {
				EMClient.getInstance().callManager().resumeVideoTransfer();
			} catch (HyphenateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void downloadFile() {
		new AsyncTask<String, integer, Bitmap>() {
			@Override
			protected Bitmap doInBackground(String... params) {
				// 加载图片
				String url = params[0];
				Bitmap bitmap = null;
				URLConnection connection;
				InputStream is;// 用于获取数据的输入流
				ByteArrayOutputStream bos;// 可以捕获内存缓冲区的数据，转换成字节数组。
				int len;
				try {
					// 获取网络连接对象
					connection = (URLConnection) new java.net.URL(url)
							.openConnection();
					// 获取输入流
					is = connection.getInputStream();
					bos = new ByteArrayOutputStream();
					byte[] data = new byte[1024];
					while ((len = is.read(data)) != -1) {
						bos.write(data, 0, len);
					}
					bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(),
							0, bos.toByteArray().length);
					float width = bitmap.getWidth();
					float height = bitmap.getHeight();
					// 创建操作图片用的matrix对象
					Matrix matrix = new Matrix();
					// 计算宽高缩放率
					float scaleWidth = ((float) 150) / width;
					float scaleHeight = ((float) 132) / height;
					// 缩放图片动作
					matrix.postScale(scaleWidth, scaleHeight);
					bitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) width,
							(int) height, matrix, true);
					bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(),
							0, bos.toByteArray().length, getBitmapOption(1));
					is.close();
					bos.close();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				super.onPostExecute(result);
				image2 = result;
				if (image2 != null && image4 != null) {
					print();
				}
			}

		}.execute(prescription.getYishi());

		new AsyncTask<String, integer, Bitmap>() {
			@Override
			protected Bitmap doInBackground(String... params) {
				// 加载图片
				String url = params[0];
				Bitmap bitmap = null;
				URLConnection connection;
				InputStream is;// 用于获取数据的输入流
				ByteArrayOutputStream bos;// 可以捕获内存缓冲区的数据，转换成字节数组。
				int len;
				try {
					// 获取网络连接对象
					connection = (URLConnection) new java.net.URL(url)
							.openConnection();
					// 获取输入流
					is = connection.getInputStream();
					bos = new ByteArrayOutputStream();
					byte[] data = new byte[1024];
					while ((len = is.read(data)) != -1) {
						bos.write(data, 0, len);
					}
					bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(),
							0, bos.toByteArray().length);
					float width = bitmap.getWidth();
					float height = bitmap.getHeight();
					// 创建操作图片用的matrix对象
					Matrix matrix = new Matrix();
					// 计算宽高缩放率
					float scaleWidth = ((float) 150) / width;
					float scaleHeight = ((float) 132) / height;
					// 缩放图片动作
					matrix.postScale(scaleWidth, scaleHeight);
					bitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) width,
							(int) height, matrix, true);
					bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(),
							0, bos.toByteArray().length, getBitmapOption(1));
					is.close();
					bos.close();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				super.onPostExecute(result);
				image4 = result;
				if (image2 != null && image4 != null) {
					print();
				}
			}

		}.execute(prescription.getYaoshi());
	}

	/**
	 * BitmapOption 位图选项
	 * 
	 * @param inSampleSize
	 * @return
	 */
	private static Options getBitmapOption(int inSampleSize) {
		System.gc();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inSampleSize = inSampleSize;
		options.inPreferredConfig = Config.ARGB_4444; // T4 二维码图片效果最佳
		return options;
	}

	private void getUsbDriverService() {
		mUsbDriver = new com.printsdk.usbsdk.UsbDriver(
				(UsbManager) getSystemService(Context.USB_SERVICE), this);

		PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0,
				new Intent("com.usb.sample.USB_PERMISSION"), 0);
		mUsbDriver.setPermissionIntent(permissionIntent);
		// Broadcast listen for new devices
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		this.registerReceiver(mUsbReceiver, filter);

		// 打开设备
		if (mUsbDriver.openUsbDevice(SERIAL_BAUDRATE)) {
			// 连接设备成功
		} else {
			// 连接设备失败
		}
	}

	/*
	 * BroadcastReceiver when insert/remove the device USB plug into/from a USB
	 * port 创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
	 */
	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				mUsbDriver.usbAttached(intent);
				mUsbDriver.openUsbDevice(SERIAL_BAUDRATE);
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				mUsbDriver.closeUsbDevice();
				mUsbDriver.usbDetached(intent);
			}
		}
	};

	/**
	 * 打印处方
	 * 
	 * @param view
	 */
	public void printing(View view) {
		if (!isPringting) {
			isPringting = true;
			downloadFile();
		}
	}

	private void print() {
		getUsbDriverService();
		getPrintTicketData();
	}

	/**
	 * 2.小票打印
	 */
	private void getPrintTicketData() {
		int iStatus = getPrinterStatus();
		if (checkStatus(iStatus) != 0)
			return;
		Bitmap image = null;
		AssetManager am = getResources().getAssets();
		try {
			InputStream is = am.open("1.png");
			image = BitmapFactory.decodeStream(is, null, getBitmapOption(1));
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int[] data = getBitmapParamsData(image);
		mUsbDriver.write(PrintCmd.PrintDiskImagefile(data, width, heigh));

		mUsbDriver.write(PrintCmd.SetBold(0));
		mUsbDriver.write(PrintCmd.SetAlignment(1));
		mUsbDriver.write(PrintCmd.SetSizetext(1, 1));
		mUsbDriver.write(PrintCmd.PrintString(prescription.getHospital()
				+ "处方笺\n\n", 0));
		CleanPrinter();

		mUsbDriver.write(PrintCmd.SetLinespace(10));
		mUsbDriver.write(PrintCmd.SetAlignment(2));
		mUsbDriver.write(PrintCmd.PrintString("处方编号：" + prescription.getPreNo()
				+ "\n", 0));

		mUsbDriver.write(PrintCmd.SetAlignment(0));
		mUsbDriver.write(PrintCmd.PrintString("———————————————————————", 0));

		mUsbDriver.write(PrintCmd.PrintString(
				"姓名：" + prescription.getName() + " 性别：" + prescription.getSex()
						+ " 年龄：" + prescription.getAge() + " 岁\n", 0));
		mUsbDriver.write(PrintCmd.PrintString("科别：" + prescription.getKeshi()
				+ "\n", 0));
		mUsbDriver.write(PrintCmd.PrintString(
				"开 具 日 期：" + prescription.getDate() + "\n", 0));

		mUsbDriver.write(PrintCmd.PrintString("电话：" + prescription.getPhone()
				+ "\n", 0));

		mUsbDriver.write(PrintCmd.PrintString("———————————————————————", 0));

		mUsbDriver.write(PrintCmd.SetLinespace(20));
		mUsbDriver.write(PrintCmd.SetBold(1));
		mUsbDriver.write(PrintCmd.PrintString("   RP.\n", 0));

		CleanPrinter();
		String[] pres = prescription.getYaopin().split("\n");
		for (String string : pres) {
			mUsbDriver.write(PrintCmd.PrintString(string + "\n", 0));
		}

		mUsbDriver.write(PrintCmd.PrintString("———————————————————————", 0));

		Bitmap image1 = null;
		try {
			InputStream is = am.open("yishi.png");
			image1 = BitmapFactory.decodeStream(is, null, getBitmapOption(1));
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bitmap image3 = null;
		try {
			InputStream is = am.open("yaoshi.png");
			image3 = BitmapFactory.decodeStream(is, null, getBitmapOption(1));
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 图片拼接
		Bitmap image11 = add2Bitmap(image1, image2);
		Bitmap image12 = add2Bitmap(image11, image3);
		Bitmap image13 = add2Bitmap(image12, image4);

		int[] data1 = getBitmapParamsData(image13);

		byte[] data12 = PrintCmd.PrintDiskImagefile(data1, width, heigh);
		mUsbDriver.write(data12);
		SetFeedCutClean(0);
		rl_pre.setVisibility(View.GONE);
	}

	private Bitmap add2Bitmap(Bitmap first, Bitmap second) {
		int width = first.getWidth() + second.getWidth();
		int height = Math.max(first.getHeight(), second.getHeight());
		Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_4444);
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(first, 0, 0, null);
		canvas.drawBitmap(second, first.getWidth(), 0, null);
		return result;
	}

	int width, heigh;

	private int[] getBitmapParamsData(Bitmap bm) {
		if (bm != null) {
			width = bm.getWidth();
			heigh = bm.getHeight();
		}
		int iDataLen = width * heigh;
		int[] pixels = new int[iDataLen];
		bm.getPixels(pixels, 0, width, 0, 0, width, heigh);
		return pixels;
	}

	// 走纸换行、切纸、清理缓存
	private void SetFeedCutClean(int iMode) {
		mUsbDriver.write(PrintCmd.PrintFeedline(5)); // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode)); // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean()); // 清理缓存
	}

	// 清理缓存，缺省模式
	private void CleanPrinter() {
		mUsbDriver.write(PrintCmd.SetBold(0)); // 粗体设置
		mUsbDriver.write(PrintCmd.SetAlignment(0)); // 对齐方式
		mUsbDriver.write(PrintCmd.SetSizetext(0, 0)); // 字符大小
	}

	// 检测打印机状态
	private int getPrinterStatus() {
		int iRet = -1;

		byte[] bRead1 = new byte[1];
		byte[] bWrite1 = PrintCmd.GetStatus1();
		if (mUsbDriver.read(bRead1, bWrite1) > 0) {
			iRet = PrintCmd.CheckStatus1(bRead1[0]);
		}

		if (iRet != 0)
			return iRet;

		byte[] bRead2 = new byte[1];
		byte[] bWrite2 = PrintCmd.GetStatus2();
		if (mUsbDriver.read(bRead2, bWrite2) > 0) {
			iRet = PrintCmd.CheckStatus2(bRead2[0]);
		}

		if (iRet != 0)
			return iRet;

		byte[] bRead3 = new byte[1];
		byte[] bWrite3 = PrintCmd.GetStatus3();
		if (mUsbDriver.read(bRead3, bWrite3) > 0) {
			iRet = PrintCmd.CheckStatus3(bRead3[0]);
		}

		if (iRet != 0)
			return iRet;

		byte[] bRead4 = new byte[1];
		byte[] bWrite4 = PrintCmd.GetStatus4();
		if (mUsbDriver.read(bRead4, bWrite4) > 0) {
			iRet = PrintCmd.CheckStatus4(bRead4[0]);
		}

		return iRet;
	}

	private int checkStatus(int iStatus) {
		iStatus = 0;
		int iRet = -1;

		StringBuilder sMsg = new StringBuilder();

		// 0 打印机正常 、1 打印机未连接或未上电、2 打印机和调用库不匹配
		// 3 打印头打开 、4 切刀未复位 、5 打印头过热 、6 黑标错误 、7 纸尽 、8 纸将尽
		switch (iStatus) {
		case 0:
			sMsg.append("正常"); // 正常
			iRet = 0;
			break;
		case 8:
			sMsg.append("纸将尽"); // 纸将尽
			iRet = 0;
			break;
		case 3:
			sMsg.append("打印头打开"); //
			break;
		case 4:
			sMsg.append("切刀未复位");
			break;
		case 5:
			sMsg.append("打印头过热");
			break;
		case 6:
			sMsg.append("黑标错误");
			break;
		case 7:
			sMsg.append("纸尽"); // 纸尽==缺纸
			break;
		case 1:
			sMsg.append("打印机未连接或未上电");
			break;
		default:
			sMsg.append("位置异常"); // 异常
			break;
		}
		sMsg.append(",请联系管理员！");
		TipDialog dialog = new TipDialog(this, this);
		dialog.show("提示", sMsg.toString());
		return iRet;
	}

	@Override
	public void SetOnClick(View view) {
		// TODO Auto-generated method stub

	}

}
