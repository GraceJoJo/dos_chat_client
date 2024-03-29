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
package com.cbk.ask;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

public class AskApplication extends Application {

	public static Context applicationContext;
	private static AskApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";
	private RequestQueue mRequestQueue;
	
	/**
	 * nickname for current user, the nickname instead of ID be shown when user receive notification from APNs
	 */
	public static String currentUserNick = "";

	@Override
	public void onCreate() {
		MultiDex.install(this);
		super.onCreate();
        applicationContext = this;
        instance = this;
        
        //init demo helper
        AskHelper.getInstance().init(applicationContext);
		//red packet code : 初始化红包上下文，开启日志输出开关
//		RedPacket.getInstance().initContext(applicationContext);
//		RedPacket.getInstance().setDebugMode(true);
		//end of red packet code
	}

	public static AskApplication getInstance() {
		return instance;
	}
	
	/**
	 * 获取RequestQueue队列对象
	 * @return
	 */
	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null)
			mRequestQueue = Volley.newRequestQueue(this);
		return mRequestQueue;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
}
