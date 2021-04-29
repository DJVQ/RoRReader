package com.example.myreadproject8.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.example.myreadproject8.Application.App;
import com.example.myreadproject8.Application.SysManager;
import com.example.myreadproject8.R;

import com.example.myreadproject8.common.APPCONST;
import com.example.myreadproject8.databinding.ActivityIndexBinding;
import com.example.myreadproject8.entity.Setting;
import com.example.myreadproject8.greendao.entity.BookGroup;
import com.example.myreadproject8.greendao.entity.Recite;
import com.example.myreadproject8.greendao.service.BookGroupService;
import com.example.myreadproject8.ui.activity.base.BaseActivity;
import com.example.myreadproject8.ui.activity.search.SearchBookActivity;
import com.example.myreadproject8.ui.dialog.ReciteDialog;
import com.example.myreadproject8.ui.fragment.MineFragment;
import com.example.myreadproject8.ui.fragment.ReciteFragment;
import com.example.myreadproject8.ui.fragment.RoRHomeFragment;
import com.example.myreadproject8.ui.fragment.bookcase.BookcaseFragment;
import com.example.myreadproject8.ui.fragment.bookcase.ReadFragment;
import com.example.myreadproject8.util.sharedpre.SharedPreUtils;
import com.example.myreadproject8.util.toast.ToastUtils;
import com.example.myreadproject8.util.utils.PermissionsChecker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class IndexActivity extends BaseActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private ActivityIndexBinding binding;
    Setting mSetting;
    private String[] titles;
    private BookcaseFragment bookcaseFragment;
    private ReadFragment readFragment;
    private ReciteFragment reciteFragment;
    private MineFragment mineFragment;
    private List<Fragment> mFragments = new ArrayList<>();
    private PermissionsChecker mPermissionsChecker;
    static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    Context context;



    /*************************/
    private Animation mBottomInAnim;
    private Animation mBottomOutAnim;
    private String groupName;
    /******************************/
    private static final int PERMISSIONS_REQUEST_STORAGE = 1;



    @Override
    protected void bindView() {
        binding = ActivityIndexBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        if (BookGroupService.getInstance().curGroupIsPrivate()) {
            /*************************put String**************************/
            SharedPreUtils.getInstance().putString(getString(R.string.curBookGroupId), "");
            SharedPreUtils.getInstance().putString(getString(R.string.curBookGroupName), "");
        }
        super.onCreate(savedInstanceState);

        //申请权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            mPermissionsChecker = new PermissionsChecker(this);
            requestPermission();
        }

    }


    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null);{
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        getSupportActionBar().setTitle(titles[0]);
        getSupportActionBar().setSubtitle(groupName);
        setStatusBarColor(R.color.colorPrimary,true);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mSetting = SysManager.getSetting();
        context = this;
        titles = new String[]{"书籍","阅读","背诵","我的"};
        bookcaseFragment = new BookcaseFragment();
        readFragment = new ReadFragment();
        reciteFragment = new ReciteFragment();
        mineFragment = new MineFragment();
        mFragments.add(bookcaseFragment);
        mFragments.add(readFragment);
        mFragments.add(reciteFragment);
        mFragments.add(mineFragment);


        /*************test*************/


        /*************test*************/
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        binding.viewPagerMain.setOffscreenPageLimit(2);
        binding.viewPagerMain.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(),BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        });
        int badgeCount = 1;
        ShortcutBadger.applyCount(context, badgeCount); //for 1.1.4+

    }

    @Override
    protected void initClick() {
        super.initClick();

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            int menuId = menuItem.getItemId();
            // 跳转指定页面：Fragment
            switch (menuId) {

                case R.id.read_b:
                    binding.viewPagerMain.setCurrentItem(1);

                    break;
                case R.id.home_b:
                    binding.viewPagerMain.setCurrentItem(0);
                    break;
                case R.id.recite_b:
                    binding.viewPagerMain.setCurrentItem(2);
                    break;
                case R.id.mine_b:
                    binding.viewPagerMain.setCurrentItem(3);
                    break;
            }
            return false;
        });



        binding.viewPagerMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                binding.bottomNavigationView.getMenu().getItem(position).setChecked(true);
                getSupportActionBar().setTitle(titles[position]);
                if (position == 0) {
                    getSupportActionBar().setSubtitle(groupName);
                } else {
                    getSupportActionBar().setSubtitle("");
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



    }

    public void reLoadFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        readFragment = (ReadFragment) fragments.get(1);
        bookcaseFragment = (BookcaseFragment) fragments.get(0);
        reciteFragment = (ReciteFragment) fragments.get(2);
        mineFragment = (MineFragment) fragments.get(3);
    }

    public ViewPager getViewPagerMain() {
        return binding.viewPagerMain;
    }


    @Override
    protected void processLogic() {
        super.processLogic();
        try {
            int settingVersion = SysManager.getSetting().getSettingVersion();
            if (settingVersion < APPCONST.SETTING_VERSION) {
                SysManager.resetSetting();
                Log.d(TAG, "resetSetting");
            }
        } catch (Exception e) {
            ToastUtils.showError(e.getLocalizedMessage());
            e.printStackTrace();
        }
        try {
            int sourceVersion = SysManager.getSetting().getSourceVersion();
            if (sourceVersion < APPCONST.SOURCE_VERSION) {
                SysManager.resetSource();

            }
        } catch (Exception e) {
            ToastUtils.showError(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.read_menu,menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isEdit = bookcaseFragment.getmBookcasePresenter() != null && bookcaseFragment.getmBookcasePresenter().ismEditState();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            menu.findItem(R.id.action_refresh).setVisible(!isEdit);
        }
        if(binding.viewPagerMain.getCurrentItem() == 0) {
            if (bookcaseFragment.getmBookcasePresenter() != null && bookcaseFragment.getmBookcasePresenter().ismEditState()) {
                menu.setGroupVisible(R.id.bookcase_menu, false);
                menu.findItem(R.id.action_finish).setVisible(true);

            } else {
                menu.setGroupVisible(R.id.bookcase_menu, true);
                menu.findItem(R.id.action_finish).setVisible(false);
            }
            menu.findItem(R.id.action_add_recite).setVisible(false);
        } else {
            menu.setGroupVisible(R.id.bookcase_menu, false);
            menu.findItem(R.id.action_finish).setVisible(false);
            if(binding.viewPagerMain.getCurrentItem() == 3)
                menu.findItem(R.id.action_add_recite).setVisible(true);
            else
                menu.findItem(R.id.action_add_recite).setVisible(false);
        }
        String isLogin = "";
        isLogin = (String)(getIntent().getSerializableExtra(APPCONST.LOGIN));
        System.out.println("mtestr0"+isLogin);
        if ("login".equals(isLogin)){
            menu.findItem(R.id.m_sync).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }



    /**
     * description:导航栏菜单点击时间
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        if (bookcaseFragment.isRecreate()) {
            reLoadFragment();
        }
        int itemId = item.getItemId();
        if (itemId == R.id.action_search) {
            Intent searchBookIntent = new Intent(this, SearchBookActivity.class);
            startActivity(searchBookIntent);
            return true;
        }else if (itemId == R.id.action_finish) {
            cancelEdit();
            return true;
        }else if (itemId == R.id.action_change_group || itemId == R.id.action_group_man) {
            if (!bookcaseFragment.getmBookcasePresenter().hasOnGroupChangeListener()) {
                bookcaseFragment.getmBookcasePresenter().addOnGroupChangeListener(() -> {//若没有则初始化分组切换监听器
                    groupName = SharedPreUtils.getInstance().getString(getString(R.string.curBookGroupName), "所有书籍");
                    getSupportActionBar().setSubtitle(groupName);
                });
            }
        }else if (itemId == R.id.action_refresh) {
            bookcaseFragment.getmBookcasePresenter().initNoReadNum();
        } else if (itemId == R.id.action_edit) {
            if (bookcaseFragment.getmBookcasePresenter().canEditBookcase()) {
                invalidateOptionsMenu();
                initMenuAnim();
                binding.bottomNavigationView.setVisibility(View.GONE);
                binding.bottomNavigationView.startAnimation(mBottomOutAnim);
            }
        }else if(itemId == R.id.action_add_recite){
            Recite recite = new Recite();
            recite.setReciteT("");
            recite.setReciteContent("");
            ReciteDialog reciteDialog = new ReciteDialog(IndexActivity.this,recite,()->{
                ToastUtils.showSuccess("加入背诵区成功");
                reciteFragment.onResume();
            });
            reciteDialog.show(getSupportFragmentManager(),"addRecite");
        }else if(itemId == R.id.m_sync){
            ToastUtils.showError("十分抱歉,由于服务器原因,暂不支持数据同步!");
        }


        return bookcaseFragment.getmBookcasePresenter().onOptionsItemSelected(item);
    }

    private void requestPermission(){
        //获取读取和写入SD卡的权限
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_STORAGE);
        }
    }

    public interface OnGroupChangeListener {
        void onChange();
    }

    @Override
    public void onBackPressed() {
        if (bookcaseFragment.getmBookcasePresenter() != null && bookcaseFragment.getmBookcasePresenter().ismEditState()) {
            cancelEdit();
            return;
        }
        if (System.currentTimeMillis() - APPCONST.exitTime > APPCONST.exitConfirmTime) {
            ToastUtils.showExit("再按一次退出");
            APPCONST.exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //获取当前书籍分组id,默认为空
        String curBookGroupId = SharedPreUtils.getInstance().getString(this.getString(R.string.curBookGroupId), "");
        BookGroup bookGroup = BookGroupService.getInstance().getGroupById(curBookGroupId);
        if (bookGroup == null) {
            groupName = "";
        } else {
            groupName = bookGroup.getName();
        }
        if (binding.viewPagerMain.getCurrentItem() == 0) {
            getSupportActionBar().setSubtitle(groupName);
        }
    }




    @Override
    protected void onDestroy() {
        App.getmApplication().shutdownThreadPool();
        super.onDestroy();
    }


    /***********************test*****************************/
    /**
     * 取消编辑状态
     */
    private void cancelEdit() {
        bookcaseFragment.getmBookcasePresenter().cancelEdit();
        invalidateOptionsMenu();
        initMenuAnim();
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
        binding.bottomNavigationView.startAnimation(mBottomInAnim);
    }

    //初始化菜单动画
    public void initMenuAnim() {
        if (mBottomInAnim != null) return;
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
    }

    public Animation getmBottomInAnim() {
        return mBottomInAnim;
    }

    public Animation getmBottomOutAnim() {
        return mBottomOutAnim;
    }


    /***********************test*****************************/
}