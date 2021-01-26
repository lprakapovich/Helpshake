package com.application.helpshake.view.others;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.application.helpshake.Constants;
import com.application.helpshake.R;
import com.application.helpshake.databinding.SettingsPopUpBinding;

// TODO: change to dialog popup

public class SettingsPopUp extends AppCompatActivity {

    private SettingsPopUpBinding mBinding;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.settings_pop_up);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.70), (int)(height*0.83));

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = mPreferences.edit();

        setButtonOpacity(mBinding.dogButton, "cDog");
        setButtonOpacity(mBinding.groceriesButton, "cGrocery");
        setButtonOpacity(mBinding.drugsButton, "cDrug");
        setButtonOpacity(mBinding.othersButton, "cOther");

        mBinding.dogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreferences.getString("cDog", "nothing").equals("nothing")){
                    editor.putString("cDog", "DogWalking");
                    mBinding.dogButton.setAlpha((float) 1.0);
                } else {
                    editor.putString("cDog", "nothing");
                    mBinding.dogButton.setAlpha((float) 0.5);
                }
                editor.apply();
            }
        });

        mBinding.drugsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreferences.getString("cDrug", "nothing").equals("nothing")){
                    editor.putString("cDrug", "Drugstore");
                    mBinding.drugsButton.setAlpha((float) 1.0);
                } else {
                    editor.putString("cDrug", "nothing");
                    mBinding.drugsButton.setAlpha((float) 0.5);
                }
                editor.apply();
            }
        });

        mBinding.groceriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreferences.getString("cGrocery", "nothing").equals("nothing")){
                    editor.putString("cGrocery", "Grocery");
                    mBinding.groceriesButton.setAlpha((float) 1.0);
                } else {
                    editor.putString("cGrocery", "nothing");
                    mBinding.groceriesButton.setAlpha((float) 0.5);
                }
                editor.apply();
            }
        });

        mBinding.othersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreferences.getString("cOther", "nothing").equals("nothing")){
                    editor.putString("cOther", "Other");
                    mBinding.othersButton.setAlpha((float) 1.0);
                } else {
                    editor.putString("cOther", "nothing");
                    mBinding.othersButton.setAlpha((float) 0.5);
                }
                editor.apply();
            }
        });

        mBinding.savePrefsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("radius", Float.parseFloat(mBinding.distanceInput.getText().toString()));
                editor.apply();
                finish();
            }
        });

        mBinding.distanceInput.setText(
                String.valueOf(mPreferences.getFloat("radius", Constants.DEFAULT_SEARCH_RADIUS)));
    }

    public void setButtonOpacity(Button button, String x){
        //red if inactive, else green if active
        if (mPreferences.getString(x, "nothing").equals("nothing")){
            button.setAlpha((float) 0.5);
        } else {
            button.setAlpha((float) 1.0);
        }
    }
}
