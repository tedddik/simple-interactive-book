package com.minionfactory.helpers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.minionfactory.sib.Globals;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Danny Remington - MacroSolve
 * 
 *         Helper class for SQLite database.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
	
    /*
     * The Android's default system path of the application database in internal
     * storage. The package of the application is part of the path of the
     * directory.
     */
	private static String PATH_DB = Globals.PATH_DB;
    private int intDBfileID = -1; 
    private String strDatabase = "";
    
    private final Context myContext;
    
    private boolean createDatabase = false;
    private boolean upgradeDatabase = false;
    
    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     * 
     * @param context
     */
    public DataBaseHelper( Context context, String dbName, int dbVersion ){ 
    	super(context, dbName, null, context.getResources().getInteger(
    			dbVersion
    			));
    	myContext = context;
    	strDatabase = dbName;
        // Get the path of the database that is based on the context.
    	//PATH_DB = myContext.getDatabasePath(DB_NAME).getAbsolutePath();
    }
    public DataBaseHelper( Context context, String dbName, int dbVersion, int dbFileID) {
    	super( context, dbName, null, context.getResources().getInteger(
    			dbVersion
    			));
    	myContext = context;
    	intDBfileID = dbFileID;
    	strDatabase = dbName;
        // Get the path of the database that is based on the context.
    	//PATH_DB = myContext.getDatabasePath(DB_NAME).getAbsolutePath();
    }
    
    public void setDBfileID( int dbFileID) {
    	intDBfileID = dbFileID;
    }
    
    /**
     * Upgrade the database in internal storage if it exists but is not current. 
     * Create a new empty database in internal storage if it does not exist.
     */
    public void initializeDatabase(boolean preserveData) {
    	if (intDBfileID == -1) return;
        /*
         * Creates or updates the database in internal storage if it is needed
         * before opening the database. In all cases opening the database copies
         * the database in internal storage to the cache.
         */
    	
        getWritableDatabase();

        if (createDatabase) {
            /*
             * If the database is created by the copy method, then the creation
             * code needs to go here. This method consists of copying the new
             * database from assets into internal storage and then caching it.
             */
            try {
                /*
                 * Write over the empty data that was created in internal
                 * storage with the one in assets and then cache it.
                 */
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        } else if (upgradeDatabase) {
            /*
             * If the database is upgraded by the copy and reload method, then
             * the upgrade code needs to go here. This method consists of
             * renaming the old database in internal storage, create an empty
             * new database in internal storage, copying the database from
             * assets to the new database in internal storage, caching the new
             * database from internal storage, loading the data from the old
             * database into the new database in the cache and then deleting the
             * old database from internal storage.
             */ 
            try {
                FileHelper.copyFile(PATH_DB+strDatabase, PATH_DB+"old_"+strDatabase);
                copyDataBase();
                SQLiteDatabase old_db = SQLiteDatabase.openDatabase(PATH_DB+"old_"+strDatabase, null, SQLiteDatabase.OPEN_READWRITE);
                SQLiteDatabase new_db = SQLiteDatabase.openDatabase(PATH_DB+strDatabase,null, SQLiteDatabase.OPEN_READWRITE);
                
                
                if (preserveData) {
//                	new_db.rawQuery("delete from history", null);
                	new_db.delete("history", null, null);
                	
                	Cursor cur;
                	
                	ContentValues cv;
                	cur = old_db.query(
            				"history h"
            				, new String[]{"h.pageID", "h.sessionID", "h.created"}
            				, null, null, null, null, "h._id asc", null);
            		//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
            		cur.moveToFirst();	

            		int colPageID = 0;		
            		int colSessionID = 0;		
            		int colCreated = 0;

            		if (!cur.isAfterLast()){
            			colPageID = cur.getColumnIndex("pageID");
            			colSessionID = cur.getColumnIndex("sessionID");
            			colCreated = cur.getColumnIndex("created");
            		}
            		while(!cur.isAfterLast()){
            			cv = new ContentValues();

            			cv.put("pageID", cur.getString(colPageID) );
            			cv.put("sessionID", cur.getString(colSessionID) );
            			cv.put("created", cur.getString(colCreated) );
            			
            			new_db.insert("history", null, cv);
            			
            			cur.moveToNext();
            		}
                }
                
                old_db.close();
                new_db.close();

                FileHelper.deleteFile(PATH_DB+"old_"+strDatabase);

            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }


    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transferring ByteStream.
     * */
    private void copyDataBase() throws IOException {
        /*
         * Close SQLiteOpenHelper so it will commit the created empty database
         * to internal storage.
         */
        close();

        /*
         * Open the database in the assets folder as the input stream.
         */
        //InputStream myInput = myContext.getAssets().open(DB_NAME);
        InputStream myInput = myContext.getResources().openRawResource( intDBfileID );

        /*
         * Open the empty DB in internal storage as the output stream.
         */
        OutputStream myOutput = new FileOutputStream(PATH_DB+strDatabase);

        /*
         * Copy over the empty DB in internal storage with the database in the
         * assets folder.
         */
        FileHelper.copyFile(myInput, myOutput);

        /*
         * Access the copied database so SQLiteHelper will cache it and mark it
         * as created.
         */
        getWritableDatabase().close();
    }

    /*
     * This is where the creation of tables and the initial population of the
     * tables should happen, if a database is being created from scratch instead
     * of being copied from the application package assets. Copying a database
     * from the application package assets to internal storage inside this
     * method will result in a corrupted database.
     * <P>
     * NOTE: This method is normally only called when a database has not already
     * been created. When the database has been copied, then this method is
     * called the first time a reference to the database is retrieved after the
     * database is copied since the database last cached by SQLiteOpenHelper is
     * different than the database in internal storage.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
         * Signal that a new database needs to be copied. The copy process must
         * be performed after the database in the cache has been closed causing
         * it to be committed to internal storage. Otherwise the database in
         * internal storage will not have the same creation TimeStamp as the one
         * in the cache causing the database in internal storage to be marked as
         * corrupted.
         */
        createDatabase = true;

        /*
         * This will create by reading a SQL file and executing the commands in it.
         */
            // try {
            // InputStream is = myContext.getResources().getAssets().open(
            // "create_database.sql");
            //
            // String[] statements = FileHelper.parseSqlFile(is);
            //
            // for (String statement : statements) {
            // db.execSQL(statement);
            // }
            // } catch (Exception ex) {
            // ex.printStackTrace();
            // }
    }

    /**
     * Called only if version number was changed and the database has already
     * been created. Copying a database from the application package assets to
     * the internal data system inside this method will result in a corrupted
     * database in the internal data system.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
         * Signal that the database needs to be upgraded for the copy method of
         * creation. The copy process must be performed after the database has
         * been opened or the database will be corrupted.
         */
        upgradeDatabase = true;

        /*
         * noTODO Code to update the database via execution of SQL statements goes
         * here.
         */

        /*
         * This will upgrade by reading a SQL file and executing the commands in
         * it.
         */
        // try {
        // InputStream is = myContext.getResources().getAssets().open(
        // "upgrade_database.sql");
        //
        // String[] statements = FileHelper.parseSqlFile(is);
        //
        // for (String statement : statements) {
        // db.execSQL(statement);
        // }
        // } catch (Exception ex) {
        // ex.printStackTrace();
        // }
    }

    /**
     * Called every time the database is opened by getReadableDatabase or
     * getWritableDatabase. This is called after onCreate or onUpgrade is
     * called.
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    /*
     * Add your public helper methods to access and get content from the
     * database. You could return cursors by doing
     * "return myDataBase.query(....)" so it'd be easy to you to create adapters
     * for your views.
     */

}
