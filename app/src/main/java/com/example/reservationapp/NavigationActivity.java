package com.example.reservationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NavigationActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    DatabaseReference refRoot, refNotifications;
    ValueEventListener notificationsListener = null;
    String studentId;
    private NotificationManagerCompat managerCompat;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopNotificationListener();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        studentId = getIntent().getStringExtra("studentId");

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this,  R.id.fragment);

//        AppBarConfiguration configuration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, configuration);

        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        refRoot = FirebaseDatabase.getInstance().getReference();
        refNotifications = refRoot.child("Notifications/" + studentId);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("reservation", "reservation", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        startNotificationListener();

    }

    void stopNotificationListener(){
        if(notificationsListener != null){
            refNotifications.removeEventListener(notificationsListener);
            notificationsListener = null;
        }
    }

    void startNotificationListener(){
        stopNotificationListener();
        notificationsListener = refNotifications.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String message = dataSnapshot.getValue().toString();

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(NavigationActivity.this, "reservation");
                    builder.setContentTitle("Reservation Cancelled");
                    builder.setContentText(message);
                    builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
                    builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                    builder.setSmallIcon(R.drawable.ic_launcher_background);
                    builder.setAutoCancel(false);

                    managerCompat = NotificationManagerCompat.from(NavigationActivity.this);
                    managerCompat.notify(1, builder.build());

                    DatabaseReference notification = dataSnapshot.getRef();
                    notification.removeValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}