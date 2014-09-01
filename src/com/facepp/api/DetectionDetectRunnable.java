package com.facepp.api;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.View;

import com.facepp.error.FaceppParseException;
import com.facepp.http.PostParameters;
import com.facepp.picturedetect.State;

public class DetectionDetectRunnable extends Api implements Runnable{

	private State currentState;
	
	private Bitmap bitmap;

	public void run() {
		//Log.v(TAG, "image size : " + img.getWidth() + " " + img.getHeight());
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		float scale = Math.min(1, Math.min(600f / bitmap.getWidth(), 600f / bitmap.getHeight()));
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		Bitmap imgSmall = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		//Log.v(TAG, "imgSmall size : " + imgSmall.getWidth() + " " + imgSmall.getHeight());
		
		imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] array = stream.toByteArray();
		try {
			//detect
			result = httpRequests.detectionDetect(new PostParameters().setImg(array));
			//finished , then call the callback function
			
			faceId = result.getJSONArray("face").getJSONObject(0).getString("face_id");
			System.out.println("faceId = "+faceId);
		} catch (FaceppParseException e) {
			
		} catch (JSONException e) {
			
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
