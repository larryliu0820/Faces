package com.facepp.api;

import org.json.JSONException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.facepp.error.FaceppParseException;
import com.facepp.http.PostParameters;

import static com.facepp.picturedetect.ImageDisplayActivity.*;

public class RecognitionSearchRunnable extends ApiRunnable implements Runnable{

	public RecognitionSearchRunnable(Handler h) {
		super(h);
	}

	private static final String[] facesets = {"FamousInHistory","StoryStone"};
	
	public void run() {
		// TODO Auto-generated method stub
		Message message = handler.obtainMessage();
		Bundle bundle = message.getData();
		try {
			
			double maxSim = 0;
			int maxNum = 0;
			String bestFaceId = null;
			for(String faceset:facesets){
    			result=httpRequests.facesetGetInfo(new PostParameters().setFacesetName(faceset));
    			String facesetId = result.getString("faceset_id");
				result=httpRequests.recognitionSearch(
						new PostParameters().
						setFacesetId(facesetId).
						setKeyFaceId(faceId));
				System.out.println("verifying... ");
				
				int facesNumber= result.getJSONArray("candidate").length();
				double maxSimilarity = 0;
				int maxNumber = 0;
				for(int i = 0; i < facesNumber; i++){
					double currSimilarity = Double.parseDouble(result.
							getJSONArray("candidate").getJSONObject(i).getString("similarity"));
					if(maxSimilarity<currSimilarity){
						maxNumber = i;
						maxSimilarity = currSimilarity;
					}
				}
				if(maxSimilarity>maxSim) {
					maxSim = maxSimilarity;
					maxNum = maxNumber;
				}
				bestFaceId = result.getJSONArray("candidate").getJSONObject(maxNum).getString("face_id");
			}
			result=httpRequests.infoGetFace(new PostParameters().setFaceId(bestFaceId));
			System.out.println(result);
			
			String onlineFileName = result.
					getJSONArray("face_info").getJSONObject(0).
					getJSONArray("person").getJSONObject(0).
					getString("person_name");
			String[] elements = onlineFileName.split("_");
			String name = elements[1];
			
			String fileName = "face_"+elements[0]+elements[2].split("[.]")[0];
			
			message.what = MSG_SEARCH_SUCCESS;
			
			bundle.putString(PERSON_NAME, name);
			bundle.putString(FILE_NAME, fileName);
			handler.sendMessage(message);
		} catch (FaceppParseException e) {
			message.what = MSG_SEARCH_FAILURE;
			bundle.putString("failure_reason", e.getMessage());
			handler.sendMessage(message);
		} catch (JSONException e) {
			message.what = MSG_SEARCH_FAILURE;
			bundle.putString("failure_reason", e.getMessage());
			handler.sendMessage(message);
		}
	}

}
