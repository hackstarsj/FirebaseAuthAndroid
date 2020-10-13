package com.supercoders.firebaseauth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int GOOGLE_SIGN_IN_REQUEST =112 ;
    FirebaseAuth auth;
    CallbackManager callbackManager;
    String verificationOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth=FirebaseAuth.getInstance();
        InitiallizeGoogleLogin();
        InitiallizeFacebook();
        InitiallizeOTPLogin();
        if(auth.getCurrentUser()!=null){
            startActivity(new Intent(MainActivity.this,HomeActivity.class));
            finish();
        }
    }

    private void InitiallizeFacebook() {
        LoginButton fb_login=findViewById(R.id.fb_login);
        callbackManager=CallbackManager.Factory.create();

        fb_login.setPermissions("email","public_profile");
        fb_login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Facebook","On Success");
                handleFacebookLogin(loginResult);
            }

            @Override
            public void onCancel() {
                Log.d("Facebook","On Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("Facebook","On Error");
            }
        });
    }

    private void handleFacebookLogin(LoginResult loginResult){
        AuthCredential credential=FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());

        auth.signInWithCredential(credential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user=auth.getCurrentUser();
                            SendUserData(user);
                            Log.d("Login","Success");
                        }
                        else{
                            Log.d("Login","Error");
                        }
                    }
                });
    }

    private void InitiallizeGoogleLogin() {
        Button google_login=findViewById(R.id.google_login);
        google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DoGoogleLogin();
            }
        });
    }

    private void InitiallizeOTPLogin(){
        Button sendOtp=findViewById(R.id.send_otp);
        Button verifyOtp=findViewById(R.id.verify_otp);
        final EditText phoneInput=findViewById(R.id.phone_input);
        final EditText otpInput=findViewById(R.id.otp_input);

        sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOtpCode(phoneInput.getText().toString());
            }
        });

        verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerifyOtp(otpInput.getText().toString());
            }
        });
    }

    private void  sendOtpCode(String phone){
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d("Success","Verified");
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d("Success","Failed");
                e.printStackTrace();
            }

            @Override
            public void onCodeSent(@NonNull String verifyToke, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                verificationOtp=verifyToke;
                PhoneAuthProvider.ForceResendingToken  token=forceResendingToken;
            }
        };

        PhoneAuthProvider
                .getInstance().
                verifyPhoneNumber(phone,60, TimeUnit.SECONDS,MainActivity.this,callbacks);
    }

    private void VerifyOtp(String otp){
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationOtp,otp);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user=task.getResult().getUser();
                            SendUserData(user);
                        }
                    }
                });
    }

    private void DoGoogleLogin() {

        //Creating Google Signin Option Object
        GoogleSignInOptions goption=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("69832788166-vmto41t1usn69cnf4kneo6oe5kqhj1lo.apps.googleusercontent.com")
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();

        //Creating Google Client Object
        GoogleSignInClient googleSignInClient= GoogleSignIn.getClient(MainActivity.this,goption);

        //Launching Google Login Dialog Intent
        Intent intent=googleSignInClient.getSignInIntent();
        startActivityForResult(intent,GOOGLE_SIGN_IN_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check Result come from Google
        if(requestCode==GOOGLE_SIGN_IN_REQUEST){
            Task<GoogleSignInAccount> accountTask=GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account=accountTask.getResult(ApiException.class);
                processFirebaseLoginStep(account.getIdToken());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            callbackManager.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void processFirebaseLoginStep(String token){
        AuthCredential authCredential= GoogleAuthProvider.getCredential(token,null);
        auth.signInWithCredential(authCredential)
        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user=auth.getCurrentUser();
                    SendUserData(user);
                }
            }
        });
    }

    private void  SendUserData(FirebaseUser user){
        Log.d("Login Success","SUccess");
        Log.d("User ",user.getUid());
    }
}