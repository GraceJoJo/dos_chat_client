package com.cbk.ask.net;

import android.content.Context;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.cbk.ask.AskApplication;

public class CGNetworkHelper implements
		Response.Listener<JSONObject>, ErrorListener {
	private Context context;
	private int mFlag;
	//构造
	public CGNetworkHelper(Context context,int flag) {
		this.context = context;
		this.mFlag = flag;
	}

	protected Context getContext() {
		return context;
	}
	
	protected NetworkRequest getRequestForGet(String url) {
		return new NetworkRequest(url,null,this, this);
	}
	
	protected NetworkRequest getRequestForPost(String url,
			String jsonstr) {
		return new NetworkRequest(url, jsonstr, this, this);
	}

	public void sendGETRequest(String url) {
		AskApplication.getInstance().getRequestQueue().add(getRequestForGet(url));
	}

	public void sendPostRequest(String url, Object obj) {
		String jsonobj = JSON.toJSONString(obj);
		Log.d("sendPostRequest",url+"\n"+jsonobj);
		AskApplication.getInstance().getRequestQueue().add(getRequestForPost(url, jsonobj));
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		uiDataListener.onError(mFlag,"-1", error == null ? "NULL" : error.getMessage());
	}
	
	@Override
	public void onResponse(JSONObject response) {
		Log.d("sendResponse",response.toJSONString());
		uiDataListener.callback(mFlag,response);
	}

	private AppCallback<JSONObject> uiDataListener;

	public void setUiDataListener(AppCallback<JSONObject> uiDataListener) {
		this.uiDataListener = uiDataListener;
	}
}