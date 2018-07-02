package com.cbk.ask.receiver;

import com.cbk.ask.service.FxService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LaunchReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		 Intent intent1 = new Intent(context , FxService.class);  
	     // ����ָ��Server  
	     context.startService(intent1);  		
	}

}
