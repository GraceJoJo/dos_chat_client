package com.cbk.ask.net;


public interface AppCallback<JSONObject> {
	void callback(int Flag,JSONObject jsonobj);
	void onError(int Flag,String errorCode, String errorMessage);
}
