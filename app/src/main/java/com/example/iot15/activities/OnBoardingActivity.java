package com.example.iot15.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.iot15.R;
import com.example.iot15.adapters.SliderAdapter;

public class OnBoardingActivity extends AppCompatActivity {
    ViewPager viewPager;
    LinearLayout dotsLayout;

    SliderAdapter sliderAdapter;
    TextView[] dots;
    Button startBtn;
    Animation animation;
    Button skipBtn;
    ConstraintLayout constraintLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_on_boarding);


        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dots);
        startBtn = findViewById(R.id.get_started_btn);
        skipBtn = findViewById(R.id.skip_btn);
        constraintLayout = findViewById(R.id.onboardLayout1);

        sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);

        addDots(0);
        setActivityBackgroundColor(0);
        viewPager.addOnPageChangeListener(changeListener);
    }

    public void toSignUp(View view) {

        startActivity(new Intent(getApplicationContext(), SignupActivity.class));
        finish();

    }

    public void setActivityBackgroundColor(int position) {

        if (position == 1){
            View view = this.getWindow().getDecorView();
            view.setBackgroundColor(getResources().getColor(R.color.artBackground2));

        }else{
            View view = this.getWindow().getDecorView();
            view.setBackgroundColor(getResources().getColor(R.color.artBackground));
        }

    }


    private void addDots(int position) {
        dots = new TextView[3];
        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.colorText));

            dotsLayout.addView(dots[i]);
        }
        if (dots.length > 0) {
            dots[position].setTextColor(getResources().getColor(R.color.black));
        }

    }

    ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            addDots(position);
            setActivityBackgroundColor(position);

            if (position != 2) {
                startBtn.setVisibility(View.INVISIBLE);
                skipBtn.setVisibility(View.VISIBLE);
            } else {
                animation = AnimationUtils.loadAnimation(OnBoardingActivity.this, R.anim.bottom_anim);
                animation.setDuration(1000);
                startBtn.setAnimation(animation);
                startBtn.setVisibility(View.VISIBLE);
                skipBtn.setVisibility(View.INVISIBLE);
            }

            if(position == 1){
                constraintLayout.setBackgroundColor(getResources().getColor(R.color.artBackground2));
            }else{
                constraintLayout.setBackgroundColor(getResources().getColor(R.color.artBackground));
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}