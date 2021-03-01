package com.jb.study.point.payment.subscription;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.jb.study.point.R;
import com.rd.PageIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionPagerActivity extends AppCompatActivity {
    private ViewPager pager;
    SubscriptionPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_subscription_pager);

        initViews();

    }

    private void initViews() {
        adapter = new SubscriptionPagerAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewPager);
        pager.setAdapter(adapter);

    }
}