package com.agenthun.blemonitor.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.agenthun.blemonitor.App;
import com.agenthun.blemonitor.R;
import com.agenthun.blemonitor.adapter.SectionsPagerAdapter;
import com.agenthun.blemonitor.bean.AllDynamicDataByContainerId;
import com.agenthun.blemonitor.bean.base.Detail;
import com.agenthun.blemonitor.bean.base.HistoryData;
import com.agenthun.blemonitor.connectivity.manager.RetrofitManager;
import com.agenthun.blemonitor.connectivity.service.PathType;
import com.agenthun.blemonitor.utils.ApiLevelHelper;
import com.agenthun.blemonitor.view.BottomSheetDialogView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity
   /*     implements NavigationView.OnNavigationItemSelectedListener*/ {

    private static final String TAG = "MainActivity";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected() returned: " + position);
                if (position == 0) {
                    fab.setVisibility(View.VISIBLE);
                    ViewCompat.animate(fab).scaleX(1).scaleY(1)
                            .setInterpolator(new LinearOutSlowInInterpolator())
                            .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(View view) {
                                    if (isFinishing() || (ApiLevelHelper.isAtLeast(Build.VERSION_CODES.JELLY_BEAN_MR1) && isDestroyed())) {
                                        return;
                                    }
                                    fab.setVisibility(View.VISIBLE);
                                }
                            })
                            .start();
                } else {
                    if (fab.isShown()) {
                        ViewCompat.animate(fab).scaleX(0).scaleY(0)
                                .setInterpolator(new FastOutSlowInInterpolator())
                                .setStartDelay(100)
                                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(View view) {
                                        if (isFinishing() || (ApiLevelHelper.isAtLeast(Build.VERSION_CODES.JELLY_BEAN_MR1) && isDestroyed())) {
                                            return;
                                        }
                                        fab.setVisibility(View.GONE);
                                    }
                                })
                                .start();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnFABClickListener != null) {
                    mOnFABClickListener.OnFABClickListener(view);
                }
/*                String token = App.getToken();
                if (mContainerNo != null && mContainerId != null) {
                    //showFreightDataListByBottomSheet(token, mContainerId, mContainerNo);
                } else {
                    Snackbar.make(view, getString(R.string.text_hint_track_content), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }*/
            }
        });

/*        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.device_alarm_title))
                    .setMessage(R.string.error_ble_not_supported)
                    .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "AlertDialog ble not supported");
                            finish();
                            return;
                        }
                    }).show();
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.device_alarm_title))
                    .setMessage(R.string.error_ble_not_supported)
                    .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "AlertDialog ble not supported");
                            finish();
                            return;
                        }
                    }).show();
        }
    }

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
        super.onBackPressed();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

/*    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }*/

    //FABClick interface
    public interface OnFABClickListener {
        void OnFABClickListener(View view);
    }

    private OnFABClickListener mOnFABClickListener;

    public void setOnItemClickListener(OnFABClickListener mOnFABClickListener) {
        this.mOnFABClickListener = mOnFABClickListener;
    }
}
