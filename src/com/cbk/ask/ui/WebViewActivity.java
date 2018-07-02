package com.cbk.ask.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;

import android.R.integer;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.cbk.ask.R;
import com.cbk.ask.domain.Prescription;
import com.cbk.ask.net.UrlPaths;
import com.cbk.ask.utils.TipDialog;
import com.cbk.ask.utils.TipDialog.TipDialogListener;
import com.printsdk.cmd.PrintCmd;

public class WebViewActivity extends BaseActivity implements OnClickListener,TipDialogListener {

	private WebView webView;
	// private File mScreenshotDir;
	// private String mImageFileName;
	// private String mImageFilePath;
	// private long mImageTime;
	// private int mImageWidth;
	// private int mImageHeight;
	private RelativeLayout rl_tip;
	private Prescription prescription;
	com.printsdk.usbsdk.UsbDriver mUsbDriver;
	private Bitmap image2 = null,image4 = null;
	final int SERIAL_BAUDRATE = com.printsdk.usbsdk.UsbDriver.BAUD115200;
	private Boolean isPringting = false;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		String str = getIntent().getStringExtra("prescription");
		prescription = JSON.parseObject(str, Prescription.class);
		setContentView(R.layout.webview_activity);
		rl_tip = (RelativeLayout) findViewById(R.id.rl_tip);
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
		webView.loadUrl(UrlPaths.IMAGEHOST + "/prescription/" + prescription.getNo()
				+ ".html");
		rl_tip.setOnClickListener(this);
		getUsbDriverService();
	}
	
	private void downloadFile()
	{
		new AsyncTask<String, integer, Bitmap>(
				) {
					@Override
					protected Bitmap doInBackground(String... params) {
						//加载图片
						 String url = params[0];
				            Bitmap bitmap=null;
				            URLConnection connection;
				            InputStream is;//用于获取数据的输入流
				            ByteArrayOutputStream bos;//可以捕获内存缓冲区的数据，转换成字节数组。
				            int len;
				            try {
				                //获取网络连接对象
				                connection=(URLConnection) new java.net.URL(url).openConnection();
				                //获取输入流
				                is=connection.getInputStream();
				                bos=new ByteArrayOutputStream();
				                byte []data=new byte[1024];
				                while((len=is.read(data))!=-1){
				                    bos.write(data,0,len);
				                }
				                bitmap=BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().length);
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
				                bitmap=BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().length,getBitmapOption(1));
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
						if(image2!=null && image4!= null)
						{
							print();
						}
					}
					
		}.execute(prescription.getYishi());
		
		new AsyncTask<String, integer, Bitmap>(
				) {
					@Override
					protected Bitmap doInBackground(String... params) {
						//加载图片
						 String url = params[0];
				            Bitmap bitmap=null;
				            URLConnection connection;
				            InputStream is;//用于获取数据的输入流
				            ByteArrayOutputStream bos;//可以捕获内存缓冲区的数据，转换成字节数组。
				            int len;
				            try {
				                //获取网络连接对象
				                connection=(URLConnection) new java.net.URL(url).openConnection();
				                //获取输入流
				                is=connection.getInputStream();
				                bos=new ByteArrayOutputStream();
				                byte []data=new byte[1024];
				                while((len=is.read(data))!=-1){
				                    bos.write(data,0,len);
				                }
				                bitmap=BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().length);
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
				                bitmap=BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().length,getBitmapOption(1));
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
						if(image2!=null && image4!= null)
						{
							print();
						}
					}
					
		}.execute(prescription.getYaoshi());
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ll_back:
			finish();
			break;
		case R.id.rl_tip:
			rl_tip.setVisibility(View.GONE);
			break;

		default:
			break;
		}
	}

	/**
	 * 截取webView快照(webView加载的整个内容的大小)
	 * 
	 * @param webView
	 * @return
	 */
