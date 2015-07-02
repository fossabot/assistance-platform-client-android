package de.tu_darmstadt.tk.android.assistance.activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.BaseActivity;
import de.tu_darmstadt.tk.android.assistance.callbacks.NavigationDrawerCallbacks;
import de.tu_darmstadt.tk.android.assistance.fragments.NavigationDrawerFragment;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;


/**
 * Main user's place
 */
public class MainActivity extends BaseActivity
        implements NavigationDrawerCallbacks {

    @Bind(R.id.toolbar_actionbar)
    protected Toolbar mToolbar;
    private String TAG = MainActivity.class.getSimpleName();
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private String mUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.main_activity_title);

        ButterKnife.bind(this);

        mUserEmail = getUserEmail();

        setSupportActionBar(mToolbar);

        DrawerLayout drawerLayout = ButterKnife.findById(this, R.id.drawer);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);

        mNavigationDrawerFragment.setup(R.id.fragment_drawer, drawerLayout, mToolbar);
        mNavigationDrawerFragment.setUserData("Wladimir Schmidt", mUserEmail, BitmapFactory.decodeResource(getResources(), R.drawable.no_user_pic));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // drawer item was select
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
        } else {

            if (mBackButtonPressedOnce) {
                super.onBackPressed();
                return;
            }

            mBackButtonPressedOnce = true;

            Toaster.showLong(this, R.string.action_back_button_pressed_once);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mBackButtonPressedOnce = false;
                }
            }, Constants.BACK_BUTTON_DELAY_MILLIS);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ButterKnife.unbind(this);
        Log.d(TAG, "onStop -> unbound resources");
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
