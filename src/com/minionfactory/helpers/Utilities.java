package com.minionfactory.helpers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Utilities {
	public static final String md5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	
    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size=1024;
        try {
            byte[] bytes=new byte[buffer_size];
            for(;;) {
            	int count=is.read(bytes, 0, buffer_size);
            	if(count==-1) break;
            	os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    
    public static Bitmap decodeFile(File f) {
    	Bitmap bmp = null;
    	try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            FileInputStream fis = new FileInputStream(f);
            bmp = BitmapFactory.decodeStream(fis, null, o);
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        }
        return bmp;
    }
}
