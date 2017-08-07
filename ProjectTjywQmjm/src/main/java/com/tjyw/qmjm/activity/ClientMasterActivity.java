package com.tjyw.qmjm.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.gyf.barlibrary.ImmersionBar;
import com.tjyw.atom.pub.adapter.AtomPubClientMasterAdapter;
import com.tjyw.qmjm.R;

import butterknife.BindView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by stephen on 07/08/2017.
 */
public class ClientMasterActivity extends BaseActivity {

    @BindView(R.id.atomPubClientMasterNavigation)
    protected AHBottomNavigation atomPubClientMasterNavigation;

    protected FragmentNavigator fragmentNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atom_pub_client_master);

        ImmersionBar.with(this)
                .statusBarDarkFont(true)
                .init();

        fragmentNavigator = new FragmentNavigator(getSupportFragmentManager(), AtomPubClientMasterAdapter.newInstance(this), R.id.masterFragmentContainer);
        fragmentNavigator.onCreate(savedInstanceState);

        atomPubClientMasterNavigation.setAccentColor(ContextCompat.getColor(getApplicationContext(), R.color.atom_ewsh_textColorBlack));
        atomPubClientMasterNavigation.setInactiveColor(ContextCompat.getColor(getApplicationContext(), R.color.atom_ewsh_textColorGrey));

        int size = AtomPubClientMasterAdapter.MASTER_TAB_RESOURCE.size();
        for (int i = 0; i < size; i ++) {
            Pair<Integer, Integer> masterTabResource = AtomPubClientMasterAdapter.getMasterTabResource(i);
            if (null != masterTabResource) {
                atomPubClientMasterNavigation.addItem(
                        new AHBottomNavigationItem(
                                masterTabResource.first, masterTabResource.second, android.R.color.white
                        )
                );
            }
        }

        atomPubClientMasterNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        atomPubClientMasterNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                fragmentNavigator.showFragment(position, false, false);
                return true;
            }
        });

        atomPubClientMasterNavigation.setCurrentItem(AtomPubClientMasterAdapter.POSITION.NAMING, true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}