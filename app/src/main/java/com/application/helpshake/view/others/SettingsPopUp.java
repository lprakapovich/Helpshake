package com.application.helpshake.view.others;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.application.helpshake.R;
import com.application.helpshake.databinding.SettingsPopUpBinding;


public class SettingsPopUp extends AppCompatActivity {

    SettingsPopUpBinding mBinding;
    SharedPreferences sharedPref;

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

        getWindow().setLayout((int)(width*0.75), (int)(height*0.75));

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = sharedPref.edit();

        setButtonColour(mBinding.dogButton, "cDog");
        setButtonColour(mBinding.groceriesButton, "cGrocery");
        setButtonColour(mBinding.drugsButton, "cDrug");
        setButtonColour(mBinding.othersButton, "cOther");

        mBinding.dogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPref.getString("cDog", "nothing").equals("nothing")){
                    editor.putString("cDog", "DogWalking");
                    mBinding.dogButton.setBackgroundColor(Color.parseColor("#0ac41d"));
                } else {
                    editor.putString("cDog", "nothing");
                    mBinding.dogButton.setBackgroundColor(Color.parseColor("#d12121"));
                }
                editor.apply();
            }
        });

        mBinding.drugsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPref.getString("cDrug", "nothing").equals("nothing")){
                    editor.putString("cDrug", "Drugstore");
                    mBinding.drugsButton.setBackgroundColor(Color.parseColor("#0ac41d"));
                } else {
                    editor.putString("cDrug", "nothing");
                    mBinding.drugsButton.setBackgroundColor(Color.parseColor("#d12121"));
                }
                editor.apply();
            }
        });

        mBinding.groceriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPref.getString("cGrocery", "nothing").equals("nothing")){
                    editor.putString("cGrocery", "Grocery");
                    mBinding.groceriesButton.setBackgroundColor(Color.parseColor("#0ac41d"));
                } else {
                    editor.putString("cGrocery", "nothing");
                    mBinding.groceriesButton.setBackgroundColor(Color.parseColor("#d12121"));
                }
                editor.apply();
            }
        });

        mBinding.othersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPref.getString("cOther", "nothing").equals("nothing")){
                    editor.putString("cOther", "Other");
                    mBinding.othersButton.setBackgroundColor(Color.parseColor("#0ac41d"));
                } else {
                    editor.putString("cOther", "nothing");
                    mBinding.othersButton.setBackgroundColor(Color.parseColor("#d12121"));
                }
                editor.apply();
            }
        });

    }

    public void setButtonColour(Button button, String x){
        //red if inactive, else green if active
        if (sharedPref.getString(x, "nothing").equals("nothing")){
            button.setBackgroundColor(Color.parseColor("#d12121"));
        } else {
            button.setBackgroundColor(Color.parseColor("#0ac41d"));
        }
    }
}
