package com.mobiotrics.contactless.smartcut;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.mobiotrics.contactless.camera.CameraActivity;
import com.mobiotrics.contactless.smartcut.Registration;

import org.greenrobot.greendao.query.Query;

import java.util.List;



public class SmartCutActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static String model_fn = "sr-detect-float.tflite";
    private static String labels_fn = "labels.txt";

    public static final String TAG = SmartCut.class.getSimpleName();

    boolean flag_SaveImage = false;
    boolean flag_CanUseCamera = false;
    boolean flag_CanSaveImageToDisk = false;
    boolean flag_CanUpload = false;

    String[] PERMISSIONS = {

            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,

            android.Manifest.permission.CAMERA,

            Manifest.permission.INTERNET
    };

    public int CountPermsDenied() {
        int cnt = 0;

        if (Build.VERSION.SDK_INT >= 23) {

            for (String permission : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ++cnt;
                }
            }
        }
        return cnt;
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, permissions[0]);

        for (int g = 0; g < grantResults.length; ++g) {

            String perm = permissions[g];

            Log.d(TAG, String.format("%s %d", perm, grantResults[g]));

            if (perm.equals(android.Manifest.permission.CAMERA) && grantResults[g] == PackageManager.PERMISSION_GRANTED) {
                flag_CanUseCamera = true;

            } else if (perm.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[g] == PackageManager.PERMISSION_GRANTED) {
                flag_CanSaveImageToDisk = true;

            } else if (perm.equals(Manifest.permission.INTERNET) && grantResults[g] == PackageManager.PERMISSION_GRANTED) {
                flag_CanUpload = true;

            }
        }
    }
    private static final int INT_PERM_REQ_CODE = 56;

    public void startCameraActivity() {


        int cnt = CountPermsDenied();

        if (cnt > 0) {


            FloatingActionButton fab = findViewById(com.mobiotrics.contactless.camera.R.id.fab);

            Snackbar.make(fab, "Grant necessary permissions to proceed", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();


            if (Build.VERSION.SDK_INT > 19) {
                requestPermissions(PERMISSIONS, INT_PERM_REQ_CODE);
            }


        } else {


            Intent i = new Intent( SmartCutActivity.this, CameraActivity.class);
            i.putExtra("model",model_fn);
            i.putExtra("labels", labels_fn);
            i.putExtra("upload_to", mSession_url);
            i.putExtra("image_class", "smartcut");


           i.putExtra("index_prob",1);
           i.putExtra("prob_cut_off", 0.85f);
           i.putExtra("msg_detected", "ShangRing circumcision detected");


            startActivityForResult(i,REQ_CODE_FOR_CAPTURE);


        }

    }

    public final static int REQ_CODE_FOR_CAPTURE = 100;
    public final static int REQ_CODE_FOR_RECORDING = 110;
    private String  mSession_url ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_cut);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraActivity();

            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        // setup the session url

        mSession_url = ((SmartCut)getApplicationContext()).getSessionUrl();
        // check registration
        // get the note DAO
        DaoSession daoSession = ((SmartCut) this.getApplicationContext()).getDaoSession();
        RegistrationDao regDao = daoSession.getRegistrationDao();

        // query all notes, sorted a-z by their text
        Query<Registration> regQuery = regDao.queryBuilder().build();

        List<Registration> l = regQuery.list();

        if (l.size() == 0) {

            Intent i = new Intent(this, RegisterActivity.class);

            //startActivity(i);

        }else{
            Registration r = l.get(0);

            if(r.getReg_status() != Registration.REG_STATUS_VERIFIED){
                Intent i = new Intent(this, RegisterActivity.class);

               // startActivity(i);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.smart_cut, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch( requestCode){

            case REQ_CODE_FOR_CAPTURE:
                break;

            case REQ_CODE_FOR_RECORDING:
                break;
        }


    }
}
