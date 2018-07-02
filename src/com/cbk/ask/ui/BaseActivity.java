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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.cbk.ask.utils.CustomToast;
import com.cbk.ask.widget.CustomProgressDialog;
import com.hyphenate.easeui.ui.EaseBaseActivity;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("Registered")
public class BaseActivity extends EaseBaseActivity {
	
	private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // umeng
        MobclickAgent.onResume(this);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(progressDialog!=null){
			progressDialog.dismiss();
			progressDialog = null;
		}
    }

    @Override
    protected void onStart() {
        super.onStart();
        // umeng
        MobclickAgent.onPause(this);
    }
    
    public void showToast(String str) {
    	CustomToast customToast = new CustomToast(this, str, Toast.LENGTH_LONG);
		customToast.show();
	}
    
    public void showProgress(String context){
		if(progressDialog==null){
			progressDialog = CustomProgressDialog.createDialog(this);
		}
		progressDialog.setMessage(context);
		if(!progressDialog.isShowing())
			progressDialog.show();
	}
	
	public void dismissProgress(){
		if(progressDialog!=null){
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

}
