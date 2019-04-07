package com.su.workbox.ui.app;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.ui.BaseAppCompatActivity;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

public class FeatureListActivity extends BaseAppCompatActivity {

    private static final String TAG = FeatureListActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_template_recycler_list);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutAnimation(getLayoutAnimationController());
        recyclerView.setAdapter(new RecyclerViewAdapter(this));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("Feature列表");
    }

    private LayoutAnimationController getLayoutAnimationController() {
        long duration = 250L;
        AnimationSet set = new AnimationSet(true);
        Animation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(duration);
        set.addAnimation(alphaAnimation);

        Animation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                                                              0.1f, Animation.RELATIVE_TO_SELF, 0.0f);
        translateAnimation.setDuration(duration);
        set.addAnimation(translateAnimation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.3f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    private static class RecyclerViewAdapter extends BaseRecyclerAdapter<FeatureInfo> {

        private RecyclerViewAdapter(@NonNull Context context) {
            super(AppHelper.getRequiredFeatures(context));
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.workbox_item_required_feature;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            FeatureInfo featureInfo = getData().get(position);
            TextView nameView = holder.getView(R.id.key);
            TextView versionView = holder.getView(R.id.value);
            String name = featureInfo.name;
            if (TextUtils.isEmpty(name)) {
                name = "OpenGL ES";
                versionView.setText(featureInfo.getGlEsVersion());
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && featureInfo.version > 0) {
                    versionView.setText(String.valueOf(featureInfo.version));
                } else {
                    versionView.setText("");
                }
            }
            nameView.setText(name);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
