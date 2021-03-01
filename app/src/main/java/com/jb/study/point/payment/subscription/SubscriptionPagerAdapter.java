package com.jb.study.point.payment.subscription;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class SubscriptionPagerAdapter extends FragmentPagerAdapter {


    SubscriptionPagerAdapter(FragmentManager supportFragmentManager) {
        super(supportFragmentManager);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new OneMonthPackFragment();

            case 1:
                return new FullPackFragment();

        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

}