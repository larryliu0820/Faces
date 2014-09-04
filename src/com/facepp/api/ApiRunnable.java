package com.facepp.api;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.facepp.http.HttpRequests;

public class ApiRunnable {
	protected Handler handler;
	
    protected HttpRequests httpRequests;
	
    protected Context context;
	
    protected String faceId;
	
    protected JSONObject result;
	
    public static final int TASK_COMPLETE = 0;
    public static final int NETWORK_ERR = 1;
    public static final int INPUT_ERR = 2;
    
    public static final String FACE_ID = "FACE_ID";
    public static final String JSON = "JSON";
    
    public ApiRunnable(Handler h) {
    	setHandler(h);
    }
    
	public HttpRequests getHttpRequests() {
		return httpRequests;
	}
	public ApiRunnable setHttpRequests(HttpRequests httpRequests) {
		this.httpRequests = httpRequests;
		return this;
	}
	public Context getContext() {
		return context;
	}
	public ApiRunnable setContext(Context context) {
		this.context = context;
		return this;
	}
	public String getFaceId() {
		return faceId;
	}
	public ApiRunnable setFaceId(String faceId) {
		this.faceId = faceId;
		return this;
	}
	public JSONObject getResult() {
		return result;
	}
	public ApiRunnable setResult(JSONObject result) {
		this.result = result;
		return this;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
}
