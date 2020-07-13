package com.su.workbox.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.TextUtils;

/**
 * Created by su on 2014/7/28.
 */
public class SimpleBlockedDialogFragment extends DialogFragment {

    private CharSequence mTitle;
    private CharSequence mMessage;

    public static SimpleBlockedDialogFragment newInstance() {
        SimpleBlockedDialogFragment f = new SimpleBlockedDialogFragment();
        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
        if (!TextUtils.isEmpty(mTitle)) {
            dialog.setTitle(mTitle);
        }
        dialog.setMessage(mMessage);
        dialog.setIndeterminate(true);
        setCancelable(false);

        return dialog;
    }

    public void updateTitle(CharSequence title) {
        mTitle = title;
    }

    public void updateMessage(CharSequence message) {
        mMessage = message;
    }
}
