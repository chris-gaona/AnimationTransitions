package com.teamtreehouse.albumcover;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AlbumDetailActivity extends Activity {

    public static final String EXTRA_ALBUM_ART_RESID = "EXTRA_ALBUM_ART_RESID";

    @Bind(R.id.album_art) ImageView albumArtView;
    @Bind(R.id.fab) ImageButton fab;
    @Bind(R.id.title_panel) ViewGroup titlePanel;
    @Bind(R.id.track_panel) ViewGroup trackPanel;
    @Bind(R.id.detail_container) ViewGroup detailContainer;

    private TransitionManager mTransitionManager;
    private Scene mExpandedScene;
    private Scene mCollapsedScene;
    private Scene mCurrentScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        ButterKnife.bind(this);
        populate();
        setupTransitions();
    }

    private void animate() {
//        ObjectAnimator scalex = ObjectAnimator.ofFloat(fab, "scaleX", 0, 1);
//        ObjectAnimator scaley = ObjectAnimator.ofFloat(fab, "scaleY", 0, 1);
//        AnimatorSet scaleFab = new AnimatorSet();
//        // play animations together
//        scaleFab.playTogether(scalex, scaley);

        // moved scaling fab animation to xml instead in scale.xml
        Animator scaleFab = AnimatorInflater.loadAnimator(this, R.animator.scale);
        scaleFab.setTarget(fab);

        int titleStartValue = titlePanel.getTop();
        int titleEndValue = titlePanel.getBottom();
        // use ObjectAnimator to utilize/access all animate properties
        ObjectAnimator animatorTitle = ObjectAnimator.ofInt(titlePanel, "bottom", titleStartValue, titleEndValue);
        // set type of transition for property
        animatorTitle.setInterpolator(new AccelerateInterpolator());

        int trackStartValue = trackPanel.getTop();
        int trackEndValue = trackPanel.getBottom();
        // use ObjectAnimator to utilize/access all animate properties
        ObjectAnimator animatorTrack = ObjectAnimator.ofInt(trackPanel, "bottom", trackStartValue, trackEndValue);
        // set type of transition for property
        animatorTrack.setInterpolator(new DecelerateInterpolator());

        titlePanel.setBottom(titleStartValue);
        trackPanel.setBottom(titleStartValue);
        fab.setScaleX(0);
        fab.setScaleY(0);

//        animatorTitle.setDuration(1000);
//        animatorTrack.setDuration(1000);
//        animatorTitle.setStartDelay(1000);

        // use AnimatorSet to create animations with specific time parameters
        AnimatorSet set = new AnimatorSet();
        // play animations right after each other
        set.playSequentially(animatorTitle, animatorTrack, scaleFab);
        set.start();
    }

    @OnClick(R.id.album_art)
    public void onAlbumArtClick(View view) {
        animate();
    }

    @OnClick(R.id.track_panel)
    public void onTrackPanelClicked(View view) {
        if (mCurrentScene == mExpandedScene) {
            mCurrentScene = mCollapsedScene;
        } else {
            mCurrentScene = mExpandedScene;
        }

        mTransitionManager.transitionTo(mCurrentScene);
//        // execute entire transition set
//        TransitionManager.go(expandedScene, transitionSet);
    }

    private void setupTransitions() {
        mTransitionManager = new TransitionManager();
        // root of view hierarchy
        ViewGroup transitionRoot = detailContainer;

        // Expanded scene
        mExpandedScene = Scene.getSceneForLayout(transitionRoot, R.layout.activity_album_detail_expanded, this);

        mExpandedScene.setEnterAction(new Runnable() {
            @Override
            public void run() {
                ButterKnife.bind(AlbumDetailActivity.this);
                populate();
                mCurrentScene = mExpandedScene;
            }
        });

//        // specifying new transition using new ChangeBounds()
//        // new way to do this is below
//        TransitionManager.go(expandedScene, new ChangeBounds());

        TransitionSet expandTransitionSet = new TransitionSet();
        // causes each transition to play one after the other
        expandTransitionSet.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);
        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(200);
        expandTransitionSet.addTransition(changeBounds);
        // make lyrics fade in
        Fade fadeLyrics = new Fade();
        fadeLyrics.setDuration(150);
        fadeLyrics.addTarget(R.id.lyrics);
        // add fade to transition set
        expandTransitionSet.addTransition(fadeLyrics);

        // Collapsed scene
        mCollapsedScene = Scene.getSceneForLayout(transitionRoot, R.layout.activity_album_detail, this);

        mCollapsedScene.setEnterAction(new Runnable() {
            @Override
            public void run() {
                ButterKnife.bind(AlbumDetailActivity.this);
                populate();
                mCurrentScene = mCollapsedScene;
            }
        });

//        // specifying new transition using new ChangeBounds()
//        // new way to do this is below
//        TransitionManager.go(expandedScene, new ChangeBounds());

        TransitionSet collapseTransitionSet = new TransitionSet();
        // causes each transition to play one after the other
        collapseTransitionSet.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);

        // make lyrics fade in
        Fade fadeOutLyrics = new Fade();
        fadeOutLyrics.setDuration(150);
        fadeOutLyrics.addTarget(R.id.lyrics);
        // add fade to transition set
        expandTransitionSet.addTransition(fadeOutLyrics);

        ChangeBounds resetBounds = new ChangeBounds();
        resetBounds.setDuration(200);
        collapseTransitionSet.addTransition(resetBounds);

        // sets the transitions
        mTransitionManager.setTransition(mExpandedScene, mCollapsedScene, collapseTransitionSet);
        mTransitionManager.setTransition(mCollapsedScene, mExpandedScene, expandTransitionSet);
        // calls enter for first scene
        mCollapsedScene.enter();
    }

    private void populate() {
        int albumArtResId = getIntent().getIntExtra(EXTRA_ALBUM_ART_RESID, R.drawable.mean_something_kinder_than_wolves);
        albumArtView.setImageResource(albumArtResId);

        // get reduced size of the artwork
        Bitmap albumBitmap = getReducedBitmap(albumArtResId);
        colorizeFromImage(albumBitmap);
    }

    private Bitmap getReducedBitmap(int albumArtResId) {
        // reduce image size in memory to avoid memory errors
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 8;
        return BitmapFactory.decodeResource(getResources(), albumArtResId, options);
    }

    private void colorizeFromImage(Bitmap image) {
        Palette palette = Palette.from(image).generate();

        // set panel colors
        int defaultPanelColor = 0xFF808080;
        int defaultFabColor = 0xFFEEEEEE;
        titlePanel.setBackgroundColor(palette.getDarkVibrantColor(defaultPanelColor));
        trackPanel.setBackgroundColor(palette.getLightMutedColor(defaultPanelColor));

        // set fab colors
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{android.R.attr.state_pressed}
        };

        int[] colors = new int[]{
                palette.getVibrantColor(defaultFabColor),
                palette.getLightVibrantColor(defaultFabColor)
        };
        fab.setBackgroundTintList(new ColorStateList(states, colors));
    }
}
