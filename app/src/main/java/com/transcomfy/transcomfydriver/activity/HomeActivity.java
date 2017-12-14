package com.transcomfy.transcomfydriver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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

        // Set action in the event avail bus space is clicked
        btnAvailSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                availSpace();
            }
        });

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
                        } else {
                            tvNumberPlate.setText(driver.getBus().getNumberPlate());
                            tvSpacesAvailable.setText(
                                    String.valueOf(driver.getBus().getAvailableSpace())
                                    .concat(getString(R.string.tv_spaces_available))
                            );

                            if(dataSnapshot.child("bus").child("requests").getValue() != null) {
                                tvPendingRequests.setText(String.valueOf(dataSnapshot.child("bus").child("requests").getChildrenCount()));
                            }

                            /*if(driver.getBus().getRequests() != null) {
                                tvPendingRequests.setText(String.valueOf(driver.getBus().getRequests().size()));
                            }*/
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
        Intent intent = new Intent(HomeActivity.this, PendingRequestsActivity.class);
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
