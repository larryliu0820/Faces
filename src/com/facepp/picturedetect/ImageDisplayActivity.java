package com.facepp.picturedetect;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.photoview.PhotoViewAttacher;

import com.facepp.api.ApiRunnable;
import com.facepp.api.DetectionDetectRunnable;
import com.facepp.api.PersonCreateRunnable;
import com.facepp.api.RecognitionSearchRunnable;
import com.facepp.http.HttpRequests;
import com.facpp.picturedetect.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import static com.facepp.picturedetect.MainActivity.*;

public class ImageDisplayActivity extends Activity {

	private final static String TAG = "ImageDisplayActivity";
	
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
			
	private final HttpRequests httpRequests = new HttpRequests(
			"7ce635b3cc93ae431de9c82174082905", 
			"n4-z3AY6ZbVbGoEFZverm00nVgI5I_Wt", false, false);

	public static final int MSG_DETECT_SUCCESS = 100;
	public static final int MSG_DETECT_FAILURE = 101;
	public static final int MSG_CREATE_SUCCESS = 200;
	public static final int MSG_CREATE_FAILURE = 201;
	public static final int MSG_SEARCH_SUCCESS = 300;
	public static final int MSG_SEARCH_FAILURE = 301;
	public static final String FAILURE_REASON = "failure_reason";
	public static final String PERSON_NAME = "person_name";
	public static final String FILE_NAME = "person_face";
	
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
        
        Intent intent = getIntent();
        if(intent != null) {
        	int requestCode = intent.getIntExtra(REQUEST_CODE, 0);
        	switch(requestCode) 
        	{
        	case PICTURE_CHOOSE:
	    	{
	    
    			Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);
    			cursor.moveToFirst();
    			int idx = cursor.getColumnIndex(ImageColumns.DATA);
    			String fileSrc = cursor.getString(idx); 
    
    			Options options = new Options();
    			options.inJustDecodeBounds = true;
    			tempImg = BitmapFactory.decodeFile(fileSrc, options);

    			options.inSampleSize = Math.max(1, (int)Math.ceil(Math.max((double)options.outWidth / 1024f, (double)options.outHeight / 1024f)));
    			options.inJustDecodeBounds = false;
    			tempImg = BitmapFactory.decodeFile(fileSrc, options);

	    		break;
	    	}
	    	case TAKE_PICTURE:
	    	{
                Uri selectedImage = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                getContentResolver().notifyChange(selectedImage, null);
                ContentResolver cr = getContentResolver();
                try {
                	tempImg = android.provider.MediaStore.Images.Media
                     .getBitmap(cr, selectedImage);

                    Toast.makeText(this, selectedImage.toString(),
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                            .show();
                    Log.e("Camera", e.toString());
                }
	    		break;
	    	}
        	}
        }
        
        textView = (TextView)this.findViewById(R.id.textView1);
        imageView = (ImageView)this.findViewById(R.id.imageView1);
        imageView.setImageBitmap(tempImg);
        
        attacher = new PhotoViewAttacher(imageView);
        
        buttonVerify = (Button)this.findViewById(R.id.button5);
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
				
        		mHandler = new Handler(Looper.getMainLooper()) {
					@Override
					public void handleMessage(Message inputMessage) {
						if(inputMessage.what == MSG_DETECT_SUCCESS) {
							Bundle bundle = inputMessage.getData();
							tempId = bundle.getString(ApiRunnable.FACE_ID);
							try {
								jsonResponse = new JSONObject(bundle.getString(ApiRunnable.JSON));
								findFaces(jsonResponse, tempImg);
							} catch (JSONException e) {
								textView.setText("Network error.");
								e.printStackTrace();
							}
							
						}
						else
							textView.setText("Detect failed."+inputMessage.getData().getString("failure_reason"));
					}
				};
				
				final DetectionDetectRunnable detectionDetect = new DetectionDetectRunnable(mHandler);
				detectionDetect.
					setBitmap(tempImg).
					setContext(getApplicationContext()).
					setFaceId(tempId).
					setHttpRequests(httpRequests);
				Thread detectThread = new Thread() {
					@Override
					public void run() {
						detectionDetect.run();
					}
				};
				detectThread.start();
          	}
        });
      
        buttonRerun.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(currentState.equals(State.RERUN))
					clear();
			}
		});
        
        //imageView.setOnTouchListener(new TouchZoomListener());
    }


	public void findFaces(final JSONObject rst, final Bitmap image) {
		//Log.i(TAG, rst.toString());
		Thread thread = new Thread() {
			@Override
			public void run() {
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
						}
					});
					
				} catch (JSONException e) {
					textView.setText("Error.");
				}
			}
		};
		thread.start();
		create();
	}
    	
    public void create() {
    	ImageDisplayActivity.this.runOnUiThread(new Runnable() {
			
			public void run() {
				textView.setText("Creating...");
				
			}
		});
		
    	mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message inputMessage) {
				if(inputMessage.what == MSG_CREATE_SUCCESS) {
					search();
				}else
					textView.setText("Detect failed."+inputMessage.getData().getString("failure_reason"));
			}
		};
		
		final PersonCreateRunnable personCreate = new PersonCreateRunnable(mHandler);
		personCreate.setContext(getApplicationContext()).
			setResult(jsonResponse).
			setFaceId(tempId).
			setHttpRequests(httpRequests);
		Thread thread = new Thread() {
			@Override
			public void run() {
				personCreate.run();
			}
		};
		thread.start();
		
    }
    
    public void search() {
    	ImageDisplayActivity.this.runOnUiThread(new Runnable() {
			
			public void run() {
				textView.setText("Searching...");
				buttonVerify.setVisibility(View.INVISIBLE);
				buttonRerun.setVisibility(View.VISIBLE);
			}
		});
		
    	mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message inputMessage) {
				if(inputMessage.what == MSG_SEARCH_SUCCESS) {
					Bundle bundle = inputMessage.getData();
					String personName = bundle.getString(PERSON_NAME);
					String fileName = bundle.getString(FILE_NAME);
					textView.setText("你的前世是：" + personName + "，好犀利！");
					Drawable famousFace = getResources().getDrawable(getResources()
			                  .getIdentifier(fileName, "drawable", getPackageName()));
					imageView.setImageDrawable(famousFace);
					attacher.update();
					
					buttonVerify.setVisibility(View.INVISIBLE);
					buttonRerun.setVisibility(View.VISIBLE);
					
					currentState = State.RERUN;
				}else
					textView.setText("Detect failed."+inputMessage.getData().getString("failure_reason"));
			}
		};
		
		final RecognitionSearchRunnable recognitionSearch = new RecognitionSearchRunnable(mHandler);
		recognitionSearch.
			setContext(getApplicationContext()).
			setFaceId(tempId).
			setHttpRequests(httpRequests);
		Thread thread = new Thread() {
			@Override
			public void run() {
				recognitionSearch.run();
			}
		};
		thread.start();
		
    }
    
    public void clear() {
		buttonVerify.setVisibility(View.VISIBLE);
		buttonRerun.setVisibility(View.INVISIBLE);
		imageView.setImageDrawable(null);
		textView.setText("Click verify -->");
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
