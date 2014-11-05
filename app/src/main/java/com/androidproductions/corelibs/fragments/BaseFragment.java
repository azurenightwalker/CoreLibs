package com.androidproductions.corelibs.fragments;


import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.androidproductions.corelibs.R;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment {


    protected Toolbar mActionBarToolbar;

    public BaseFragment() {
        // Required empty public constructor
    }


    protected Toolbar getActionBarToolbar(View rootView) {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) rootView.findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                ((ActionBarActivity)this.getActivity()).setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    protected float getProgress(int value, int min, int max) {
        if (min == max) {
            throw new IllegalArgumentException("Max (" + max + ") cannot equal min (" + min + ")");
        }

        return (value - min) / (float) (max - min);
    }


}