//	private Bitmap captureWebView(WebView webView) {
//		Picture snapShot = webView.capturePicture();
//
//		Bitmap bmp = Bitmap.createBitmap(snapShot.getWidth(),
//				snapShot.getHeight(), Bitmap.Config.RGB_565);
//		Canvas canvas = new Canvas(bmp);
//		snapShot.draw(canvas);
//		return bmp;
//	}

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
		if(!isPringting)
		{
			isPringting = true;
			downloadFile();
		}
	}
	
	private void print()
	{
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
			image = BitmapFactory.decodeStream(is, null,
					getBitmapOption(1));
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int[] data = getBitmapParamsData(image);
		mUsbDriver.write(PrintCmd.PrintDiskImagefile(data, width, heigh));

		mUsbDriver.write(PrintCmd.SetBold(0));
		mUsbDriver.write(PrintCmd.SetAlignment(1));
		mUsbDriver.write(PrintCmd.SetSizetext(1, 1));
		mUsbDriver
				.write(PrintCmd.PrintString(prescription.getHospital() + "处方笺\n\n", 0));
		CleanPrinter();

		mUsbDriver.write(PrintCmd.SetLinespace(10));
		mUsbDriver.write(PrintCmd.SetAlignment(2));
		mUsbDriver.write(PrintCmd.PrintString("处方编号：" + prescription.getPreNo() + "\n", 0));

		mUsbDriver.write(PrintCmd.SetAlignment(0));
		mUsbDriver.write(PrintCmd.PrintString("———————————————————————", 0));

		mUsbDriver.write(PrintCmd.PrintString("姓名：" + prescription.getName() + " 性别："
				+ prescription.getSex() + " 年龄：" + prescription.getAge() + " 岁\n", 0));
		mUsbDriver
				.write(PrintCmd.PrintString("科别：" + prescription.getKeshi() + "\n", 0));
		mUsbDriver.write(PrintCmd.PrintString("开 具 日 期：" + prescription.getDate()
				+ "\n", 0));

		mUsbDriver
				.write(PrintCmd.PrintString("电话：" + prescription.getPhone() + "\n", 0));

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
			image1 = BitmapFactory.decodeStream(is, null,
					getBitmapOption(1));
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bitmap image3 = null;
		try {
			InputStream is = am.open("yaoshi.png");
			image3 = BitmapFactory.decodeStream(is, null,
					getBitmapOption(1));
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//图片拼接
		Bitmap image11 = add2Bitmap(image1, image2);
		Bitmap image12 = add2Bitmap(image11, image3);
		Bitmap image13 = add2Bitmap(image12, image4);

		int[] data1 = getBitmapParamsData(image13);

		byte[] data12 = PrintCmd.PrintDiskImagefile(data1, width, heigh);
		mUsbDriver.write(data12);
		SetFeedCutClean(0);
		finish();
	}

	private Bitmap add2Bitmap(Bitmap first, Bitmap second) {
		int width = first.getWidth() + second.getWidth();
		int height = Math.max(first.getHeight(), second.getHeight());
		Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_4444);
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(first, 0, (height - first.getHeight())/2, null);
		canvas.drawBitmap(second, first.getWidth(), (height - second.getHeight())/2, null);
		return result;
	}
	
	int width, heigh;
	private int[] getBitmapParamsData(Bitmap bm) {
		if(bm != null){
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
		mUsbDriver.write(PrintCmd.PrintFeedline(5));      // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode));  // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean());            // 清理缓存
	}
	// 清理缓存，缺省模式
	private void CleanPrinter() {
		mUsbDriver.write(PrintCmd.SetBold(0));             // 粗体设置
		mUsbDriver.write(PrintCmd.SetAlignment(0));        // 对齐方式
		mUsbDriver.write(PrintCmd.SetSizetext(0, 0));      // 字符大小
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
		TipDialog dialog = new TipDialog(this,this);
		dialog.show("提示",sMsg.toString());
		return iRet;
	}

	@Override
	public void SetOnClick(View view) {
		//
	}
}
