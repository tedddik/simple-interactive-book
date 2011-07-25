package com.minionfactory.sib;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.minionfactory.helpers.SIBActivity;

public class DialogSettingsActivity extends SIBActivity {
	private SeekBar sbBGalpha;
	private int intFontSize = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_settings);
        
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        
        sbBGalpha = (SeekBar) findViewById(R.id.dialog_settings_sb_bgalpha);
        sbBGalpha.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				((ImageView) findViewById(R.id.dialog_settings_iv_preview_image)).setAlpha( progress + 100 );
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        
        Spinner spinner = (Spinner) findViewById(R.id.dialog_settings_s_fontsize);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        		this, R.array.dialog_settings_sa_fontsizes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
        
        Button buttonSave = (Button) findViewById( R.id.dialog_settings_b_save );
        buttonSave.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			
    			int intBGalpha = sbBGalpha.getProgress() + 100;
    			setBGalpha(intBGalpha);
    			
				setFontSize(intFontSize);
				
    			finish();
    		}
        });

        Button buttonCancel = (Button) findViewById( R.id.dialog_settings_b_cancel );
        buttonCancel.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			finish();
    		}
        });
    }
    
    /**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
        
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		intFontSize = settings.getInt("fontSize", 0);
		((Spinner) findViewById(R.id.dialog_settings_s_fontsize)).setSelection(intFontSize);
		((TextView) findViewById(R.id.dialog_settings_tv_preview)).setTextSize(getFontSize(FONT_TEXT,intFontSize));
		
		int intBGalpha = settings.getInt("bgAlpha", 255);
		sbBGalpha.setProgress( intBGalpha - 100);
		((ImageView) findViewById(R.id.dialog_settings_iv_preview_image)).setAlpha( intBGalpha );
    }
    
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    public class MyOnItemSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			intFontSize = pos;
			((TextView) findViewById(R.id.dialog_settings_tv_preview)).setTextSize(getFontSize(FONT_TEXT,intFontSize));
			//Toast.makeText(parent.getContext(), pos, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing.
		}
    }
}
