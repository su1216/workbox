package com.su.workbox.ui.system;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.utils.SystemInfoHelper;
import com.su.workbox.utils.TelephonyManagerWrapper;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * see https://developer.android.com/guide/topics/resources/providing-resources.html
 * and {@link android.content.res.Configuration}
 *
 * Created by su on 19-10-13.
 * */
public class ConfigQualifiersActivity extends BaseAppCompatActivity {

    public static final String TAG = ConfigQualifiersActivity.class.getSimpleName();
    private BaseRecyclerAdapter<ConfigQualifier> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        mAdapter = new ConfigQualifierAdapter(new ArrayList<>());
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("配置限定符");
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<ConfigQualifier> data = initData();
        Log.d(TAG, "data: " + data);
        mAdapter.refresh(data);
    }

    private List<ConfigQualifier> initData() {
        Configuration configuration = getResources().getConfiguration();
        List<ConfigQualifier> list = new ArrayList<>();
        ConfigQualifier mccMnc = new ConfigQualifier("MCC and MNC", getMccMnc(configuration));
        ConfigQualifier locale = new ConfigQualifier("Language and region", configuration.locale.getLanguage() + "-r" + configuration.locale.getCountry()); //settings
        ConfigQualifier layoutDirection = new ConfigQualifier("Layout Direction", configuration.getLayoutDirection() == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL ? "ldrtl" : "ldltr");
        ConfigQualifier smallestWidth = new ConfigQualifier("smallestWidth", "sw" + configuration.smallestScreenWidthDp + "dp");
        ConfigQualifier availableWidth = new ConfigQualifier("Available width", "w" + configuration.screenWidthDp + "dp");
        ConfigQualifier availableHeight = new ConfigQualifier("Available height", "h" + configuration.screenHeightDp + "dp");
        ConfigQualifier screenSize = new ConfigQualifier("Screen size", getScreenSize(configuration.screenLayout));
        ConfigQualifier screenAspect = new ConfigQualifier("Screen aspect", getScreenAspect(configuration.screenLayout));
        list.add(mccMnc);
        list.add(locale);
        list.add(layoutDirection);
        list.add(smallestWidth);
        list.add(availableWidth);
        list.add(availableHeight);
        list.add(screenSize);
        list.add(screenAspect);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConfigQualifier roundScreen = new ConfigQualifier("Round screen", configuration.isScreenRound() ? "round" : "notround");
            roundScreen.apiLevel = Build.VERSION_CODES.M;
            list.add(roundScreen);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ConfigQualifier wideColorGamut = new ConfigQualifier("Wide Color Gamut", configuration.isScreenWideColorGamut() ? "widecg" : "nowidecg");
            wideColorGamut.apiLevel = Build.VERSION_CODES.O;
            list.add(wideColorGamut);

            ConfigQualifier highDynamicRange = new ConfigQualifier("High Dynamic Range (HDR)", configuration.isScreenHdr() ? "highdr" : "lowdr");
            highDynamicRange.apiLevel = Build.VERSION_CODES.O;
            list.add(highDynamicRange);
        }

        ConfigQualifier screenOrientation = new ConfigQualifier("Screen orientation", configuration.orientation == Configuration.ORIENTATION_PORTRAIT ? "port" : "land");
        ConfigQualifier uiMode = new ConfigQualifier("UI mode", getUiMode(configuration.uiMode));
        ConfigQualifier nightMode = new ConfigQualifier("Night mode", getNightMode(configuration.uiMode));
        ConfigQualifier screenPixelDensity = new ConfigQualifier("Screen pixel density (dpi)", SystemInfoHelper.getDpiInfo(configuration.densityDpi));
        ConfigQualifier touchscreenType = new ConfigQualifier("Touchscreen type", getTouchscreenType(configuration.touchscreen));
        ConfigQualifier keyboardAvailability = new ConfigQualifier("Keyboard availability", getKeyboardAvailability(configuration.hardKeyboardHidden, configuration.keyboardHidden));
        ConfigQualifier primaryTextInputMethod = new ConfigQualifier("Primary text input method", getPrimaryTextInputMethod(configuration.keyboard));
        ConfigQualifier navigationKeyAvailability = new ConfigQualifier("Navigation key availability", getNavigationKeyAvailability(configuration.navigation, configuration.navigationHidden));
        ConfigQualifier primaryNonTouchNavigationMethod = new ConfigQualifier("Primary non-touch navigation method", getPrimaryNonTouchNavigationMethod(configuration.navigation));
        ConfigQualifier platformVersion = new ConfigQualifier("Platform Version (Api level)", "v" + Build.VERSION.SDK_INT);
        list.add(screenOrientation);
        list.add(uiMode);
        list.add(nightMode);
        list.add(screenPixelDensity);
        list.add(touchscreenType);
        list.add(keyboardAvailability);
        list.add(primaryTextInputMethod);
        list.add(navigationKeyAvailability);
        list.add(primaryNonTouchNavigationMethod);
        list.add(platformVersion);
        return list;
    }

    private String getMccMnc(@NonNull Configuration configuration) {
        if (configuration.mcc == 0) {
            return getString(R.string.workbox_no_qualifier);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("mcc");
        sb.append(String.format(Locale.US, "%03d", configuration.mcc));
        sb.append("-mnc");

        String mnc = String.valueOf(configuration.mnc);
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyManagerWrapper wrapper = new TelephonyManagerWrapper(manager);
        int count = wrapper.getPhoneCount();
        for (int i = 0; i < count; i++) {
            if (!wrapper.isSimCardPresent(i)) {
                continue;
            }
            String simOperator = wrapper.getSimOperator(i);
            if (TextUtils.isEmpty(simOperator)) {
                continue;
            }
            if (simOperator != null && simOperator.length() > 3) {
                mnc = simOperator.substring(3);
                break;
            }
        }
        sb.append(mnc);
        return sb.toString();
    }

    private String getPrimaryNonTouchNavigationMethod(int navigation) {
        switch (navigation) {
            case Configuration.NAVIGATION_UNDEFINED:
                return "undefined";
            case Configuration.NAVIGATION_NONAV:
                return "nonav";
            case Configuration.NAVIGATION_DPAD:
                return "dpad";
            case Configuration.NAVIGATION_TRACKBALL:
                return "trackball";
            case Configuration.NAVIGATION_WHEEL:
                return "wheel";
            default:
                return "";
        }
    }

    private String getNavigationKeyAvailability(int navigation, int navigationHidden) {
        if (navigation == Configuration.NAVIGATION_NONAV) {
            return getString(R.string.workbox_no_qualifier);
        }
        switch (navigationHidden) {
            case Configuration.NAVIGATIONHIDDEN_UNDEFINED:
                return "undefined";
            case Configuration.NAVIGATIONHIDDEN_NO:
                return "navexposed";
            case Configuration.NAVIGATIONHIDDEN_YES:
                return "navhidden";
            default:
                return "";
        }
    }

    private String getPrimaryTextInputMethod(int keyboard) {
        switch (keyboard) {
            case Configuration.KEYBOARD_UNDEFINED:
                return "undefined";
            case Configuration.KEYBOARD_NOKEYS:
                return "nokeys";
            case Configuration.KEYBOARD_QWERTY:
                return "qwerty";
            case Configuration.KEYBOARD_12KEY:
                return "12key";
            default:
                return "";
        }
    }

    private String getKeyboardAvailability(int hardKeyboardHidden, int keyboardHidden) {
        // keyboardHidden -> soft keyboard
        // hardKeyboardHidden -> hard keyboard
        if (keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
            return "keyssoft";
        } else if (keyboardHidden == Configuration.KEYBOARDHIDDEN_YES && hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            return "keyshidden";
        } else if (hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
            return "keysexposed";
        } else {
            return getString(R.string.workbox_no_qualifier);
        }
    }

    private String getTouchscreenType(int touchscreen) {
        switch (touchscreen) {
            case Configuration.TOUCHSCREEN_UNDEFINED:
                return "undefined";
            case Configuration.TOUCHSCREEN_NOTOUCH:
                return "notouch";
            case Configuration.TOUCHSCREEN_FINGER:
                return "finger";
            default:
                return "";
        }
    }

    private String getNightMode(int uiMode) {
        uiMode &= Configuration.UI_MODE_NIGHT_MASK;
        switch (uiMode) {
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return "undefined";
            case Configuration.UI_MODE_NIGHT_NO:
                return "notnight";
            case Configuration.UI_MODE_NIGHT_YES:
                return "night";
            default:
                return "";
        }
    }

    private String getUiMode(int uiMode) {
        uiMode &= Configuration.UI_MODE_TYPE_MASK;
        switch (uiMode) {
            case Configuration.UI_MODE_TYPE_UNDEFINED:
                return "undefined";
            case Configuration.UI_MODE_TYPE_NORMAL:
                return getString(R.string.workbox_no_qualifier);
            case Configuration.UI_MODE_TYPE_DESK:
                return "desk";
            case Configuration.UI_MODE_TYPE_CAR:
                return "car";
            case Configuration.UI_MODE_TYPE_TELEVISION:
                return "television";
            case Configuration.UI_MODE_TYPE_APPLIANCE:
                return "appliance";
            case Configuration.UI_MODE_TYPE_WATCH:
                return "watch";
            case Configuration.UI_MODE_TYPE_VR_HEADSET:
                return "vrheadset";
            default:
                return "";
        }
    }

    private String getScreenSize(int screenLayout) {
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "small";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return getString(R.string.workbox_no_qualifier);
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "large";
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return "xlarge";
            case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
                return "undefined";
            default:
                return "";
        }
    }

    private String getScreenAspect(int screenLayout) {
        screenLayout &= Configuration.SCREENLAYOUT_LONG_MASK;
        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_LONG_NO:
                return "notlong";
            case Configuration.SCREENLAYOUT_LONG_YES:
                return "long";
            case Configuration.SCREENLAYOUT_LONG_UNDEFINED:
                return "undefined";
            default:
                return "";
        }
    }

    private static class ConfigQualifierAdapter extends BaseRecyclerAdapter<ConfigQualifier> {

        ConfigQualifierAdapter(List<ConfigQualifier> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_config_qualifier;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            ConfigQualifier qualifier = getData().get(position);
            TextView valueView = holder.getView(R.id.value);
            TextView descView = holder.getView(R.id.desc);
            TextView levelView = holder.getView(R.id.level);
            valueView.setText(qualifier.value);
            descView.setText(qualifier.key);
            if (qualifier.apiLevel > 0) {
                levelView.setText("Api level " + qualifier.apiLevel);
            } else {
                levelView.setText("");
            }
        }
    }

    private static class ConfigQualifier {
        private String key;
        private String value;
        private int apiLevel;

        ConfigQualifier(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "ConfigQualifier{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    ", apiLevel=" + apiLevel +
                    '}';
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
