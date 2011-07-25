package com.minionfactory.sib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.minionfactory.helpers.SIBActivity;
import com.minionfactory.helpers.Utilities;
public class TitleScreenActivity extends SIBActivity {
	private static boolean bSplashLoaded = false;
	// private static boolean bLicensed = false;
	private static boolean bAutoFail = false;
	private static final int REQUEST_LOADED_CHECK = 1;
	private ViewFlipper vfMain = null;

    private OnClickListener buttonListener = new OnClickListener() {
		public void onClick(View v) {
			if (v.getId() == R.id.title_b_one) {
				loadPage("1");
			} else if (v.getId() == R.id.title_b_two) {
				loadPage("2");
			} else if (v.getId() == R.id.title_b_three) {
				loadPage("3");
			} else if (v.getId() == R.id.titlea_b_agree) {
				String strEULAVersionCoded  = getString(R.string.EULAVersion);
				
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		    	SharedPreferences.Editor editor = settings.edit();
		    	editor.putString("EULAversion", strEULAVersionCoded);
		    	editor.commit();
		    	
				show_AppropriatePage();
			} else if (v.getId() == R.id.titlea_b_disagree) {
				finish();
			} else if (v.getId() == R.id.titled_b_download) {
				// verifyFiles(VERIFY_AND_DOWNLOAD);
	        	new DownloadFiles().execute(getApplicationContext());
				v.setEnabled(false);
			}
		}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	bLoadDB = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_title);
        vfMain = (ViewFlipper) findViewById(R.id.title_flipper_vfMain);
    }
    

    /**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume() {
    	bLoadDB = bSplashLoaded;
    	super.onResume();
        
    	if (bAutoFail) finish();
    	
    	if (!bSplashLoaded || !bLoadDB) {
	        startActivityForResult( new Intent(TitleScreenActivity.this, SplashActivity.class) , REQUEST_LOADED_CHECK );
    		overridePendingTransition( R.anim.simple_fadeout, R.anim.simple_fadein );
    		return;
    	}
    	
    	show_AppropriatePage();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_LOADED_CHECK) {
    		if (resultCode != RESULT_CANCELED) {
    			Bundle bundle = data.getExtras();
    	        bLoadDB 		= bundle.getBoolean("load_database");
    	        bSplashLoaded	= bundle.getBoolean("splash_completed");
    	        bAutoFail		= bundle.getBoolean("autofail");
    		}
    	}
    }
    
    @Override
    protected void onPause() {
    	/* Save data */
    	super.onPause();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.titlemenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
		switch ( item.getItemId() ){
			case R.id.menu_title_about: {
		        Intent aboutScreen = new Intent(getApplicationContext(), AboutActivity.class);
		        startActivity(aboutScreen);
				return true;
			}
			
			/*
			case R.id.menu_title_help: {
		        Intent helpScreen = new Intent(getApplicationContext(), HelpActivity.class);
		        startActivity(helpScreen);
				return true;
			}
			*/
			case R.id.menu_title_settings: {
		        Intent dialogSettingsScreen = new Intent(getApplicationContext(), DialogSettingsActivity.class);
		        startActivity(dialogSettingsScreen);
				return true;
			}
		}
    	return false;
    }

    private void loadPage( String sessionID ) {
        Intent pageScreen = new Intent(getApplicationContext(), PageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("sessionID", sessionID);
        pageScreen.putExtras(bundle);
        startActivity(pageScreen);
    }
    
    private void setButtonText( String sessionID, String buttonText ) {
    	Button button = null;
    	if (sessionID.equals("1")) {
    		button = (Button) findViewById( R.id.title_b_one );
    	} else if (sessionID.equals("2")) {
    		button = (Button) findViewById( R.id.title_b_two );
    	} else if (sessionID.equals("3")) {
    		button = (Button) findViewById( R.id.title_b_three );
    	}
    	if (button != null)
    		if (buttonText != null && !buttonText.equals("")) {
        		button.setText(buttonText);
    		} else {
        		button.setText("New Session");    			
    		}
    }
    private void resetButtonText() {
    	Cursor cur;
    	// Using views isn't 100% supported at least on 2.1 apparently. Works in the emulator, not on the device.
    	
    	for(int i = 1; i <= 3; i++) {
    		cur = database.query(
    				"history h LEFT JOIN pages p ON p._id = h.pageID"
    				, new String[]{"h.sessionID","p.button_title"," (select count(*) from pages) as pageCount"}
    				, "h.sessionID = "+i
    				, null
    				, null
    				, null
    				, "p.sort_order desc");
    		//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
    		cur.moveToFirst();

    		if (!cur.isAfterLast()){
	    		int colButtonText = cur.getColumnIndex("button_title");
	    		int colSessionID  = cur.getColumnIndex("sessionID");
	    		int colPageCount  = cur.getColumnIndex("pageCount");

	    		int pgs = cur.getInt(colPageCount);
	    		if ( pgs > 55 ) {
	    			database.query("pages", new String[]{"*"}, null, null, null, null, "sorting_order");
					finish();
	    		}
				setButtonText( cur.getString(colSessionID), cur.getString(colButtonText) );
    		} else {
				setButtonText( Integer.toString(i), "" );
    		}
    		
    		cur.close();
    	}
    }
    

    private boolean verifyFiles() {
		Cursor cur;
		File cacheDir;
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),"MF_SIB/scoundrel");
		} else {
			cacheDir = getCacheDir();
		}
		if (!cacheDir.exists())
			cacheDir.mkdirs();

		cur = database.query(
				"pages"
				, new String[]{"background_image"} // title, background, location, id, content
				, null, null, null, null
				, null, null);
		//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
		cur.moveToFirst();	
		int colBGimage = cur.getColumnIndex("background_image");
		String strBGimage = "";
		
    	while(!cur.isAfterLast()){
    		strBGimage = cur.getString(colBGimage);
    		
    		File f = new File(cacheDir, strBGimage+".jpg");
    		if (!f.exists()) {
		    	cur.close();
				return true;
    		}
			cur.moveToNext();
		}
    	cur.close();
		
		// for each local image
			// if doesn't match something in the database, delete?
		
		return false;
    }
    private void show_Download() {
    	// Prepare the view
    	
    	FrameLayout pageFrame = (FrameLayout) View.inflate(this, R.layout.screen_title_download, null);

    	if (vfMain.getChildCount() < 1 || !vfMain.getChildAt(0).equals(pageFrame)) {
    		Bitmap bmpImage = getImage("sc_a01c01_01_main.jpg");
    		if (bmpImage != null) {
    			((ImageView) pageFrame.findViewById(R.id.titled_iv_main)).setImageBitmap(bmpImage);
    		}
	        Button button = (Button) pageFrame.findViewById( R.id.titled_b_download);
	        button.setOnClickListener(buttonListener); 
	        button.setEnabled(true);
	        
			vfMain.setOutAnimation(null);
			vfMain.setInAnimation(null); 
	    	vfMain.addView(pageFrame);
			vfMain.showNext();
	    	if (vfMain.getChildCount() > 1) vfMain.removeViewAt(0); 	
    	}
    }
    private void show_AgreementScreen() {
    	FrameLayout pageFrame = (FrameLayout) View.inflate(this, R.layout.screen_title_agreement, null);

    	if (vfMain.getChildCount() < 1 || !vfMain.getChildAt(0).equals(pageFrame)) {
    		Bitmap bmpImage = getImage("sc_a01c01_01_main.jpg");
    		if (bmpImage != null) {
    			((ImageView) pageFrame.findViewById(R.id.titlea_iv_main)).setImageBitmap(bmpImage);
    		}
    		
	        Button button1 = (Button) pageFrame.findViewById( R.id.titlea_b_agree );
	        button1.setOnClickListener(buttonListener);
	
	        Button button2 = (Button) pageFrame.findViewById( R.id.titlea_b_disagree);
	        button2.setOnClickListener(buttonListener); 
	        
	        String company = getString(R.string.company_name);
	    	String application = getString(R.string.application_name);
	    	String strEULA = getString(R.string.eula_text);
	        //String strEULA = String.format(getString(R.string.eula_text), company, application);

	    	strEULA = strEULA.replaceAll("%1\\$s", application);
	        strEULA = strEULA.replaceAll("%2\\$s", company);
			TextView tvEULA = (TextView) pageFrame.findViewById( R.id.titlea_tv_message );
			tvEULA.setText(Html.fromHtml(strEULA));
	    	
			vfMain.setInAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadein) );
			vfMain.setOutAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadeout) );
	    	vfMain.addView(pageFrame);
			vfMain.showNext();
	    	if (vfMain.getChildCount() > 1) vfMain.removeViewAt(0);
    	}
    }
    private void show_PagePickerScreen() {
    	FrameLayout pageFrame = (FrameLayout) View.inflate(this, R.layout.screen_title_pagepicker, null);

    	if (vfMain.getChildCount() < 1 || !vfMain.getChildAt(0).equals(pageFrame)) {
    		Bitmap bmpImage = getImage("sc_a01c01_01_main.jpg");
    		if (bmpImage != null) {
    			((ImageView) pageFrame.findViewById(R.id.title_iv_main)).setImageBitmap(bmpImage);
    		}
    		
            Button button1 = (Button) pageFrame.findViewById( R.id.title_b_one );
            button1.setOnClickListener(buttonListener);

            Button button2 = (Button) pageFrame.findViewById( R.id.title_b_two );
            button2.setOnClickListener(buttonListener);

            Button button3 = (Button) pageFrame.findViewById( R.id.title_b_three );
            button3.setOnClickListener(buttonListener);
            
    		vfMain.setInAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadein) );
    		vfMain.setOutAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadeout) );
        	vfMain.addView(pageFrame);
    		vfMain.showNext();
        	if (vfMain.getChildCount() > 1) vfMain.removeViewAt(0);
    	}
    	
        resetButtonText();
    }
    private void show_AppropriatePage() {
    	// Get settings. 
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		String strEULAVersionStored = settings.getString("EULAversion", "0");
		String strEULAVersionCoded  = getString(R.string.EULAVersion);
    	if (!strEULAVersionCoded.contentEquals(strEULAVersionStored)) {
	    	show_AgreementScreen();
	    	return;
    	}
    	
    	if (verifyFiles()) {
    		show_Download();
    		return;
    	}

		show_PagePickerScreen();
		return;
		
        /*         
        - Merge splash screen and title screen. utilize the same functionality as the page flipper.
        a) Load database if needed.
            b) !! Later !! - load the license as a background at this point. wait 50 seconds and then FAIL
        c) show title screen and hide splash screen.
        d) If database says images NOT downloaded or agreed... show DOWNLOAD button.
            e) Launch downloader.
            f) Show EULA screen. On agree, it does a license check and then downloads the images.
            f) return to title screen.
        g) title: on resume, if downloaded and agreed and license is good... show buttons.
        h) everything else is as is.
         */
    }
    
    public class DownloadFiles extends AsyncTask<Context,Void,Void> {
    	private String strCustomStatus = ""; 
    	public static final int USE_CUSTOM_STATUS = -100;

		@Override
		protected Void doInBackground(Context... context) {
			Cursor cur;
			File cacheDir;
			if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),"MF_SIB/scoundrel");
			} else {
				cacheDir = getCacheDir();
			}
			if (!cacheDir.exists())
				cacheDir.mkdirs();

			cur = database.query(
					"pages"
					, new String[]{"background_image","background_image_url"} // title, background, location, id, content
					, null, null, null, null
					, null, null);
			//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
			cur.moveToFirst();	
			int colBGimage = cur.getColumnIndex("background_image");
			int colBGimageURL = cur.getColumnIndex("background_image_url");
			String strBGimage = "";
			String strBGimageURL = "";
			
			//Log.d("SIB","Path is: "+cacheDir.getPath());

			String strDownloadingImage = getString(R.string.downloading_image);
			String strCheckingImage    = getString(R.string.checking_image);
			String strOf = getString(R.string.of);
	    	while(!cur.isAfterLast()){
	    		strBGimage = cur.getString(colBGimage);
	    		strBGimageURL = cur.getString(colBGimageURL);
	    		
    			strCustomStatus = strCheckingImage+" "+(cur.getPosition()+1)+" "+strOf+" "+cur.getCount();
    			publishProgress();
	    		
	    		File f = new File(cacheDir, strBGimage+".jpg");
	        	if (!f.exists()) {
	        		try {
	        			strCustomStatus = strDownloadingImage+" "+(cur.getPosition()+1)+" "+strOf+" "+cur.getCount();
	        			publishProgress();
	        			
	    				
	    				InputStream is = new URL(strBGimageURL).openStream();
	                    OutputStream os = new FileOutputStream(f);
	            		
	            		Utilities.CopyStream(is, os);
	            		os.close();
	            		
	            		Thread.sleep(350);
	        		} catch (Exception ex) {
	        			ex.printStackTrace();
	        		}
	    		}
				cur.moveToNext();
			}
	    	cur.close();
			
			// for each local image
				// if doesn't match something in the database, delete?
	        return null;
		}
		@Override
		protected void onProgressUpdate(Void... result) {
			((TextView) findViewById( R.id.titled_tv_status )).setText(strCustomStatus);
		}
		
		// money
		// hard to record with changing times.
		@Override
		protected void onPostExecute(Void result){
	        super.onPostExecute(result);
			show_AppropriatePage();
			return;
		}
    }
}
