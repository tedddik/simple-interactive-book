package com.minionfactory.helpers;

import com.minionfactory.sib.Globals;
import com.minionfactory.sib.R;

import android.app.ListActivity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

public class SIBListActivity extends ListActivity {
	
    public static final String PREFS_NAME = "SIBScoundrelPrefsFile";
	protected boolean bLoadDB = true; 
	
    protected static SQLiteDatabase database;
    private static DataBaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
    protected void onResume() {
        super.onResume();
        
        if (bLoadDB && (databaseHelper == null || database == null || !database.isOpen())) {
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

}
