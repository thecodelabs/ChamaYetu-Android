package com.chamayetu.chamayetu.statements;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.chamayetu.chamayetu.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.chamayetu.chamayetu.utils.Contract.FULL_STATEMENT_CHOICE;

/**
 * ChamaYetu
 * com.chamayetu.chamayetu.statements
 * Created by lusinabrian on 12/10/16.
 * Description: Display a full statement to the user
 */

public class FullStatement extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener{

    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;
    private int mMaxScrollSize;
    private boolean mIsImageHidden;

    /*ui references*/
    @BindView(R.id.full_statement_collapsingtoolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.full_statement_appbar) AppBarLayout appBarLayout;
    @BindView(R.id.full_statement_toolbar) Toolbar mToolbar;
    @BindView(R.id.full_statement_cardview) CardView mCardView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullstatement_layout);
        ButterKnife.bind(this);
        Bundle receiveUserChoice = getIntent().getExtras();

        //extract the data and store for processing
        CharSequence statementPeriod = receiveUserChoice.getCharSequence(FULL_STATEMENT_CHOICE);
        

        /*go back to dashboard view in MainActivity*/
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        /*add a listener for changing of offset*/
        appBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(verticalOffset)) * 100 / mMaxScrollSize;

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE) {
            if (!mIsImageHidden) {
                mIsImageHidden = true;

                //animation for fab
                //ViewCompat.animate(mFab).scaleY(0).scaleX(0).start();
            }
        }

        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (mIsImageHidden) {
                mIsImageHidden = false;
                //animation for fab
                //ViewCompat.animate(mFab).scaleY(1).scaleX(1).start();
            }
        }
    }
}
