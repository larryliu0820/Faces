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

	final private static String TAG = "MainActivity";
	final private int TAKE_PICTURE = 2;
	final private int PICTURE_CHOOSE = 1;
	
	private State currentState = State.RUN;
	private Bitmap tempImg;
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
        		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        		File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        	    intent.putExtra(MediaStore.EXTRA_OUTPUT,
        	            Uri.fromFile(photo));
        	    imageUri = Uri.fromFile(photo);
				
				startActivityForResult(intent,TAKE_PICTURE);
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
    	
    	//the image picker callback
    	switch(requestCode){
    	case PICTURE_CHOOSE:
    	{
    		if (intent != null) {
    
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
    			
    			buttonGetImage.setVisibility(View.INVISIBLE);
    	    	buttonPhoto.setVisibility(View.INVISIBLE);
    			
    		}
    		else {
    			Log.d(TAG, "idButSelPic Photopicker canceled");
    		}
    		break;
    	}
    	case TAKE_PICTURE:
    	{
    		if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = imageUri;
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
                buttonGetImage.setVisibility(View.INVISIBLE);
            	buttonPhoto.setVisibility(View.INVISIBLE);
            }			
    		break;
    	}
    	}
    	Intent imageDisplayIntent = new Intent(this, ImageDisplayActivity.class);
    	startActivity(imageDisplayIntent);
    }
    
}
