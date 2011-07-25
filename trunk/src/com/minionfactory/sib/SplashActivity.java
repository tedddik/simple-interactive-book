package com.minionfactory.sib;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.minionfactory.helpers.SIBActivity;
import com.minionfactory.helpers.DataBaseHelper;

public class SplashActivity extends SIBActivity {
	private boolean bLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_splash);
    	bLoadDB = false;
        
        // Animation is controlled by the setting of the device.
        
		FrameLayout llMain = (FrameLayout) findViewById(R.id.splash_fl_root);
		
		llMain.setOnClickListener(
    			new View.OnClickListener() {
		    		public void onClick(View v) {
		    			if (!bLoaded) return;
		    			
				        Bundle bundle = new Bundle();
				        bundle.putBoolean("autofail", false);
				        bundle.putBoolean("splash_completed", true);
				        bundle.putBoolean("load_database", true);
				        setResult(RESULT_OK, (new Intent()).putExtras(bundle) );
		    			finish();
		    		}
		        }
			);
        
        new LoadResources().execute(this);
    }
        
    public class LoadResources extends AsyncTask<Context,Integer,Void> {
    	private String strCustomStatus = ""; 
    	public static final int USE_CUSTOM_STATUS = -100;

		@Override
		protected Void doInBackground(Context... context) {
	        try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// e.printStackTrace();
			}

			// Get the start time for Logo delay.
			Long lnStartTime = System.nanoTime();
			
			publishProgress(R.string.loading_database);
			//initDB( context[0], Globals.NAME_DB, R.integer.databaseVersion, R.raw.sib_scoundrel, false );
			initDB( context[0], Globals.NAME_DB, R.string.databaseVersion, R.raw.sib_scoundrel, true ); 

			
		    DataBaseHelper databaseHelper = new DataBaseHelper( context[0], Globals.NAME_DB, R.string.databaseVersion, -1);
	        //databaseHelper = new DataBaseHelper( this, Globals.NAME_DB, R.integer.databaseVersion, -1);
			try {
				SQLiteDatabase database = databaseHelper.getWritableDatabase();
				
				database.close(); 
			} catch (Exception e) {
				e.printStackTrace();
				try {
					database.close();
					databaseHelper.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			
			
			publishProgress(R.string.loading_clickskip);
			bLoaded = true;
			
			//getDatabase = true;
			// This will skip the rest of the loading page from this moment on.

			publishProgress(R.string.loading_complete);
			// make sure splash screen shows for at least 4 seconds.
			Long lnEndTime = System.nanoTime();
			Long lnSleepTime = 4000 - (lnEndTime - lnStartTime) / 1000000;
			if (lnSleepTime < 500) lnSleepTime = ((Integer) 500).longValue();

	        try {
				Thread.sleep(lnSleepTime);
			} catch (InterruptedException e) {
				// e.printStackTrace();
			}

	        Bundle bundle = new Bundle();
	        bundle.putBoolean("autofail", false);
	        bundle.putBoolean("splash_completed", true);
	        bundle.putBoolean("load_database", true);
	        setResult(RESULT_OK, (new Intent()).putExtras(bundle) );
	        finish();
	        
	        return null;
		}
    	

		@Override
		protected void onProgressUpdate(Integer... values) {
			TextView loadingSplashText = (TextView) findViewById( R.id.splash_tv_loading_current );
			if (values[0] == USE_CUSTOM_STATUS)
				loadingSplashText.setText(strCustomStatus);
			else 
				loadingSplashText.setText(values[0]);
			//super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Void result){
	        super.onPostExecute(result);
	        finish();
	        overridePendingTransition(R.anim.simple_fadein, R.anim.simple_fadeout);
			return;
		}
		
		protected void initDB ( Context context, String dbName, int intVersion, int intDB, boolean preserveData ) {
			SQLiteDatabase myDb = null;
			DataBaseHelper myDbHelper = new DataBaseHelper( context, dbName, intVersion, intDB);
			
			/*
			 * The database must be initialized before it can be used. 
			 * This will ensure that the database exists and is the 
			 * current version.
			 */
			myDbHelper.initializeDatabase(preserveData);
			
			try {
				// A reference to the database can be obtained after initialization.
				myDb = myDbHelper.getWritableDatabase();
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					myDbHelper.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					myDb.close();
				}
			}
		}
    }
}
