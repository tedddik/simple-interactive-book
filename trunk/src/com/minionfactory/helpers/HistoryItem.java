package com.minionfactory.helpers;

import android.content.Context;
import android.content.res.Resources;
import com.minionfactory.sib.Globals;

public class HistoryItem {
	private String strPageID;
	private int iPicture;
	private String strTitle;
	private String strBlurb;

	public String getPageID(){
		return strPageID;
	}
	public void setPageID( String pageID ){
		this.strPageID = pageID;
	}

	public String getTitle(){
		return strTitle;
	}
	public void setTitle( String title ){
		this.strTitle = title;
	}
	
	public int getPicture() {
		return iPicture;
	}
	public void setPicture ( int picture ){
		this.iPicture = picture;
	}
	public void setPicture ( String picture, Context context ) {
		Resources res = context.getResources();
		this.iPicture = res.getIdentifier(picture, "drawable", Globals.PACKAGE);
	}
	
	public void setBlurb(String strBlurb) {
		this.strBlurb = strBlurb;
	}
	public String getBlurb() {
		return strBlurb;
	}
}
