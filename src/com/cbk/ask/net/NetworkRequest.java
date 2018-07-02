package com.cbk.ask.net;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

public class NetworkRequest<T> extends JsonRequest<JSONObject> {
	private final Map<String, String> mHeaders;
    public NetworkRequest(String url,
						   String requestBody, Listener<JSONObject> listener,
                           ErrorListener errorListener) {
    	super(Method.POST, url,requestBody, listener, errorListener);
    	mHeaders = new HashMap<String, String>();
    	mHeaders.put("Content-Type","application/json");
    	this.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, 1.0f));
    }

	@Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }
    
	@Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
        	String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
	        return Response.success(JSON.parseObject(json),
	                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

}