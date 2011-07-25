package com.minionfactory.sib;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.minionfactory.helpers.SIBListActivity;
import com.minionfactory.helpers.HistoryItem;

public class DialogHistoryActivity extends SIBListActivity {
	private String sessionID;
	
	private ProgressDialog pdProgress = null;
	private ArrayList<HistoryItem> hiItems = null;
	private HistoryItemAdapter hiAdapter;
	private Runnable viewHistoryItems;
	private Runnable returnRes = new Runnable() {
		@Override
		public void run() {
			if (hiItems != null && hiItems.size() > 0){
				hiAdapter.notifyDataSetChanged();
				for(int i=0; i < hiItems.size(); i++ ) 
					hiAdapter.add(hiItems.get(i));
			}
			pdProgress.dismiss();
			hiAdapter.notifyDataSetChanged();
		}
	};
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_history);
        
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
        
        hiItems = new ArrayList<HistoryItem>();
        this.hiAdapter = new HistoryItemAdapter(this, R.layout.dialog_history_row, hiItems);
        setListAdapter(this.hiAdapter);

        viewHistoryItems = new Runnable() {
        	@Override
        	public void run() {
        		getItems();
        	}
        };
        Thread thread = new Thread( null, viewHistoryItems, "MagentoBackground");
        thread.start();
        pdProgress = ProgressDialog.show(DialogHistoryActivity.this, "Please wait...", "Retrieving history ...", true);
    }
    
	/**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();

    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	outState.putString("sessionID", sessionID);
    	super.onSaveInstanceState(outState);
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final String pageID = ((HistoryItem) l.getItemAtPosition(position)).getPageID();
				
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.erase_history_warning))
			.setCancelable(false)
			.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					database.execSQL("delete from history where sessionID = "+sessionID+" and pageID > "+pageID);
					finish();
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
		super.onListItemClick(l, v, position, id);
	}
	
    private void getItems() {
    	hiItems.clear();
    	Cursor cur;
		// does exist.
    	
    	cur = database.query(
				"history h INNER JOIN pages p ON h.pageID = p._id"
				, new String[]{"h.pageID", "p.background_image", "p.button_title", "substr(content,0,60) as blurb"}
				, "sessionID = ?"
				, new String[]{ sessionID }
				, null, null, "p.sort_order desc", null);
		//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
		cur.moveToFirst();	

		int colPageID = 0;		
		int colBGimage = 0;		
		int colButtonTitle = 0;	
		int colBlurb = 0;

		if (!cur.isAfterLast()){
			colPageID = cur.getColumnIndex("pageID");
			colBGimage = cur.getColumnIndex("background_image");
			colButtonTitle = cur.getColumnIndex("button_title");
			colBlurb = cur.getColumnIndex("blurb");
		}

        hiItems = new ArrayList<HistoryItem>();
		while(!cur.isAfterLast()){
    		HistoryItem hi = new HistoryItem();
    		hi.setPageID(cur.getString(colPageID));
    		hi.setTitle(cur.getString(colButtonTitle));
    		hi.setPicture("m80_"+cur.getString(colBGimage), this);
    		hi.setBlurb(cur.getString(colBlurb));
    		hiItems.add(hi);
			cur.moveToNext();
		}
		
		cur.close();
    	try {
    		/*
    		HistoryItem hi2 = new HistoryItem();
    		hi2.setTitle("Second");
    		hi2.setPageID("2");
    		hi2.setPicture(R.drawable.sc2_justice);
    		
    		HistoryItem hi3 = new HistoryItem();
    		hi3.setTitle("Third");
    		hi3.setPageID("4");
    		hi3.setPicture("sc1_intro", this);
    		hiItems.add(hi2);
    		hiItems.add(hi3);
    		*/
    		Thread.sleep(1000);
    		
    	} catch (Exception e ) {
    		
    	}
    	runOnUiThread(returnRes);
    }
    
    private class HistoryItemAdapter extends ArrayAdapter<HistoryItem> {
    	private ArrayList<HistoryItem> items;
    	
        public HistoryItemAdapter(Context context, int textViewResourceId, ArrayList<HistoryItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	Resources res = getResources();
	    	
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.dialog_history_row, null);
            }
            HistoryItem o = items.get(position);
            if (o != null) {
                    TextView tt = (TextView) v.findViewById(R.id.dialog_history_row_tv_toptext);
                    TextView bt = (TextView) v.findViewById(R.id.dialog_history_row_tv_bottomtext);
                    ImageView img = (ImageView) v.findViewById(R.id.dialog_history_row_iv_image);
                   
                    if (tt != null) {
                    	tt.setText(""+o.getTitle());
                    }
                    if ( img != null ) {
        				Drawable myImage = res.getDrawable(o.getPicture());
        				if (myImage != null) {
        					img.setImageDrawable(myImage);
        				}
                    }
                    if(bt != null){
                		if (o.getBlurb() != null)
                			bt.setText("Snippet: "+ o.getBlurb().replaceAll("\\<[^>]*>","") );
                		else 
                			bt.setText("Snippet: no content.");
                    }
            }
            return v;
	    }
    }
}
