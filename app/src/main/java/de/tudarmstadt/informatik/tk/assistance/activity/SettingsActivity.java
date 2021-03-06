package de.tudarmstadt.informatik.tk.assistance.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.tudarmstadt.informatik.tk.assistance.Constants;
import de.tudarmstadt.informatik.tk.assistance.R.drawable;
import de.tudarmstadt.informatik.tk.assistance.R.id;
import de.tudarmstadt.informatik.tk.assistance.R.layout;
import de.tudarmstadt.informatik.tk.assistance.R.string;
import de.tudarmstadt.informatik.tk.assistance.R.xml;
import de.tudarmstadt.informatik.tk.assistance.fragment.settings.AboutSettingsFragment;
import de.tudarmstadt.informatik.tk.assistance.fragment.settings.AppSettingsFragment;
import de.tudarmstadt.informatik.tk.assistance.fragment.settings.DevSettingsFragment;
import de.tudarmstadt.informatik.tk.assistance.fragment.settings.DeviceSettingsFragment;
import de.tudarmstadt.informatik.tk.assistance.fragment.settings.SensorListSettingsFragment;
import de.tudarmstadt.informatik.tk.assistance.fragment.settings.UserProfileSettingsFragment;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.LoginUtils;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;

/**
 * Core user settings activity
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private final String[] VALID_FRAGMENTS = {
            AboutSettingsFragment.class.getName(),
            AppSettingsFragment.class.getName(),
            DevSettingsFragment.class.getName(),
            DeviceSettingsFragment.class.getName(),
            UserProfileSettingsFragment.class.getName(),
            SensorListSettingsFragment.class.getName()
    };

    private Unbinder unbinder;

    @BindView(id.toolbar)
    protected Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup root = ButterKnife.findById(this, android.R.id.content);
        View content = root.getChildAt(0);
        LinearLayoutCompat toolbarContainer = (LinearLayoutCompat) View.inflate(this, layout.activity_settings, null);

        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);

        unbinder = ButterKnife.bind(this, toolbarContainer);

        setTitle(string.settings_activity_title);

        mToolBar.setTitle(getTitle());
        mToolBar.setNavigationIcon(drawable.ic_ab_back_material);
    }

    @OnClick(id.toolbar)
    protected void onBackClicked() {
        Log.d(TAG, "On toolbar back pressed");
        setResult(id.menu_settings);
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "On normal back pressed");
        setResult(id.menu_settings);
        finish();
    }

    @Override
    public void onBuildHeaders(List<Header> headers) {

        loadHeadersFromResource(xml.preference_headers, headers);

        boolean isUserDeveloper = PreferenceUtils.isUserDeveloper(getApplicationContext());

        for (Header header : headers) {
            if (header.id == id.development_settings) {

                // user not developer -> remove developer menu entry
                if (!isUserDeveloper) {
                    headers.remove(header);
                }

                break;
            }
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {

        /**
         * Preventing fragment injection vulnerability
         */
        for (String validFragmentName : VALID_FRAGMENTS) {
            if (validFragmentName.equals(fragmentName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);

        switch ((int) header.id) {

            case id.logout_settings:
                LoginUtils.doLogout(getApplicationContext());
                setResult(Constants.INTENT_SETTINGS_LOGOUT_RESULT);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    public Toolbar getToolBar() {
        return mToolBar;
    }
}