package com.su.workbox.widget.preference;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class EditTextPreferenceDialogFragmentCompat extends androidx.preference.EditTextPreferenceDialogFragmentCompat {

    private EditText mEditText;
    private int mInputType;

    public static EditTextPreferenceDialogFragmentCompat newInstance(String key, int inputType) {
        EditTextPreferenceDialogFragmentCompat fragment = new EditTextPreferenceDialogFragmentCompat();
        Bundle b = new Bundle(2);
        b.putString("key", key);
        b.putInt("inputType", inputType);
        fragment.setArguments(b);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInputType = this.getArguments().getInt("inputType");
    }

    protected void onBindDialogView(View view) {
        mEditText = view.findViewById(android.R.id.edit);
        mEditText.setInputType(mInputType);
        super.onBindDialogView(view);
    }
}
