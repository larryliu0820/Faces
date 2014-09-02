package com.facepp.picturedetect;

import java.io.File;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.facpp.picturedetect.R;

/**
 * A simple demo, get a picture form your phone<br />
 * Use the facepp api to detect<br />
 * Find all face on the picture, and mark them out.
 * @author moon5ckq
 */
public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	public static final int TAKE_PICTURE = 2;
	public static final int PICTURE_CHOOSE = 1;
	
	public static final String KEY_IMAGE="image";
	public static final String REQUEST_CODE="request_code";
	
	private State currentState = State.RUN;
	private Button buttonGetImage;
	private Button buttonPhoto;
		
	
	private Uri imageUri;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        buttonGetImage = (Button)this.findViewById(R.id.button1);
        buttonPhoto =(Button)this.findViewById(R.id.button4);
		 
        buttonGetImage.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				//get a picture form your phone
				System.out.println("state = "+currentState);
				if(currentState.compareTo(State.RUN) != 0)
					return;
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		        photoPickerIntent.setType("image/*");
		        startActivityForResult(photoPickerIntent, PICTURE_CHOOSE);
			}
		});
        
        buttonPhoto.setOnClickListener(new OnClickListener() {
        	public void onClick(View arg0){
        		if(currentState.compareTo(State.RUN) != 0)
					return;
        		Intent imageCaptureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        		File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        		imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
        	            Uri.fromFile(photo));
        	    imageUri = Uri.fromFile(photo);
				
				startActivityForResult(imageCaptureIntent,TAKE_PICTURE);
        	}
        });
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	    	
    	Intent imageDisplayIntent = new Intent(this, ImageDisplayActivity.class);
    	imageDisplayIntent.putExtra(REQUEST_CODE, requestCode);
		
    	//the image picker callback
		if (resultCode == Activity.RESULT_OK) {
			switch(requestCode) {
			case PICTURE_CHOOSE:
				imageDisplayIntent.setData(intent.getData());
				break;
			case TAKE_PICTURE:
				imageDisplayIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				break;
			}
	    	startActivity(imageDisplayIntent);
		}
    }
    
}
