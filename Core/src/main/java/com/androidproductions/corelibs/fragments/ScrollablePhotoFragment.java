package com.androidproductions.corelibs.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidproductions.corelibs.R;
import com.androidproductions.corelibs.views.ObservableScrollView;

public abstract class ScrollablePhotoFragment extends BaseFragment implements ObservableScrollView.Callbacks {
    private static final float PHOTO_ASPECT_RATIO = 1.7777777f;
    private ImageView mPhotoView;
    private TextView mTitleView;
    private TextView mSubTitleView;
    private int mPhotoHeightPixels;
    private float mMaxHeaderElevation;
    private int mHeaderHeightPixels;
    private View mDetailsContainer;
    protected final int mLayoutId;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            recomputePhotoAndScrollingMetrics();
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScrollablePhotoFragment() {
        mLayoutId = 0;
    }

    private FrameLayout mPhotoViewContainer;
    private LinearLayout mHeaderBox;
    private ObservableScrollView mScrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(mLayoutId, container, false);

        mPhotoViewContainer = (FrameLayout) rootView.findViewById(R.id.photo_view_container);
        mPhotoView = (ImageView) rootView.findViewById(R.id.photo_view);

        mHeaderBox = (LinearLayout) rootView.findViewById(R.id.header_box);
        mTitleView = (TextView) rootView.findViewById(R.id.title);
        mSubTitleView = (TextView) rootView.findViewById(R.id.subtitle);
        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.scroll_root);
        mScrollView.addCallbacks(this);
        mDetailsContainer = rootView.findViewById(R.id.content_container);
        mMaxHeaderElevation = getResources().getDimensionPixelSize(
                R.dimen.max_header_elevation);

        final Toolbar toolbar = getActionBarToolbar(rootView);
        toolbar.setNavigationIcon(R.drawable.abc_btn_check_material);

        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("");
            }
        });

        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
        return rootView;
    }

    private void recomputePhotoAndScrollingMetrics() {
        mHeaderHeightPixels = mHeaderBox.getHeight();

        mPhotoHeightPixels = (int) (mPhotoView.getWidth() / PHOTO_ASPECT_RATIO);
        mPhotoHeightPixels = Math.min(mPhotoHeightPixels, mScrollView.getHeight() * 2 / 3);

        ViewGroup.LayoutParams lp;
        lp = mPhotoViewContainer.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            mPhotoViewContainer.setLayoutParams(lp);
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mDetailsContainer.getLayoutParams();
        if (mlp.topMargin != mHeaderHeightPixels + mPhotoHeightPixels) {
            mlp.topMargin = mHeaderHeightPixels + mPhotoHeightPixels;
            mDetailsContainer.setLayoutParams(mlp);
        }

        onScrollChanged(0, 0); // trigger scroll handling
    }


    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        // Reposition the header bar -- it's normally anchored to the top of the content,
        // but locks to the top of the screen on scroll
        int scrollY = mScrollView.getScrollY();

        float newTop = Math.max(mPhotoHeightPixels, scrollY);
        mHeaderBox.setTranslationY(newTop);

        float gapFillProgress = 1;
        if (mPhotoHeightPixels != 0) {
            gapFillProgress = Math.min(Math.max(getProgress(scrollY,
                    0,
                    mPhotoHeightPixels), 0), 1);
        }

        ViewCompat.setElevation(mHeaderBox, gapFillProgress * mMaxHeaderElevation);

        // Move background photo (parallax effect)
        mPhotoViewContainer.setTranslationY(scrollY * 0.5f);
    }
}
