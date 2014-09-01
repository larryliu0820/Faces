package com.facepp.picturedetect;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.photoview.PhotoViewAttacher;

import com.facepp.api.DetectionDetectRunnable;
import com.facepp.api.PersonCreateRunnable;
import com.facepp.api.RecognitionSearchRunnable;
import com.facepp.http.HttpRequests;
import com.facpp.picturedetect.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageDisplayActivity extends Activity {

	final private static String TAG = "ImageDisplayActivity";
	
	private State currentState = State.RUN;
	private ImageView imageView;
	private Bitmap tempImg;
	private Button buttonVerify;
	private Button buttonRerun;
	private TextView textView;
	private JSONObject jsonResponse;
	
	public PhotoViewAttacher attacher;
	
	private String tempId = null;
	
	private Handler mHandler;
		
	final private HttpRequests httpRequests = new HttpRequests(
			"7ce635b3cc93ae431de9c82174082905", 
			"n4-z3AY6ZbVbGoEFZverm00nVgI5I_Wt", false, false);

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_display, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);
        
        buttonVerify =(Button)this.findViewById(R.id.button5);
        buttonRerun = (Button)this.findViewById(R.id.button2);
        
        buttonVerify.setVisibility(View.VISIBLE);
        buttonRerun.setVisibility(View.INVISIBLE);
		 
        
        buttonVerify.setOnClickListener(new OnClickListener() {
        	public void onClick(View arg0){
        		if(currentState.compareTo(State.RUN) != 0)
					return;
        		currentState = State.RUNNING;
        		ImageDisplayActivity.this.runOnUiThread(new Runnable() {
        			
        			public void run() {
        				textView.setText("Detecting...");
        				
        			}
        		});
				
				DetectionDetectRunnable detectionDetect = new DetectionDetectRunnable();
				detectionDetect.
					setBitmap(tempImg).
					setContext(getApplicationContext()).
					setFaceId(tempId).
					setHttpRequests(httpRequests);
				detectionDetect.run();
				
				mHandler = new Handler(Looper.getMainLooper()) {
					@Override
					public void handleMessage(Message inputMessage) {
						
					}
				};
          	}
        });
      
        buttonRerun.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(currentState.equals(State.RERUN))
					clear();
			}
		});
        textView = (TextView)this.findViewById(R.id.textView1);
        imageView = (ImageView)this.findViewById(R.id.imageView1);
        imageView.setImageBitmap(tempImg);
        
        attacher = new PhotoViewAttacher(imageView);
        //imageView.setOnTouchListener(new TouchZoomListener());
    }

    public class Callback {
    	public void getResult(JSONObject rst, Bitmap image) {
			//Log.i(TAG, rst.toString());
			
			//use the red paint
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStrokeWidth(Math.max(image.getWidth(), image.getHeight()) / 100f);

			//create a new canvas
			Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(image, new Matrix(), null);
			
			
			try {
				//find out all faces
				final int count = rst.getJSONArray("face").length();
				for (int i = 0; i < count; ++i) {
					float x, y, w, h;
					//get the center point
					x = (float)rst.getJSONArray("face").getJSONObject(i)
							.getJSONObject("position").getJSONObject("center").getDouble("x");
					y = (float)rst.getJSONArray("face").getJSONObject(i)
							.getJSONObject("position").getJSONObject("center").getDouble("y");

					//get face size
					w = (float)rst.getJSONArray("face").getJSONObject(i)
							.getJSONObject("position").getDouble("width");
					h = (float)rst.getJSONArray("face").getJSONObject(i)
							.getJSONObject("position").getDouble("height");
					
					//change percent value to the real size
					x = x / 100 * image.getWidth();
					w = w / 100 * image.getWidth() * 0.7f;
					y = y / 100 * image.getHeight();
					h = h / 100 * image.getHeight() * 0.7f;

					//draw the box to mark it out
					canvas.drawLine(x - w, y - h, x - w, y + h, paint);
					canvas.drawLine(x - w, y - h, x + w, y - h, paint);
					canvas.drawLine(x + w, y + h, x - w, y + h, paint);
					canvas.drawLine(x + w, y + h, x + w, y - h, paint);
				}
				
				//save new image
				tempImg = bitmap;
				
				
				
				ImageDisplayActivity.this.runOnUiThread(new Runnable() {
					
					public void run() {
						//show the image
						imageView.setImageBitmap(tempImg);
						attacher.update();
						textView.setText("Finished, "+ count + " faces.");
					}
				});
				
			} catch (JSONException e) {
				e.printStackTrace();
				ImageDisplayActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						textView.setText("Error.");
					}
				});
			}
			
			
		}
	}

    public void create() {
    	ImageDisplayActivity.this.runOnUiThread(new Runnable() {
			
			public void run() {
				textView.setText("Creating...");
				
			}
		});
		
		PersonCreateRunnable personCreate = new PersonCreateRunnable();
		personCreate.setContext(getApplicationContext()).
			setResult(jsonResponse).
			setFaceId(tempId).
			setHttpRequests(httpRequests);
		personCreate.run();
    }
    
    public void search() {
    	ImageDisplayActivity.this.runOnUiThread(new Runnable() {
			
			public void run() {
				textView.setText("Searching...");
				buttonVerify.setVisibility(View.INVISIBLE);
				buttonRerun.setVisibility(View.VISIBLE);
			}
		});
		
		RecognitionSearchRunnable recognitionSearch = new RecognitionSearchRunnable();
		recognitionSearch.
			setContext(getApplicationContext()).
			setFaceId(tempId).
			setHttpRequests(httpRequests);
		recognitionSearch.run();
		
    }
    
    public void clear() {
    	currentState = State.RUN;
		buttonVerify.setVisibility(View.INVISIBLE);
		buttonRerun.setVisibility(View.INVISIBLE);
		imageView.setImageDrawable(null);
    }
	public JSONObject getJsonResponse() {
		return jsonResponse;
	}

	public void setJsonResponse(JSONObject jsonResponse) {
		this.jsonResponse = jsonResponse;
	}

	public String getTempId() {
		return tempId;
	}

	public void setTempId(String tempId) {
		this.tempId = tempId;
	}

	public State getCurrentState() {
		return currentState;
	}

	public void setCurrentState(State currentState) {
		this.currentState = currentState;
	}
}
