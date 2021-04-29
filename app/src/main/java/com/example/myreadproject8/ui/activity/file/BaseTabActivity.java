package com.example.myreadproject8.ui.activity.file;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.example.myreadproject8.R;

import com.example.myreadproject8.ui.activity.base.BaseActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

/**
 * created by ycq on 2021/4/3 0003
 * describe：查找本地基类
 */
public abstract class BaseTabActivity extends BaseActivity {
    /**************View***************/
    protected TabLayout mTlIndicator;
    protected ViewPager mVp;

    /************Params*******************/
    private List<Fragment> mFragmentList;
    private List<String> mTitleList;

    /**************abstract***********/
    protected abstract List<Fragment> createTabFragments();
    protected abstract List<String> createTabTitles();

    @Override
    protected void bindView() {
        mTlIndicator = findViewById(R.id.tab_tl_indicator);
        mVp = findViewById(R.id.tab_vp);
    }

    /*****************rewrite method***************************/

    @Override
    protected void initWidget() {
        super.initWidget();
        setUpTabLayout();
    }


    /**
     * description:将viewpager与TabLayout关联
     */
    private void setUpTabLayout(){
        mFragmentList = createTabFragments();
        mTitleList = createTabTitles();

        checkParamsIsRight();

        TabFragmentPageAdapter adapter = new TabFragmentPageAdapter(getSupportFragmentManager());
        mVp.setAdapter(adapter);
        mVp.setOffscreenPageLimit(3);
        mTlIndicator.setupWithViewPager(mVp);
    }

    /**
     * description:检查输入的参数是否正确。即Fragment和title是成对的。
     */
    private void checkParamsIsRight(){
        if (mFragmentList == null || mTitleList == null){
            throw new IllegalArgumentException("fragmentList or titleList doesn't have null");
        }

        if (mFragmentList.size() != mTitleList.size())
            throw new IllegalArgumentException("fragment and title size must equal");
    }


    /******************inner class*****************/
    class TabFragmentPageAdapter extends FragmentPagerAdapter {


        public TabFragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }
    }
}

