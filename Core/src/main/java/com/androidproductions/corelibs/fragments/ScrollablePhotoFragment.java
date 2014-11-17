package com.androidproductions.corelibs.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
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
import com.androidproductions.corelibs.utils.ImageLoader;
import com.androidproductions.corelibs.views.ObservableScrollView;

public abstract class ScrollablePhotoFragment extends BaseFragment implements ObservableScrollView.Callbacks {
    private static final float PHOTO_ASPECT_RATIO = 1.7777777f;

    private FrameLayout mPhotoViewContainer;
    private LinearLayout mHeaderBox;
    private ObservableScrollView mScrollView;
    private View mDetailsContainer;

    protected ImageView mPhotoView;
    protected TextView mTitleView;
    protected TextView mSubTitleView;

    private int mPhotoHeightPixels;
    private float mMaxHeaderElevation;

    protected int mLayoutId;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            recomputePhotoAndScrollingMetrics();
        }
    };
    protected boolean mHasPhoto;
    private int mToolbarHeight;
    private boolean headerStatic = false;
    private TransitionDrawable transitionDrawable;
    protected int mPrimary;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScrollablePhotoFragment() {
    }

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
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

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
        int attributeResourceId = getActivity().getTheme().obtainStyledAttributes(new int[] {R.attr.colorPrimary}).getResourceId(0, 0);
        mPrimary = getResources().getColor(attributeResourceId);
        transitionDrawable = new TransitionDrawable(new Drawable[]{
                new ColorDrawable(getResources().getColor(android.R.color.transparent)),
                new ColorDrawable(mPrimary)
        });
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            // Only used when newer unavailable
            //noinspection deprecation
            mActionBarToolbar.setBackgroundDrawable(transitionDrawable);
        } else {
            mActionBarToolbar.setBackground(transitionDrawable);
        }
        return rootView;
    }

    protected void recomputePhotoAndScrollingMetrics() {
        int mHeaderHeightPixels = mHeaderBox.getHeight();
        mToolbarHeight = mActionBarToolbar.getHeight();
        mPhotoHeightPixels = 0;
        if (mHasPhoto) {
            mPhotoHeightPixels = (int) (mPhotoView.getWidth() / PHOTO_ASPECT_RATIO);
            mPhotoHeightPixels = Math.min(mPhotoHeightPixels, mScrollView.getHeight() * 2 / 3);
        }

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

    protected void setImage(long imageId)
    {
        mHasPhoto = true;
        new ImageLoader(mPhotoView,getActivity(), new ImageLoader.Callback() {
            @Override
            public void doAction(Bitmap bmp) {
                recomputePhotoAndScrollingMetrics();
            }
        }).execute(imageId);
    }


    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        // Reposition the header bar -- it's normally anchored to the top of the content,
        // but locks to the top of the screen on scroll
        int scrollY = mScrollView.getScrollY();

        float newTop = Math.max(mPhotoHeightPixels, scrollY+mToolbarHeight);
        mHeaderBox.setTranslationY(newTop);
        // Ensure static
        mActionBarToolbar.setTranslationY(scrollY);

        // If no longer moving
        if (scrollY > mPhotoHeightPixels-mToolbarHeight && !headerStatic)
        {
            headerStatic = true;
            transitionDrawable.startTransition(100);
        }
        else if (scrollY < mPhotoHeightPixels-mToolbarHeight && headerStatic)
        {
            headerStatic = false;
            transitionDrawable.reverseTransition(100);
        }
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

    protected void updateColors(Palette palette)
    {
        ColorDrawable primary = new ColorDrawable(palette.getVibrantColor(mPrimary));
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            // Only used when newer unavailable
            //noinspection deprecation
            mHeaderBox.setBackgroundDrawable(primary);
        } else {
            mHeaderBox.setBackground(primary);
        }
    }
}
