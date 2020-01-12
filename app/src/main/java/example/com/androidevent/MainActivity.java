package example.com.androidevent;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import example.com.androidevent.Common.Common;

public class MainActivity extends AppCompatActivity {

    private static final int APP_REQUEST_CODE = 7117; //can put any number

    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @BindView(R.id.btn_login)
    Button btn_login;
    @BindView(R.id.txt_skip)
    TextView txt_skip;

    @OnClick(R.id.btn_login)
    void loginUser(){
        startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers).build(), APP_REQUEST_CODE);
    }

    @OnClick(R.id.txt_skip)
    void skipLoginJustGoHome(){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(Common.IS_LOGIN, false);
        startActivity(intent);

    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop(){
        if (authStateListener != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == APP_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra(Common.IS_LOGIN, true);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this,"Failed to Sign In",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        setContentView(R.layout.activity_main);
        ButterKnife.bind(MainActivity.this);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth1 -> {
            FirebaseUser user = firebaseAuth1.getCurrentUser();
            if (user != null){
                checkUserFromFirebase(user);
            }
        };

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {//if already logged in
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(Common.IS_LOGIN, true);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(MainActivity.this);
        }




    }

    private void checkUserFromFirebase(FirebaseUser user) {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        //Common.updateToken(getBaseContext(),task.getResult().getToken());
                        //Log.d("solutventToken",task.getResult().getToken());

                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.putExtra(Common.IS_LOGIN,true);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.putExtra(Common.IS_LOGIN, true);
                    startActivity(intent);
                    finish();
                });
    }


}
