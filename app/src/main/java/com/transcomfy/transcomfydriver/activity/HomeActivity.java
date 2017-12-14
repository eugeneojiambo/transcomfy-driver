package com.transcomfy.transcomfydriver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.transcomfy.transcomfydriver.R;

public class HomeActivity extends AppCompatActivity {

    private Toolbar tbHome;
    private View btnAvailSpace;
    private View btnPendingRequests;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tbHome = findViewById(R.id.tb_home);
        btnAvailSpace = findViewById(R.id.btn_avail_space);
        btnPendingRequests = findViewById(R.id.btn_pending_requests);

        setSupportActionBar(tbHome);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnAvailSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                availSpace();
            }
        });

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
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    private void availSpace() {
        Intent intent = new Intent(HomeActivity.this, AvailSpaceActivity.class);
        startActivity(intent);
    }

    private void pendingRequests() {
        Intent intent = new Intent(HomeActivity.this, PendingRequestsActivity.class);
        startActivity(intent);
    }

}
