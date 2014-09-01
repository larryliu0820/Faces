package com.facepp.api;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.http.HttpRequests;

public class Api {
    protected HttpRequests httpRequests;
	
    protected Context context;
	
    protected String faceId;
	
    protected JSONObject result;
	
    public static final int TASK_COMPLETE = 0;
    public static final int NETWORK_ERR = 1;
    public static final int INPUT_ERR = 2;
    
	public HttpRequests getHttpRequests() {
		return httpRequests;
	}
	public Api setHttpRequests(HttpRequests httpRequests) {
		this.httpRequests = httpRequests;
		return this;
	}
	public Context getContext() {
		return context;
	}
	public Api setContext(Context context) {
		this.context = context;
		return this;
	}
	public String getFaceId() {
		return faceId;
	}
	public Api setFaceId(String faceId) {
		this.faceId = faceId;
		return this;
	}
	public JSONObject getResult() {
		return result;
	}
	public Api setResult(JSONObject result) {
		this.result = result;
		return this;
	}
}
