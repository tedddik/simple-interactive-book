package com.minionfactory.sib;

import android.os.Environment;

public class Globals {
	public static String PACKAGE   = "com.minionfactory.sib";
	//public static String PATH_FILE = Environment.getDataDirectory()+ "/data/"+PACKAGE+"/files/";
	public static String PATH_DB   = Environment.getDataDirectory()+ "/data/"+PACKAGE+"/databases/";
	public static String NAME_DB   = "sib_scoundrel.sqlite";
//  The names cannot be referenced directly	
	// "/data/data/YOUR_PACKAGE/databases/" is what the DBhelper referenced.
	public static float[][] fDefaultFontSizes = {
		{14,18,26,100}
		, {20,28,36,120}
		, {24,30,44,140}
		, {28,36,52,160}
	};
}
 
