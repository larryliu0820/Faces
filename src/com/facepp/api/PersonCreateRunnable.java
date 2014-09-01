package com.facepp.api;

import org.json.JSONException;

import com.facepp.error.FaceppParseException;
import com.facepp.http.PostParameters;

public class PersonCreateRunnable extends Api implements Runnable{

	public void run() {
		// TODO Auto-generated method stub
		try {
			int numOfFaces = result.getJSONArray("face").length();
			if(numOfFaces>1){

			}
			//create a person
			
			String personName = "person_1";
			PostParameters params = new PostParameters().setPersonName(personName).setFaceId(faceId);
			try{
				result=httpRequests.personCreate(params);
			}catch(FaceppParseException e) {
				e.printStackTrace();
				System.out.println("NAME_EXIST");
				httpRequests.personDelete(params);
				result=httpRequests.personCreate(params);
			}

			System.out.println("person create response: "+result);

			result=httpRequests.trainVerify(new PostParameters().setPersonName(personName));
			
			System.out.println(httpRequests.getSessionSync(result.get("session_id").toString()));

		} catch (FaceppParseException e) {

		} catch (JSONException e) {

		}
	}

}
