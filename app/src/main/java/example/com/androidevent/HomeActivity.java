package example.com.androidevent;

import android.app.AlertDialog;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import example.com.androidevent.Common.Common;
import example.com.androidevent.Fragments.HomeFragment;
import example.com.androidevent.Fragments.ShoppingFragment;
import example.com.androidevent.Model.User;
import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    BottomSheetDialog bottomSheetDialog;

    CollectionReference userRef;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(HomeActivity.this);

        //Init
        userRef = FirebaseFirestore.getInstance().collection("User");
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        //Check intent, if login = true, enable full access
        // if login = false, just let user around shopping to view
        if (getIntent() != null){
            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN, false);
            if (isLogin){

                dialog.show();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //Paper.init(HomeActivity.this);
                //Paper.book().write(Common.LOGGED_KEY,user.getPhoneNumber());

                DocumentReference currentUser = userRef.document(user.getPhoneNumber());
                currentUser.get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot userSnapShot = task.getResult();
                                if (!userSnapShot.exists()) {
                                    showUpdateDialog(user.getPhoneNumber());
                                } else {
                                    //if user already available
                                    Common.currentUser = userSnapShot.toObject(User.class);
                                    bottomNavigationView.setSelectedItemId(R.id.action_home);
                                }
                                if (dialog.isShowing()){
                                    dialog.dismiss();
                                }
                            }
                        });
            }
        }

        //view
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.action_home)
                    fragment = new HomeFragment();
                else if (menuItem.getItemId() == R.id.action_shopping)
                    fragment = new ShoppingFragment();

                return loadFragment(fragment);
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    @Override
    public void onSaveInstanceState(Bundle outstate, PersistableBundle outPersistentState){

    }

    private void showUpdateDialog(String phoneNumber){

        if (dialog.isShowing())
            dialog.dismiss();

        //init dialog
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("One more step!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information,null);

        Button btn_update = (Button)sheetView.findViewById(R.id.btn_update);
        TextInputEditText edt_name = (TextInputEditText)sheetView.findViewById(R.id.edt_name);
        TextInputEditText edt_address = (TextInputEditText)sheetView.findViewById(R.id.edt_address);

        btn_update.setOnClickListener(view -> {

            if (!dialog.isShowing())
                dialog.show();

            User user = new User(edt_name.getText().toString(),
                    edt_address.getText().toString(),
                    phoneNumber);
            userRef.document(phoneNumber)
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            bottomSheetDialog.dismiss();
                            if (dialog.isShowing())
                                dialog.dismiss();
                            Toast.makeText(HomeActivity.this, "Thank you", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        bottomSheetDialog.dismiss();
                        if (dialog.isShowing())
                            dialog.dismiss();
                        Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private boolean loadFragment(Fragment fragment){
        if (fragment != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
