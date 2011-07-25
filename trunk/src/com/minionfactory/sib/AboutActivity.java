package com.minionfactory.sib;

/*
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
*/
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.minionfactory.helpers.SIBActivity;

public class AboutActivity extends SIBActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_about); 
        String email = "<a href=\"mailto:developers@minionfactory.com\">developers@minionfactory.com</a>";
        TextView tvEmail = (TextView) findViewById(R.id.tv_about_screen_email);
        tvEmail.setText(Html.fromHtml(email));
        tvEmail.setMovementMethod(LinkMovementMethod.getInstance());
        
        // TODO fix this so it's pulling it dynamically.
        //int ver = Integer.getInteger((String) getText(R.string.EULAVersion)) + Integer.getInteger((String) getText(R.string.databaseVersion)); 
        //String version = getVersionName(this,AboutActivity.class)+"-"+ver;
        String version = "1.0-2";
        ((TextView) findViewById(R.id.about_build)).setText(version);
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    	//finish();
	}
    
    
    @Override
    protected void onPause() {
		super.onPause();
    }
    /*
    public static String getVersionName(Context context, Class cls) 
    {
    	try {
    	    ComponentName comp = new ComponentName(context, cls);
    	    PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
    	    return pinfo.versionName;
    	} catch (android.content.pm.PackageManager.NameNotFoundException e) {
    	    return null;
    	}
	}
	*/
}
