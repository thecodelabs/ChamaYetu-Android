package com.chamayetu.chamayetu.useraccount;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.chamayetu.chamayetu.R;
import com.chamayetu.chamayetu.adapters.UserChamaViewHolder;
import com.chamayetu.chamayetu.models.ChamaGroupsModel;
import com.chamayetu.chamayetu.utils.Contract;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import static com.chamayetu.chamayetu.utils.Contract.CHAMA_GROUPS;
import static com.chamayetu.chamayetu.utils.Contract.USERACCT_TAG;
import static com.chamayetu.chamayetu.utils.Contract.USERS_NODE;

public class UserAccountActivity extends AppCompatActivity implements UserAccountView{

    @BindView(R.id.useracct_profilecard) CardView profileCardView;
    @BindView(R.id.useracct_img) ImageView userImagView;
    @BindView(R.id.useracct_chamacard) CardView chamaCardView;
    @BindView(R.id.useracct_email) TextView userEmail;
    @BindView(R.id.user_toolbar) Toolbar toolbar;
    @BindView(R.id.useracct_collapsing_toolbar_layout) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.useracct_chamarecycler) RecyclerView mRecyclerView;
    @BindView(R.id.useracct_phone) TextView userPhone;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.useracctact_layout);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        initDataIntoViews();

        //TODO: change user profile in Firebase, display a dialog
        fab.setOnClickListener(view -> Snackbar.make(view,
                "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }

    /**initialize Firebase Recycler*/
    private void initFirebaseRecycler(ChamaGroupsModel chamaGroupsModel) {
        FirebaseRecyclerAdapter<ChamaGroupsModel, UserChamaViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChamaGroupsModel, UserChamaViewHolder>(
                ChamaGroupsModel.class,
                R.layout.userchamagroups_item,
                UserChamaViewHolder.class,
                databaseReference.child(USERS_NODE).child(CHAMA_GROUPS)
        ) {
            @Override
            protected void populateViewHolder(UserChamaViewHolder viewHolder, ChamaGroupsModel chamaGroupsModel, int position) {
                for(String key : chamaGroupsModel.getChamaGroups().keySet()){
                    Log.d(USERACCT_TAG+"KEY", key);
                    viewHolder.chamaName.setText(key);
                }
                //viewHolder.bind(chamaGroupsModel);
            }
        };

        //set the linear layout manager and the adapter
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    /**Fetches data from Firebase,loads them into the views*/
    private void initDataIntoViews() {
        Glide.with(this).load(mFirebaseUser.getPhotoUrl()).into(userImagView);
        collapsingToolbarLayout.setTitle(mFirebaseUser.getDisplayName());

        userEmail.setText(mFirebaseUser.getEmail());
        int indx = mFirebaseUser.getEmail().indexOf("@");
        String username;
        try{
            username = mFirebaseUser.getEmail().substring(0,indx).toLowerCase().replaceAll("\\s+","");
        }catch (NullPointerException npe){
            Log.e(USERACCT_TAG, npe.getMessage());
            username = mFirebaseUser.getEmail().substring(0,indx);
        }

        databaseReference.child(Contract.USERS_NODE).child(username).child(CHAMA_GROUPS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            for(DataSnapshot d : dataSnapshot.getChildren()){
                                Map<String,Object> mapper = new HashMap<>();
                                mapper.put(d.getKey(),d.getValue());
                                ChamaGroupsModel chamaGroupsModel = new ChamaGroupsModel(mapper);

                                Log.d(USERACCT_TAG+"Groups", chamaGroupsModel.toString());

                                initFirebaseRecycler(chamaGroupsModel);
                            }
                        }catch (NullPointerException npe){
                            Log.e(USERACCT_TAG, npe.getMessage());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        /*silently log the db error*/
                        Log.e(USERACCT_TAG+"DBError", databaseError.getMessage());
                    }
                });
    }
}
