package com.facepp.api;
import org.json.JSONException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.facepp.error.FaceppParseException;
import com.facepp.http.PostParameters;

import static com.facepp.picturedetect.ImageDisplayActivity.*;

public class PersonCreateRunnable extends ApiRunnable implements Runnable{

	public PersonCreateRunnable(Handler h) {
		super(h);
	}

	public void run() {
		Message message = handler.obtainMessage();
		Bundle bundle = message.getData();
		try {
			int numOfFaces = result.getJSONArray("face").length();
			if(numOfFaces>1){
				message.what = MSG_CREATE_FAILURE;
				bundle.putString(FAILURE_REASON, "More than 1 face.");
				return;
			}
			
			String personName = "person_1";
			PostParameters params = new PostParameters().setPersonName(personName).setFaceId(faceId);
			try{
				result=httpRequests.personCreate(params);
			}catch(FaceppParseException e) {
				httpRequests.personDelete(params);
				result=httpRequests.personCreate(params);
			}

			System.out.println("person create response: "+result);

			result=httpRequests.trainVerify(new PostParameters().setPersonName(personName));
			
			System.out.println(httpRequests.getSessionSync(result.get("session_id").toString()));

			message.what = MSG_CREATE_SUCCESS;
			
			handler.sendMessage(message);
		} catch (FaceppParseException e) {
			message.what = MSG_CREATE_FAILURE;
			bundle.putString("failure_reason", e.getMessage());
			handler.sendMessage(message);
		} catch (JSONException e) {
			message.what = MSG_CREATE_FAILURE;
			bundle.putString("failure_reason", e.getMessage());
			handler.sendMessage(message);
		}
	}

}
