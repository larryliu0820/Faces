package com.facepp.api;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.PostParameters;
import com.facepp.picturedetect.State;
import static com.facepp.picturedetect.ImageDisplayActivity.*;

public class DetectionDetectRunnable extends ApiRunnable implements Runnable{

	public DetectionDetectRunnable(Handler h) {
		super(h);
	}

	private State currentState;
	
	private Bitmap bitmap;
	private final static String TAG = "DetectionDetectRunnable";

	
	
	public void run() {
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		float scale = Math.min(1, Math.min(600f / bitmap.getWidth(), 600f / bitmap.getHeight()));
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		Bitmap imgSmall = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		
		
		imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] array = stream.toByteArray();
		//Log.v(TAG, "imgSmall size : " + imgSmall.getWidth() + " " + imgSmall.getHeight());
		Message message = handler.obtainMessage();
		Bundle bundle = message.getData();
		try {
			//detect
			result = httpRequests.detectionDetect(new PostParameters().setImg(array));
			
			faceId = result.getJSONArray("face").getJSONObject(0).getString("face_id");			
			
			message.what = MSG_DETECT_SUCCESS;
			
			bundle.putString(FACE_ID, faceId);
			bundle.putString(JSON, result.toString());
			
			handler.sendMessage(message);
			
		} catch (FaceppParseException e) {
			message.what = MSG_DETECT_FAILURE;
			bundle.putString(FAILURE_REASON, e.getMessage());
			handler.sendMessage(message);
		} catch (JSONException e) {
			message.what = MSG_DETECT_FAILURE;
			bundle.putString(FAILURE_REASON, e.getMessage());
			handler.sendMessage(message);
		}
		
	}
	
	public State getCurrentState() {
		return currentState;
	}

	public DetectionDetectRunnable setCurrentState(State currentState) {
		this.currentState = currentState;
		return this;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public DetectionDetectRunnable setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		return this;
	}

}
