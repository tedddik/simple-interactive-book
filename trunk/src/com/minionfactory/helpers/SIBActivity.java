package com.minionfactory.helpers;

import java.io.File;

import com.minionfactory.sib.Globals;
import com.minionfactory.sib.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

public class SIBActivity extends Activity {
	protected static final int FONT_TEXT = 0; 
	protected static final int FONT_LOCATION = 1; 
	protected static final int FONT_TITLE = 2; 
	protected static final int FONT_HEADER = 3;
	
    public static final String PREFS_NAME = "SIBScoundrelPrefsFile";
	protected boolean bLoadDB = true; 
	
    protected static SQLiteDatabase database;
    private static DataBaseHelper databaseHelper;

    @Override
    protected void onResume() {
        super.onResume();
        //if (getDatabase) getDB();
        
        if (bLoadDB) {
	        databaseHelper = new DataBaseHelper( this, Globals.NAME_DB, R.string.databaseVersion, -1);
	        //databaseHelper = new DataBaseHelper( this, Globals.NAME_DB, R.integer.databaseVersion, -1);
			try {
				database = databaseHelper.getWritableDatabase();
			} catch (Exception e) {
				e.printStackTrace();
				try {
					databaseHelper.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
        }
    }
    
    @Override
    protected void onPause() {
    	if (databaseHelper != null) {
			try {
				databaseHelper.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (database != null && database.isOpen())
					database.close();
			}
    	}
        super.onPause();
    }

    protected float getFontSize(int part) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		int intFontSize = settings.getInt("fontSize", 0);
		
		return getFontSize(part,intFontSize);
		
    }
    
    protected float getFontSize(int part, int intFontSize) {
    	if (Globals.fDefaultFontSizes[intFontSize] == null) return -1;
    	
		switch( part ) {
			case FONT_HEADER: 
			case FONT_TITLE: 
			case FONT_LOCATION: 
			case FONT_TEXT: {
				return Globals.fDefaultFontSizes[intFontSize][part];
			}
		}
		return 10;
    	
    }
    
    protected void setFontSize( int size ) {
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putInt("fontSize", size);
    	editor.commit();
    }
    
    protected int getBGalpha( ) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		int intBGalpha = settings.getInt("bgAlpha", 0);
		if (intBGalpha < 50) intBGalpha = 255;
		return intBGalpha;
    }
    protected void setBGalpha( int opacity ) {
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putInt("bgAlpha", opacity);
    	editor.commit();
    }
    
    protected Bitmap getImage( String file ) {
    	Bitmap bmp = null;

		File cacheDir;
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),"MF_SIB/scoundrel");
		} else {
			cacheDir = this.getCacheDir();
		}
		
		File f = new File(cacheDir, file);
		if (f.exists()) {
			bmp = Utilities.decodeFile(f);
		}
		
		return bmp;
    }
}
