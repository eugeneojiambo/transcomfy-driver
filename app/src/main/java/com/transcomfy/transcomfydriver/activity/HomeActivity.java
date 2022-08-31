package com.transcomfy.transcomfydriver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.transcomfy.transcomfydriver.R;
import com.transcomfy.transcomfydriver.data.model.Driver;

public class HomeActivity extends AppCompatActivity {

    private Toolbar tbHome;
    private TextView tvName;
    private TextView tvNumberPlate;
    private TextView tvSpacesAvailable;
    private View btnAvailSpace;
    private View btnPendingRequests;
    private TextView tvPendingRequests;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tbHome = findViewById(R.id.tb_home);
        tvName = findViewById(R.id.tv_name);
        tvNumberPlate = findViewById(R.id.tv_number_plate);
        tvSpacesAvailable = findViewById(R.id.tv_spaces_available);
        btnAvailSpace = findViewById(R.id.btn_avail_space);
        btnPendingRequests = findViewById(R.id.btn_pending_requests);
        tvPendingRequests = findViewById(R.id.tv_pending_requests);

        setSupportActionBar(tbHome);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setHome(); // Set up data and refresh UI for home page

        // Set action in the event pending requests is clicked
        btnPendingRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pendingRequests();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                logOut();
                return true;
            default:
                return false;
        }
    }

    private void setHome() {
        tvName.setText(R.string.msg_loading);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("drivers").child(auth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Driver driver = dataSnapshot.getValue(Driver.class);
                        tvName.setText(driver.getName());

                        if(driver.getBus() == null) {
                            tvNumberPlate.setText(R.string.tv_bus_allocation);
                            tvSpacesAvailable.setText(R.string.tv_no_bus);

                            // Set action in the event avail bus space is clicked
                            btnAvailSpace.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(HomeActivity.this, "You have no bus assigned to you. Contact your administrators", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            tvNumberPlate.setText(driver.getBus().getNumberPlate());
                            tvSpacesAvailable.setText(
                                    String.valueOf(driver.getBus().getAvailableSpace())
                                    .concat(getString(R.string.tv_spaces_available))
                            );

                            // Set action in the event avail bus space is clicked
                            btnAvailSpace.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    availSpace();
                                }
                            });
                        }

                        DataSnapshot snapshot = dataSnapshot.child("bus").child("requests");
                        if(snapshot.getValue() != null) {
                            int i = 0;
                            for(DataSnapshot snap : snapshot.getChildren()) {
                                if(snap.child("status").getValue(String.class).equalsIgnoreCase("PENDING")) {
                                    i++;
                                    tvPendingRequests.setText(String.valueOf(i));
                                }
                            }
                            if(i == 0) {
                                tvPendingRequests.setText(R.string.chr_hyphen);
                            }
                        } else {
                            tvPendingRequests.setText(R.string.chr_hyphen);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void availSpace() {
        Intent intent = new Intent(HomeActivity.this, AvailSpaceActivity.class);
        startActivity(intent);
    }

    private void pendingRequests() {
        Intent intent = new Intent(HomeActivity.this, RequestsActivity.class);
        startActivity(intent);
    }

    private void logOut() {
        // Get Firebase Authentication service and Sign Out user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        // Go to Log in page and clear activity history
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
        HomeActivity.this.finish();
    }

}
