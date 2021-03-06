package com.chamayetu.chamayetu.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chamayetu.chamayetu.R;
import com.chamayetu.chamayetu.calender.CalenderFrag;
import com.chamayetu.chamayetu.dashboard.DashboardFrag;
import com.chamayetu.chamayetu.auth.loginuser.LoginActivity;
import com.chamayetu.chamayetu.models.Projects;
import com.chamayetu.chamayetu.settings.SettingsActivity;
import com.chamayetu.chamayetu.useraccount.UserAccountActivity;
import com.chamayetu.chamayetu.utils.Contract;
import com.chamayetu.chamayetu.utils.UtilityMethods;
import com.chamayetu.chamayetu.widgets.Fab;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.sdsmdg.tastytoast.TastyToast;

import butterknife.BindView;
import butterknife.ButterKnife;

import android.text.InputType;

import java.util.HashMap;

import static com.chamayetu.chamayetu.utils.Contract.REQUEST_INVITE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String MAINACT_TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private Uri mPhotoUrl;
    private String mEmail;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Drawer drawer = null;
    private AccountHeader headerResult = null;
    private SharedPreferences mNotifications;
    private MaterialSheetFab materialSheetFab;
    private int statusBarColor;
    private int projCount = 0;
    private DatabaseReference mDatabase;
    private FirebaseAnalytics mFirebaseAnalytics;

    // Define UI elements
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //set the back arrow in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*todo:Change to the current chama*/
        getSupportActionBar().setTitle(R.string.app_name);

        setupFab();
        updateSnackbar();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Initialize Firebase Remote Config.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        // Initialize Firebase Measurement.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            mEmail = mFirebaseUser.getEmail();
            mPhotoUrl = mFirebaseUser.getPhotoUrl();
        }
        mNotifications = getSharedPreferences(Contract.NOTIFICATION_SP_FILE,0);
        int badgeCount = mNotifications.getInt(Contract.NOTIFICATION_SP_KEY, 0);

        /*user profile*/
        final IProfile user_profile = new ProfileDrawerItem().withName(mUsername).withEmail(mEmail).withIcon(mPhotoUrl);
        Log.d(MAINACT_TAG,"Username: "+ mUsername + " Email: " + mEmail + " Photo Url: " + mPhotoUrl);

        //create the account header
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        user_profile
                )
                .withSavedInstance(savedInstanceState)
                .build();

        //create the drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        //default screen My Chama
                        new PrimaryDrawerItem().withName(R.string.drawer_item_mychama).withIcon(GoogleMaterial.Icon.gmd_group_work).withIdentifier(1),

                        // My Account
                        new PrimaryDrawerItem().withName(R.string.drawer_item_myaccount).withIcon(FontAwesome.Icon.faw_user).withIdentifier(2),

                        //Calender
                        new PrimaryDrawerItem().withName(R.string.drawer_item_calender).withIcon(FontAwesome.Icon.faw_calendar).withIdentifier(21),
                        /*projects*/
                        new PrimaryDrawerItem().withName(R.string.drawer_item_projects).withIcon(FontAwesome.Icon.faw_briefcase).withIdentifier(22),

                        //Reports
                        new PrimaryDrawerItem().withName(R.string.drawer_item_reports).withIcon(FontAwesome.Icon.faw_book).withIdentifier(23),

                        //Notifications
                        new PrimaryDrawerItem().withName(R.string.drawer_item_notification).withIcon(FontAwesome.Icon.faw_bell).withBadge(String.valueOf(badgeCount)).withBadgeStyle(new BadgeStyle(Color.RED, Color.RED)).withIdentifier(3),

                        //settings
                        new PrimaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(FontAwesome.Icon.faw_cogs).withIdentifier(4),

                        // Help
                        new PrimaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_question).withIdentifier(5),

                        //About
                        new PrimaryDrawerItem().withName(R.string.drawer_item_about).withIcon(GoogleMaterial.Icon.gmd_info).withIdentifier(6)
                ).withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if(drawerItem instanceof Nameable){
                        Fragment fragment = null;
                        String title = ((Nameable) drawerItem).getName().getText();
                        // perform click events for drawer items
                        switch ((int) drawerItem.getIdentifier()){
                            case 1:
                                /**todo: replace with the user's chama*/
                                //default screen, MyChama
                                fragment = DashboardFrag.newInstance();
                                title = "My Chama";
                                Log.d(MAINACT_TAG,"FragmentView:"+title);
                                break;
                            case 2:
                                //my account
                                startActivity(new Intent(MainActivity.this, UserAccountActivity.class));
                                break;
                            /*calender*/
                            case 21:
                                //calender fragment
                                fragment = CalenderFrag.newInstance();
                                title = "Calender";
                                break;
                            case 22:
                                //projects activity

                                break;

                            case 23:
                                //reports activity

                                break;
                            case 3:
                                // notifications
                                break;
                            case 4:
                                //settings
                                startActivity(new Intent(this, SettingsActivity.class));
                                break;
                            case 5:
                                //help screen
                                break;
                            case 6:
                                //about screen
                                new LibsBuilder()
                                        //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                        //start the activity
                                        .withAboutIconShown(true)
                                        .withAboutVersionShown(true)
                                        .withAboutDescription(getString(R.string.about_app))
                                        .start(this);
                                break;
                            default:
                                fragment = DashboardFrag.newInstance();
                                title = ((Nameable) drawerItem).getName().getText();
                                break;
                        }
                        /**swap the fragments appropriately*/
                        if(fragment != null){
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container_body, fragment);
                            fragmentTransaction.commit();
                            getSupportActionBar().setTitle(title);
                        }
                    }
                    //we do not consume the event and want the Drawer to continue with the event chain
                    return false;
                }).withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();

        //set the default screen to DashboardFrag
        Fragment fragment = DashboardFrag.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fragment);
        fragmentTransaction.commit();
    }

    /**handles setting up the FAB */
    private void setupFab() {
        Fab fab = (Fab) findViewById(R.id.materialsheet_fab);
        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.materialsheet_overlay);
        int sheetColor = getResources().getColor(R.color.gray);
        int fabColor = getResources().getColor(R.color.theme_default_primary);

        // Create material sheet FAB
        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay, sheetColor, fabColor);

        // Set material sheet event listener
        materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
            @Override
            public void onShowSheet() {
                // Save current status bar color
                statusBarColor = getStatusBarColor();
                // Set darker status bar color to match the dim overlay
                setStatusBarColor(getResources().getColor(R.color.black));
            }

            @Override
            public void onHideSheet() {
                // Restore status bar color
                setStatusBarColor(statusBarColor);
            }
        });

        // Set material sheet item click listeners
        findViewById(R.id.materialsheet_item_reminder).setOnClickListener(this);
        findViewById(R.id.materialsheet_item_project).setOnClickListener(this);
    }

    /**Updates the snackbar*/
    private void updateSnackbar() {
        View snackbar = findViewById(R.id.snackbar);
        snackbar.setVisibility(View.GONE);
    }

    private int getStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getWindow().getStatusBarColor();
        }
        return 0;
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = drawer.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }

        /*close the material sheet fab*/
        if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            /*invite a new user to the app*/
            case R.id.settings_invite:
                /*.setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                *.setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                * */
                Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                        .setMessage(getString(R.string.invitation_message))
                        .setCallToActionText(getString(R.string.invitation_cta))
                        .build();
                startActivityForResult(intent, REQUEST_INVITE);
                return true;
            case R.id.settings_menu:
                //open settings
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            //sign out the user
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                mFirebaseUser = null;
                mUsername = Contract.ANONYMOUS;
                mPhotoUrl = null;
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(MAINACT_TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Use Firebase Measurement to log that invitation was sent.
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");

                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(MAINACT_TAG, "Invitations sent: " + ids.length);
            } else {
                // Use Firebase Measurement to log that invitation was not sent
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);

                // Sending failed or it was canceled, show failure message to the user
                UtilityMethods.showToast(MainActivity.this,"Failed to send invitation", TastyToast.ERROR);
                Log.d(MAINACT_TAG, "Failed to send invitation.");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            /*todo: push projects to projects node of current user's chama*/
            case R.id.materialsheet_item_project:
                //create dialog to suggest a project
                new MaterialDialog.Builder(this)
                        .title(R.string.project_input)
                        .content(R.string.project_content)
                        .inputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE |
                                InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                        .inputRange(5, 24)
                        .positiveText(R.string.submit_btn_txt)
                        .positiveColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .input(R.string.project_hint, R.string.project_hint_prefill, false,
                                (dialog, input) -> {
                                    UtilityMethods.showToast(MainActivity.this, input.toString() + " submitted.",TastyToast.INFO);
                                    Projects projects = new Projects("Oct 14 2016",input.toString());
                                    HashMap<String, Projects> proj = new HashMap<>();
                                    proj.put("proj" + String.valueOf(projCount), projects);
                                    mDatabase.child(Contract.PROJECTS_NODE).child("boda").setValue(proj);
                                }).show();
                projCount += 1;
                break;

            case R.id.materialsheet_item_reminder:
                /*setup a reminder for the group*/
                new MaterialDialog.Builder(this)
                        .title(R.string.date_picker)
                        .customView(R.layout.dialog_datepicker, false)
                        .positiveText(android.R.string.ok)
                        .positiveColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .negativeText(android.R.string.cancel)
                        .negativeColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .show();
                break;
        }
        materialSheetFab.hideSheet();
    }


}
