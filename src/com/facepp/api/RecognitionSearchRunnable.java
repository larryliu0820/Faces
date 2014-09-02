package com.facepp.api;

import org.json.JSONException;

import android.graphics.drawable.Drawable;
import android.os.Handler;

import com.facepp.error.FaceppParseException;
import com.facepp.http.PostParameters;

public class RecognitionSearchRunnable extends ApiRunnable implements Runnable{

	public RecognitionSearchRunnable(Handler h) {
		super(h);
	}

	private static final String[] facesets = {"FamousInHistory","StoryStone"};
	
	public void run() {
		// TODO Auto-generated method stub
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
				System.out.println("bestFaceId: "+bestFaceId);
			}
			result=httpRequests.infoGetFace(new PostParameters().setFaceId(bestFaceId));
			System.out.println(result);
			
			String onlineFileName = result.
					getJSONArray("face_info").getJSONObject(0).
					getJSONArray("person").getJSONObject(0).
					getString("person_name");
			String[] elements = onlineFileName.split("_");
			final String name = elements[1];
			
			String fileName = "face_"+elements[0]+elements[2].split("[.]")[0];
			final Drawable famousFace = context.getResources().getDrawable(context.getResources()
	                  .getIdentifier(fileName, "drawable", context.getPackageName()));
			
			
		} catch (FaceppParseException e) {

		} catch (JSONException e) {

		}
	}

}
