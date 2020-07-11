package com.mobiotrics.contactless.smartcut;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.mobiotrics.contactless.smartcut.Registration;

import org.greenrobot.greendao.query.Query;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = SmartCut.class.getSimpleName();


    EditText ed_fullnames;
    EditText ed_email;
    Button btn_login;

    Button btn_vericode;
    EditText ed_code;

    ProgressBar progressBar;
    private DaoSession daoSession;
    private RegistrationDao regDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_smart_cut_register);

        ed_fullnames = (EditText)findViewById(R.id.names);
        ed_email= (EditText)findViewById(R.id.email);

        btn_login = (Button)findViewById(R.id.btn_login);

        progressBar = (ProgressBar)findViewById(R.id.prgBar_loading);

        btn_vericode = (Button)findViewById(R.id.btn_vericode);
        ed_code = (EditText)findViewById(R.id.vericode);


        mHanderThread = new HandlerThread("Smartcut");
        mHanderThread.start();

        mHandler = new Handler(mHanderThread.getLooper());



        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard();

                if(!isEmailValid( ed_email.getText().toString().trim()   )){

                    SnackBarMsg("ERROR : Email entered is not in valid format ");

                    return;

                }


                progressBar.setVisibility(View.VISIBLE);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SyncRegitrationRegistration();
                    }
                });

            }
        });

        btn_vericode.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                hideKeyboard();

                Query<Registration> regQuery = regDao.queryBuilder().build();
                Registration e = regQuery.list().get(0);

                String test = ed_code.getText().toString().trim().toLowerCase();

                if(test.equals( e.getVerification_code() )){

                    e.setReg_status( Registration.REG_STATUS_VERIFIED);

                    regDao.update(e);

                    SnackBarMsg("CONGRATULATIONS : Your are verified to use Smartcut !");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    finish();

                }else {

                    SnackBarMsg("ERROR : Code does not much. Try again (case INSENSITIVE");

                }


            }
        });



        // check registration
        // get the note DAO
        daoSession = ( (SmartCut) this.getApplicationContext()).getDaoSession();
        regDao =   daoSession.getRegistrationDao();

        // query all notes, sorted a-z by their text
        Query<Registration> regQuery = regDao.queryBuilder().build();

        if(regQuery.list().size() == 0){
            enableViews(true,false);
        }else{
            Registration r = regQuery.list().get(0);


             switch (r.REG_STATUS_REG){
                 case Registration.REG_STATUS_NOT_REG:
                     enableViews(true,false);
                     break;
                     case Registration.REG_STATUS_REG:

                         ed_email.setText(r.getEmail());
                         ed_fullnames.setText(r.getName());

                         enableViews(false,true);
                     break;
                     case Registration.REG_STATUS_VERIFIED:
                         enableViews(false,false);
                     break;

             }

        }


    }

    public boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    private HandlerThread mHanderThread;
    private Handler mHandler;

    private RequestQueue mRequestQueue;
    private void SyncRegitrationRegistration()
    {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());

        }

// we make the request

        JSONObject jso = new JSONObject();

        try{
            jso.put("email", ed_email.getText().toString().trim().toLowerCase() );
            jso.put("name", ed_fullnames.getText().toString().trim());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestFuture<JSONObject> future = RequestFuture.newFuture();


        String url = String.format( ((SmartCut)getApplicationContext()).getSessionUrl() , "register/");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url , jso, future, future);

//Add the request to the Request Queue
        mRequestQueue.add(request);


        try {
//Set an interval for the request to timeout. This will block the //worker thread and force it to wait for a response for 60 seconds //before timing out and raising an exception
            JSONObject response = future.get(4, TimeUnit.SECONDS);

            Log.d(TAG, response.toString(2));

            int resp_code = response.getInt("response_code");

            if(resp_code == -1){
                Snackbar.make(findViewById(R.id.btn_login), response.getString("response") , Snackbar.LENGTH_LONG).show();

            }else{ // Good result save a ne record


                Registration n = new Registration();

                n.setEmail( response.getString("email") );
                n.setName(response.getString("name"));
                n.setVericode(response.getString("verification_code"));
                n.setReg_status(Registration.REG_STATUS_REG);

                regDao.save(n);

                enableViews(false,true);

                SnackBarMsg(response.getString("response"));
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
            // exception handling
            Log.e(TAG, "Interrupted...");
            Snackbar.make(findViewById(R.id.btn_login), "ERROR : Registration process was interrupted", Snackbar.LENGTH_LONG).show();


        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.e(TAG, "ExecutionExceptuon");
            Snackbar.make(findViewById(R.id.btn_login), "ERROR : Something went wring while executing the registration", Snackbar.LENGTH_LONG).show();


            // exception handling
        } catch (TimeoutException e) {
            e.printStackTrace();
            Log.e(TAG, "Timeout");

            Snackbar.make(findViewById(R.id.btn_login), "ERROR : Confirm internet connectivity", Snackbar.LENGTH_LONG).show();



        } catch (JSONException e) {

            Snackbar.make(findViewById(R.id.btn_login), "ERROR : Could not understand server response", Snackbar.LENGTH_LONG).show();

            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });


    }

    private void SnackBarMsg(String msg){

        Snackbar.make(findViewById(R.id.btn_login), msg, Snackbar.LENGTH_LONG).show();

    }


    public  void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void enableViews(final boolean b, final boolean c){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ed_email.setEnabled(b);
                ed_fullnames.setEnabled(b);
                btn_login.setEnabled(b);

                btn_vericode.setEnabled(c);
                ed_code.setEnabled(c);
            }
        });



    }
}
