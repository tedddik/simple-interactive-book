package com.minionfactory.sib;

import com.minionfactory.helpers.SIBActivity;
import com.minionfactory.helpers.PageButton;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class PageActivity extends SIBActivity {
	private static final int ANIM_NONE = 0;
	private static final int ANIM_PAGE_FLIP = 1;
	private static final int ANIM_START_OVER = 2;
	private static final int ANIM_PAGE_BACK = 3;
	
	private static final int REQUEST_PAGE_SELECT = 0;
	
	private String sessionID;
	private ViewFlipper vfMain = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.page_flipper);
        
        if (savedInstanceState == null) {
	        Bundle bundle = getIntent().getExtras();
	        sessionID = bundle.getString("sessionID");
        } else {
        	sessionID = savedInstanceState.getString("sessionID");
        	if (sessionID == null){
    	        Bundle bundle = getIntent().getExtras();
    	        sessionID = bundle.getString("sessionID");
        	}
        }

        vfMain = (ViewFlipper) findViewById(R.id.page_flipper_vfMain);
        
        if (sessionID.equals("")) finish();
    }
    

    /**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadPage(ANIM_NONE);
    }
    
    
    @Override
    protected void onPause() {
    	/* Save data */
    	super.onPause();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	outState.putString("sessionID", sessionID);
    	super.onSaveInstanceState(outState);
    }

    /*
    @Override
    protected void onRestoreInstanceState( Bundle inState) {
    	Log.d("SIB",inState.getString("pageID"));
    }
    */
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pagemenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
		switch ( item.getItemId() ){
			case R.id.menu_page_startover: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getResources().getString(R.string.erase_history_warning))
					.setCancelable(false)
					.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							database.execSQL("delete from history where sessionID = "+sessionID);
							
							Cursor cur;
							cur = database.query(
									"pages p"
									, new String[]{"_id as pageID"} // title, background, location, id, content
									, "p.start = 1"
									, null, null, null, null, "1");
							//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
							cur.moveToFirst();
							
							int colPage_id = 0; 
							if (!cur.isAfterLast()){
								colPage_id = cur.getColumnIndex("pageID");
							}
							String targetPageID = cur.getString(colPage_id);
							cur.close();
							setPage( targetPageID );
							loadPage(ANIM_START_OVER);
						}
					})
					.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}
			case R.id.menu_page_menu: {
				finish();
				return true;
			}
			case R.id.menu_page_about: {
		        Intent aboutScreen = new Intent(getApplicationContext(), AboutActivity.class);
		        startActivity(aboutScreen);
				return true;
			}
			/*
			case R.id.menu_page_help: {
		        Intent helpScreen = new Intent(getApplicationContext(), HelpActivity.class);
		        startActivity(helpScreen);
				return true;
			}
			*/
			
			case R.id.menu_page_history: {
		        Intent dialogHistoryScreen = new Intent(getApplicationContext(), DialogHistoryActivity.class);
		        Bundle bundle = new Bundle();
		        bundle.putString("sessionID", sessionID);
		        dialogHistoryScreen.putExtras(bundle);
		        //startActivityForResult(dialogHistoryScreen, REQUEST_PAGE_SELECT);
		        startActivity(dialogHistoryScreen);
		        return true;
			}
			
			case R.id.menu_page_back: {
				database.execSQL("delete from history where sessionID = "+sessionID+" and pageID = (select max(pageID) from history where sessionID = "+sessionID+") ");
				loadPage(ANIM_PAGE_BACK);
				return true;
			}
			
			case R.id.menu_page_settings: {
		        Intent dialogSettingsScreen = new Intent(getApplicationContext(), DialogSettingsActivity.class);
		        startActivity(dialogSettingsScreen);
				return true;
			}
		}
		
    	return false;
    } 

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_PAGE_SELECT) {
    		if (resultCode > 0) {
    			setPage(""+resultCode);
    	        loadPage(ANIM_NONE);
    		}
    	}
    }
    
    public void setPage( String pageID ) {
    	Cursor cur;
		// does exist.
		cur = database.query(
				"history"
				, new String[]{"count(*) as totalcount"}
				, "pageID = ? AND sessionID = ?"
				, new String[]{ pageID, sessionID }
				, null, null, null, null);
		//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
		cur.moveToFirst();	

		int colTotal = 0;
		
		if (!cur.isAfterLast()){
			colTotal = cur.getColumnIndex("totalcount");
		}

		int colCount = cur.getInt(colTotal);
		cur.close();
		
		if (colCount > 0) {
			return;
		}
		
		ContentValues cv = new ContentValues();
		cv.put("pageID",pageID);
		cv.put("sessionID", sessionID);
		database.insert("history", null, cv);
		
		return;
    	
    }
    
    public void loadPage(int animationType) {
    	Cursor cur, curCheck;
    	LinearLayout.LayoutParams llp;
    	Resources res = this.getResources();

    	
    	// Prepare the view
    	FrameLayout pageFrame = (FrameLayout) View.inflate(this, R.layout.page_frame, null);
    	

    	// Get page data
		cur = database.query(
				"pages p LEFT JOIN history h ON h.pageID = p._id"
				, new String[]{"p._id","p.title","p.background_image","p.location_text","p.content"} // title, background, location, id, content
				, "(h.sessionID = ?) OR (p.start = 1) and (p._id < 60)"
				, new String[]{ sessionID }
				, null, null
				, "p.sort_order desc", "1");
		//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
		cur.moveToFirst();	
		
		
		int colPage_id = 0; 
		int colPage_title = 0;
		int colPage_bg = 0;
		int colPage_location = 0;
		int colPage_content = 0;
		if (!cur.isAfterLast()){
			colPage_id = cur.getColumnIndex("_id");
			colPage_title  = cur.getColumnIndex("title");
			colPage_bg  = cur.getColumnIndex("background_image");
			colPage_location  = cur.getColumnIndex("location_text");
			colPage_content  = cur.getColumnIndex("content");
		}
		String pageID     = cur.getString(colPage_id);
		String tmpContent = cur.getString(colPage_content);
		String tmpTitle   = cur.getString(colPage_title);
		String tmpBGimage = cur.getString(colPage_bg);
		String tmpLocation= cur.getString(colPage_location);

		if (cur.getString(colPage_id).equals("")) 
			return;  // LATER setup proper error handling here.
		cur.close();
		
		
		// Populate the page with the data.
		// Title
		if (tmpTitle != null && !tmpTitle.equals("")) {
			((TextView) pageFrame.findViewById(R.id.page_tvPageTitle)).setText(tmpTitle);
		} else {
			((TextView) pageFrame.findViewById(R.id.page_tvPageTitle)).setText("");
		}
		((TextView) pageFrame.findViewById(R.id.page_tvPageTitle)).setTextSize(getFontSize(FONT_TITLE));
		
		
		// Location
		llp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		if (tmpLocation != null && !tmpLocation.equals("")) {
			((TextView) pageFrame.findViewById(R.id.page_tvLocation)).setText(tmpLocation);
			llp.setMargins(0, 0, 0, 20); // llp.setMargins( left, top, right, bottom);
		} else {
			((TextView) pageFrame.findViewById(R.id.page_tvLocation)).setText("");
			llp.setMargins(0, 0, 0, 0); // llp.setMargins( left, top, right, bottom);
		}
		((TextView) pageFrame.findViewById(R.id.page_tvLocation)).setLayoutParams(llp);
		((TextView) pageFrame.findViewById(R.id.page_tvLocation)).setTextSize(getFontSize(FONT_LOCATION));

		
		// Content
		if (tmpContent != null && !tmpContent.equals("")) {
			tmpContent = tmpContent.replaceAll("[\\r\\n]{2,4}", "<br />\r\n<br />\r\n");
			((TextView) pageFrame.findViewById(R.id.page_tvContent)).setText(Html.fromHtml(tmpContent));
		} else {
			((TextView) pageFrame.findViewById(R.id.page_tvContent)).setText("");
		}
		((TextView) pageFrame.findViewById(R.id.page_tvContent)).setTextSize(getFontSize(FONT_TEXT));
		
		
		// Background
		if (tmpBGimage != null && !tmpBGimage.equals("")) {
			
			Bitmap bmpImage = getImage(tmpBGimage+".jpg");
			if (bmpImage != null) {
    			((ImageView) pageFrame.findViewById(R.id.page_iv_Background)).setImageBitmap(bmpImage);
    		} else {
    			int iMyImageResID = res.getIdentifier("m80_"+tmpBGimage, "drawable", Globals.PACKAGE);
    			if (iMyImageResID > 0) {
    				Drawable myImage = res.getDrawable(iMyImageResID);
    				if (myImage != null) {
    					((ImageView) pageFrame.findViewById(R.id.page_iv_Background)).setImageDrawable(myImage);
    					//((ImageView) pageFrame.findViewById(R.id.page_iv_Background)).setAlpha( getBGalpha());
    				}
    			}
    		}
			((ImageView) pageFrame.findViewById(R.id.page_iv_Background)).setAlpha( getBGalpha());
			
		}
    	
		
		// Get buttons
		cur = database.query(
				"buttons b"
				, new String[]{"targetID","label","sqlcheck"}
				, "pageID = ?"
				, new String[]{ pageID }
				, null, null, null, null);
		//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
		cur.moveToFirst();

		int colButtonText = 0;
		int colTargetID   = 0;
		int colSqlCheck   = 0;
		
		if (!cur.isAfterLast()){
			colButtonText = cur.getColumnIndex("label");
			colTargetID   = cur.getColumnIndex("targetID");
			colSqlCheck   = cur.getColumnIndex("sqlcheck");
		}
		
		while(!cur.isAfterLast()){
			
			String strSQLcheck = cur.getString(colSqlCheck);
			if (strSQLcheck != null) {
				strSQLcheck = strSQLcheck.replace("%%SESSIONID%%", sessionID);
			
				if (strSQLcheck.length() > 10) {
					curCheck = database.rawQuery(strSQLcheck, null);
					if (!curCheck.isAfterLast()) {
						curCheck.moveToFirst();
						int colCheck = curCheck.getColumnIndex("checkval");
						if (colCheck != -1) {
							String result = curCheck.getString(colCheck);
							if (!result.contentEquals("1")) {
								curCheck.close();
								cur.moveToNext();
								continue;
							}
						}
					}
					curCheck.close();
				}
			}
			
			llp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			llp.setMargins(0, 10, 10, 10); // llp.setMargins( left, top, right, bottom);
			
			PageButton button = new PageButton(this);
			button.setText( cur.getString(colButtonText) );
			button.targetID = cur.getString(colTargetID);
			button.setTextSize(getFontSize(FONT_TEXT));
			button.setLayoutParams(llp);
			
	        button.setOnClickListener(
    			new View.OnClickListener() {
		    		public void onClick(View v) {
		    			String targetID = ((PageButton) v).targetID; 
		    			setPage( targetID );
		    			loadPage(ANIM_PAGE_FLIP);
		    		}
		        }
			);

			((LinearLayout) pageFrame.findViewById(R.id.page_ll_Content)).addView(button);
			
			cur.moveToNext();
		}
		
		cur.close();
		
		
		
		// Add the view to the Flipper, set the animation and show
		vfMain.setOutAnimation(null);
		vfMain.setInAnimation(null); 
    	switch(animationType) {
	    	case ANIM_NONE: {
	        	vfMain.addView(pageFrame);
	    		vfMain.showNext();
    			break; 
			}
	    	case ANIM_START_OVER: {
	        	vfMain.addView(pageFrame, -1);
	    		vfMain.setInAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadein) );
	    		vfMain.setOutAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadeout) );
	    		//vfMain.setInAnimation( AnimationUtils.loadAnimation(this, R.anim.page_back) );
	    		//vfMain.setOutAnimation( AnimationUtils.loadAnimation(this, R.anim.page_fadeout) );
	    		vfMain.showPrevious();
    			break;
	    	}
	    	case ANIM_PAGE_FLIP: {
	        	vfMain.addView(pageFrame);
	    		vfMain.setInAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadein) );
	    		vfMain.setOutAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadeout) );
	    		//vfMain.setInAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadein) );
	    		//vfMain.setOutAnimation( AnimationUtils.loadAnimation(this, R.anim.page_next) );
	    		vfMain.showNext();
    			break;
	    	}
	    	case ANIM_PAGE_BACK: {
	        	vfMain.addView(pageFrame, -1);
	    		vfMain.setInAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadein) );
	    		vfMain.setOutAnimation( AnimationUtils.loadAnimation(this, R.anim.simple_fadeout) );
	    		//vfMain.setInAnimation( AnimationUtils.loadAnimation(this, R.anim.page_back) );
	    		//vfMain.setOutAnimation( AnimationUtils.loadAnimation(this, R.anim.page_fadeout) );
	    		vfMain.showPrevious();
    			break;
	    	}
    	}
		
		// remove the previous view.
    	if (vfMain.getChildCount() > 1) 
    		vfMain.removeViewAt(0);
    }
}
