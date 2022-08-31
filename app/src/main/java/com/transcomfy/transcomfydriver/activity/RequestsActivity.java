package com.transcomfy.transcomfydriver.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.transcomfy.transcomfydriver.R;
import com.transcomfy.transcomfydriver.data.model.Bus;
import com.transcomfy.transcomfydriver.data.model.History;
import com.transcomfy.transcomfydriver.data.model.Request;
import com.transcomfy.transcomfydriver.userinterface.recycleradapter.RequestsRecyclerAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar tbPendingRequests;
    private SupportMapFragment mapFragment;
    private TextView tvPendingRequests;
    private RecyclerView rvPendingRequests;
    private TextView tvApprovedRequests;
    private RecyclerView rvApprovedRequests;
    private TextView tvInTransit;
    private RecyclerView rvInTransit;

    private GoogleMap googleMap;

    private List<Request> pendingRequests;
    private List<Request> approvedRequests;
    private List<Request> transitRequests;
    private List<Request> pins;
    private Bus bus;

    private int REQUEST_CODE_LOCATION = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tbPendingRequests = findViewById(R.id.tb_pending_requests);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        tvPendingRequests = findViewById(R.id.tv_pending_requests);
        rvPendingRequests = findViewById(R.id.rv_pending_requests);
        tvApprovedRequests = findViewById(R.id.tv_approved_requests);
        rvApprovedRequests = findViewById(R.id.rv_approved_requests);
        tvInTransit = findViewById(R.id.tv_in_transit);
        rvInTransit = findViewById(R.id.rv_in_transit);

        setSupportActionBar(tbPendingRequests);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mapFragment.getMapAsync(RequestsActivity.this);

        setPendingRequests(); // Set up data and refresh UI for pending requests page
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(RequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(RequestsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        } else {
            googleMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(criteria, true);
            locationManager.requestLocationUpdates(provider, 15000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(location != null) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 11.0f));

                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        String driverId = auth.getUid();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        com.transcomfy.transcomfydriver.data.model.Location newLocation = new com.transcomfy.transcomfydriver.data.model.Location();
                        if(bus == null) {
                            newLocation.setName("Please wait...");
                        } else {
                            newLocation.setName(bus.getNumberPlate());
                        }
                        newLocation.setLatitude(location.getLatitude());
                        newLocation.setLongitude(location.getLongitude());
                        database.getReference("drivers").child(driverId).child("bus").child("location").setValue(newLocation);
                        database.getReference("buses").child(bus.getBusId()).child("location").setValue(newLocation);
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
        }

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (ActivityCompat.checkSelfPermission(RequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(RequestsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkLocationPermission();
                    return false;
                }

                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(provider);
                if(location != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 11.0f));
                }
                return true;
            }
        });

        pins = new ArrayList<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String driverId = auth.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("drivers").child(driverId).child("bus").child("requests")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        googleMap.clear();
                        pins.clear();
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Request pin = snapshot.getValue(Request.class);
                            pins.add(pin);
                            LatLng position = new LatLng(pin.getLocation().getLatitude(), pin.getLocation().getLongitude());
                            MarkerOptions options = new MarkerOptions();
                            options.title(pin.getName());
                            options.snippet(pin.getLocation().getName());
                            options.position(position);
                            if(pin.getStatus().equalsIgnoreCase("PENDING")) {
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_primary_dark_18dp));
                                googleMap.addMarker(options);
                            } else if(pin.getStatus().equalsIgnoreCase("APPROVED")) {
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_green_18dp));
                                googleMap.addMarker(options);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION && googleMap != null) {
            if (ActivityCompat.checkSelfPermission(RequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(RequestsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                googleMap.setMyLocationEnabled(true);
            }
        }
    }

    private void setPendingRequests() {
        pendingRequests = new ArrayList<>();
        approvedRequests = new ArrayList<>();
        transitRequests = new ArrayList<>();

        rvPendingRequests.setNestedScrollingEnabled(false);
        rvApprovedRequests.setNestedScrollingEnabled(false);
        rvInTransit.setNestedScrollingEnabled(false);

        rvPendingRequests.setLayoutManager(new LinearLayoutManager(RequestsActivity.this));
        rvApprovedRequests.setLayoutManager(new LinearLayoutManager(RequestsActivity.this));
        rvInTransit.setLayoutManager(new LinearLayoutManager(RequestsActivity.this));

        RequestsRecyclerAdapter pendingAdapter = new RequestsRecyclerAdapter(RequestsActivity.this, pendingRequests);
        pendingAdapter.setOnItemClicked(this::pendingRequestOptions);
        rvPendingRequests.setAdapter(pendingAdapter);

        RequestsRecyclerAdapter approvedAdapter = new RequestsRecyclerAdapter(RequestsActivity.this, approvedRequests);
        approvedAdapter.setOnItemClicked(this::approvedRequestOptions);
        rvApprovedRequests.setAdapter(approvedAdapter);

        RequestsRecyclerAdapter transitAdapter = new RequestsRecyclerAdapter(RequestsActivity.this, transitRequests);
        transitAdapter.setOnItemClicked(this::transitRequestOptions);
        rvInTransit.setAdapter(transitAdapter);

        tvPendingRequests.setText("0 ".concat(getString(R.string.tv_pending_requests)));
        tvApprovedRequests.setText("0 ".concat(getString(R.string.tv_approved_requests)));
        tvInTransit.setText("0 ".concat(getString(R.string.tv_in_transit)));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String driverId = auth.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("drivers").child(driverId).child("bus")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        bus = dataSnapshot.getValue(Bus.class);
                        bus.setId(dataSnapshot.getKey());

                        DataSnapshot snapshot = dataSnapshot.child("requests");

                        pendingRequests.clear();
                        approvedRequests.clear();
                        transitRequests.clear();

                        rvPendingRequests.getAdapter().notifyDataSetChanged();
                        rvApprovedRequests.getAdapter().notifyDataSetChanged();
                        rvInTransit.getAdapter().notifyDataSetChanged();

                        for(DataSnapshot snap : snapshot.getChildren()) {
                            Request request = snap.getValue(Request.class);
                            if(request.getStatus().equalsIgnoreCase("PENDING")) {
                                request.setId(snap.getKey());
                                pendingRequests.add(request);
                                rvPendingRequests.getAdapter().notifyDataSetChanged();
                                tvPendingRequests.setText(
                                        String.valueOf(pendingRequests.size())
                                        .concat(" ")
                                        .concat(getString(R.string.tv_pending_requests))
                                );
                            } else if(request.getStatus().equalsIgnoreCase("APPROVED")) {
                                request.setId(snap.getKey());
                                approvedRequests.add(request);
                                rvApprovedRequests.getAdapter().notifyDataSetChanged();
                                tvApprovedRequests.setText(
                                        String.valueOf(approvedRequests.size())
                                                .concat(" ")
                                                .concat(getString(R.string.tv_approved_requests))
                                );
                            } else if(request.getStatus().equalsIgnoreCase("TRANSIT")) {
                                request.setId(snap.getKey());
                                transitRequests.add(request);
                                rvInTransit.getAdapter().notifyDataSetChanged();
                                tvInTransit.setText(
                                        String.valueOf(transitRequests.size())
                                                .concat(" ")
                                                .concat(getString(R.string.tv_in_transit))
                                );
                            }
                        }

                        if(pendingRequests.size() > 0) {
                            String subtitle = String.valueOf(pendingRequests.size()).concat(" requests remaining");
                            getSupportActionBar().setSubtitle(subtitle);
                        } else {
                            getSupportActionBar().setSubtitle(null);
                        }


                        tvPendingRequests.setText(
                                String.valueOf(pendingRequests.size())
                                        .concat(" ")
                                        .concat(getString(R.string.tv_pending_requests))
                        );

                        tvApprovedRequests.setText(
                                String.valueOf(approvedRequests.size())
                                        .concat(" ")
                                        .concat(getString(R.string.tv_approved_requests))
                        );

                        tvInTransit.setText(
                                String.valueOf(transitRequests.size())
                                        .concat(" ")
                                        .concat(getString(R.string.tv_in_transit))
                        );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void pendingRequestOptions(final Request request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RequestsActivity.this);
        builder.setTitle("Approve request");
        String message = "Would you like to approve a pickup request from "
                .concat(request.getName()).concat(" at ").concat(request.getLocation().getName());
        builder.setMessage(message);
        builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String driverId = auth.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference("drivers").child(driverId).child("bus").child("requests").child(request.getId()).child("status").setValue("DECLINED");
                database.getReference("buses").child(bus.getBusId()).child("requests").child(request.getId()).child("status").setValue("DECLINED");

                writeHistory(request, "Declined");
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Approve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(bus.getAvailableSpace() > 0) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String driverId = auth.getUid();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.getReference("drivers").child(driverId).child("bus").child("requests").child(request.getId()).child("status").setValue("APPROVED");
                    int newAvailableSpace = bus.getAvailableSpace() - 1;
                    database.getReference("drivers").child(driverId).child("bus").child("availableSpace").setValue(newAvailableSpace);
                    database.getReference("buses").child(bus.getBusId()).child("requests").child(request.getId()).child("status").setValue("APPROVED");
                    database.getReference("buses").child(bus.getBusId()).child("availableSpace").setValue(newAvailableSpace);
                    writeHistory(request, "Approved");
                } else {
                    Toast.makeText(RequestsActivity.this, "You do not have enough available space", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void approvedRequestOptions(final Request request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RequestsActivity.this);
        builder.setTitle("Passenger boarded");
        String message = "Did the passenger "
                .concat(request.getName()).concat(" board at ").concat(request.getLocation().getName()).concat("?");
        builder.setMessage(message);
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String driverId = auth.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference("drivers").child(driverId).child("bus").child("requests").child(request.getId()).child("status").setValue("DECLINED");
                database.getReference("buses").child(bus.getBusId()).child("requests").child(request.getId()).child("status").setValue("DECLINED");

                writeHistory(request, "Declined");
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(bus.getAvailableSpace() > 0) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String driverId = auth.getUid();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.getReference("drivers").child(driverId).child("bus").child("requests").child(request.getId()).child("status").setValue("TRANSIT");
                    database.getReference("buses").child(bus.getBusId()).child("requests").child(request.getId()).child("status").setValue("TRANSIT");

                    writeHistory(request, "You are in transit, fare ".concat(String.valueOf(request.getFare())));
                } else {
                    Toast.makeText(RequestsActivity.this, "You do not have enough available space", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void transitRequestOptions(final Request request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RequestsActivity.this);
        builder.setTitle("Passenger trip");
        String message = "Would you like to end passenger ".concat(request.getName()).concat(" trip?");
        builder.setMessage(message);
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String driverId = auth.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference("drivers").child(driverId).child("bus").child("requests").child(request.getId()).child("status").setValue("COMPLETED");
                int newAvailableSpace = bus.getAvailableSpace() + 1;
                database.getReference("drivers").child(driverId).child("bus").child("availableSpace").setValue(newAvailableSpace);
                database.getReference("buses").child(bus.getBusId()).child("requests").child(request.getId()).child("status").setValue("COMPLETED");
                database.getReference("buses").child(bus.getBusId()).child("availableSpace").setValue(newAvailableSpace);
                double newBalance = request.getCurrentBalance() - request.getFare();
                database.getReference("users").child(request.getId()).child("billing").child("balance").setValue(newBalance);

                // updating payment history
                String amt = "Fare Ksh ".concat(String.valueOf(request.getFare())).concat(" from ").concat(request.getFrom()).concat(" to ").concat(request.getTo());
                Calendar calendar = Calendar.getInstance();
                long createdAt = calendar.getTimeInMillis();
                String id = database.getReference().push().getKey();
                database.getReference("users").child(request.getId()).child("billing").child("paymentHistory").child(id).child("amount").setValue(amt);
                database.getReference("users").child(request.getId()).child("billing").child("paymentHistory").child(id).child("createdAt").setValue(createdAt);

                // updating history
                writeHistory(request, "Trip completed, fare ".concat(String.valueOf(request.getFare())));
            }
        });
        builder.show();
    }

    private void writeHistory(Request request, String description) {
        Calendar calendar = Calendar.getInstance();
        long createdAt = calendar.getTimeInMillis();

        History history = new History();
        history.setTitle("Trip ".concat(request.getFrom()).concat(" to ").concat(request.getTo()));
        history.setDescription(description);
        history.setCreatedAt(createdAt);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(request.getId()).child("history").push().setValue(history);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", request.getFare());
        transaction.put("userId", auth.getCurrentUser().getUid());
        transaction.put("userName", "Sharon");
        transaction.put("routeNumber", bus.getRoute());
        database.getReference("transactions").child(bus.getSaccoId()).push().setValue(transaction);
    }

    private void checkLocationPermission() {
        ActivityCompat.requestPermissions(RequestsActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
    }

}
