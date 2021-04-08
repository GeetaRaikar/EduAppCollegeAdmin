package com.padmajeet.techforedu.wowcollege.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.AcademicYear;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class ActivitySplashScreen extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference academicYearCollectionRef=db.collection("AcademicYear");
    private Gson gson = Utility.getGson();
    private SessionManager sessionManager;
    private DocumentReference staffDocRef;
    private Staff loggedInUser;
    private String loggedInUserId,staffId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        sessionManager = new SessionManager(ActivitySplashScreen.this);
        staffId = sessionManager.getString("loggedInUserId");

        // System.out.println("adminJson - " + adminId);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                if (!TextUtils.isEmpty(staffId)) {
                    validateAdmin(staffId);
                }
                else {
                    // This method will be executed once the timer is over
                    Intent i = new Intent(ActivitySplashScreen.this, ActivityLogin.class);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    startActivity(i);
                    finish();
                }
            }
        }, 1000);
    }

    private void validateAdmin(String documentId) {

        staffDocRef = db.document("Staff/" + documentId);
        staffDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        //System.out.println("Data -key -" + documentSnapshot.getId() + " value -" + documentSnapshot.getData());
                        loggedInUserId = documentSnapshot.getId();
                        loggedInUser = documentSnapshot.toObject(Staff.class);
                        loggedInUser.setId(documentSnapshot.getId());
                        if (loggedInUser != null && loggedInUser.getStatus().equals("A")) {
                            sessionManager.putString("loggedInUser", gson.toJson(loggedInUser));
                            sessionManager.putString("loggedInUserId", loggedInUserId);
                            sessionManager.putString("instituteId", loggedInUser.getInstituteId());
                            getCurrentAcademicYear();
                            Intent intent = new Intent(ActivitySplashScreen.this, ActivityHome.class);
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent i = new Intent(ActivitySplashScreen.this, ActivityLogin.class);
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                            startActivity(i);
                            finish();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Intent i = new Intent(ActivitySplashScreen.this, ActivityLogin.class);
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        startActivity(i);
                        finish();
                    }
                });
    }

    private void getCurrentAcademicYear() {
        academicYearCollectionRef
                .whereEqualTo("status", "A")
                .whereEqualTo("instituteId", loggedInUser.getInstituteId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                AcademicYear academicYear = document.toObject(AcademicYear.class);
                                academicYear.setId(document.getId());
                                sessionManager.putString("academicYear", gson.toJson(academicYear));
                                sessionManager.putString("academicYearId", document.getId());
                            }
                        } else {
                            // Log.d(TAG, "Error getting documents: ", task.getException());
                            //System.out.println("Error getting documents: -" + task.getException());
                        }
                    }
                });
    }
}
