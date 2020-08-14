package com.su.sample;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.su.workbox.Workbox;

public class LifecycleActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lifecycle);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("生命周期历史记录");
    }

    public static class LifecycleFragment extends Fragment {

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            Workbox.registerFragment(this);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_lifecycle, container, false);
            view.findViewById(R.id.history).setOnClickListener(v -> Workbox.startActivity(Workbox.MODULE_LIFECYCLE, getContext()));
            return view;
        }
    }
}
