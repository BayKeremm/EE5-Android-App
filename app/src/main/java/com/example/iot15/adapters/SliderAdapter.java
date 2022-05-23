package com.example.iot15.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.example.iot15.R;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    int images[] = {
            R.drawable.onboardingplant2,
            R.drawable.onboardingplant3,
            R.drawable.onboardingplant4
    };
    int headings[] = {
            R.string.slider_title1,
            R.string.slider_title2,
            R.string.slider_title3
    };

    int texts[] = {
            R.string.slider_text1,
            R.string.slider_text2,
            R.string.slider_text3
    };

    int textColor[] ={
            R.color.colorText,
            R.color.white,
            R.color.colorText
    };


    @Override
    public int getCount() {
        return headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (ConstraintLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slides_layout, container, false);

        ImageView imageView = view.findViewById(R.id.slider_img);
        TextView title = view.findViewById(R.id.title_onboarding);
        TextView text = view.findViewById(R.id.text_onboarding);

        imageView.setImageResource(images[position]);
        title.setText(headings[position]);
        text.setText(texts[position]);
//        title.setTextColor(textColor[position]);
//        text.setTextColor(textColor[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout)object);
    }
}
