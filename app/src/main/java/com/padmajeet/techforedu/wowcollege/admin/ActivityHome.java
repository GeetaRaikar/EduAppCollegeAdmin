package com.padmajeet.techforedu.wowcollege.admin;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.padmajeet.techforedu.wowcollege.admin.util.Utility.unCheckAllMenuItems;


public class ActivityHome extends AppCompatActivity {

    private Gson gson;
    private Staff loggedInUser;
    private final static String TAG = "ActivityHome";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DrawerLayout drawer;
    private ImageView ivProfilePic;
    private SweetAlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        String userJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(userJson, Staff.class);
        View header = navigationView.getHeaderView(0);
        TextView tv_nav_name = (TextView) header.findViewById(R.id.tv_nav_name);
        TextView tv_nav_mobnum = (TextView) header.findViewById(R.id.tv_nav_mobnum);
        ivProfilePic = header.findViewById(R.id.ivProfilePic);
        if (loggedInUser != null) {

            String firstName = loggedInUser.getFirstName();
            String lastName = loggedInUser.getLastName();
            String name = "";
            if (!TextUtils.isEmpty(firstName)) {
                name = firstName;
            }
            if (!TextUtils.isEmpty(lastName)) {
                name = name + " " + lastName;
            }
            tv_nav_name.setText(name);
            tv_nav_mobnum.setText("" + loggedInUser.getMobileNumber());

        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, drawer);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation view item clicks here.
                int id = menuItem.getItemId();
                Menu menuNav = navigationView.getMenu();
                navigationView.setCheckedItem(id);
                switch (id) {
                    case R.id.nav_home:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_home).setChecked(true);
                        //replaceFragment(new FragmentHome(), getString(R.string.home));
                        break;
                    /*case R.id.nav_message:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_message).setChecked(true);
                        //replaceFragment(new FragmentMessage(), getString(R.string.message));
                        break;*/
                    case R.id.nav_event:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_event).setChecked(true);
                        replaceFragment(new FragmentEvent(), getString(R.string.event));
                        break;
                    /*case R.id.nav_sms_email:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_sms_email).setChecked(true);
                        //replaceFragment(new FragmentSendSmsEmail(), getString(R.string.smsEmail));
                        break;*/
                    case R.id.nav_feedback:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_feedback).setChecked(true);
                        //replaceFragment(new FragmentFeedback(), getString(R.string.feedback));
                        break;
                    case R.id.nav_student:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_student).setChecked(true);
                        replaceFragment(new FragmentStudent(), getString(R.string.students));
                        break;
                    /*case R.id.nav_student_kit:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_student_kit).setChecked(true);
                        //replaceFragment(new FragmentStudentKit(), getString(R.string.studentKit));
                        break;
                    case R.id.nav_student_fees:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_student_fees).setChecked(true);
                        //replaceFragment(new FragmentStudentFees(), getString(R.string.studentFees));
                        break;*/
                    case R.id.nav_attendance:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_attendance).setChecked(true);
                        //replaceFragment(new FragmentStudentAttendance(), getString(R.string.studentAttendance));
                        break;
                    /*case R.id.nav_birthday:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_birthday).setChecked(true);
                        //replaceFragment(new FragmentBirthday(), getString(R.string.birthday));
                        break;
                    case R.id.nav_enquiry:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_enquiry).setChecked(true);
                        //replaceFragment(new FragmentEnquiry(), getString(R.string.enquiry));
                        break;
                    case R.id.nav_expense:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_expense).setChecked(true);
                        //replaceFragment(new FragmentExpense(), getString(R.string.expense));
                        break;*/
                    case R.id.nav_staff:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_staff).setChecked(true);
                        replaceFragment(new FragmentStaff(), getString(R.string.staff));
                        break;
                    case R.id.nav_staff_attendance:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_staff_attendance).setChecked(true);
                        replaceFragment(new FragmentTakeStaffAttendance(), getString(R.string.staffAttendance));
                        break;
                    case R.id.nav_subject_faculty:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_subject_faculty).setChecked(true);
                        //replaceFragment(new FragmentBatchFaculty(), getString(R.string.batchFaculty));
                        break;
                    case R.id.nav_school:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_school).setChecked(true);
                        replaceFragment(new FragmentInstitute(), getString(R.string.institute));
                        break;
                    case R.id.nav_batch:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_batch).setChecked(true);
                        replaceFragment(new FragmentBatch(), getString(R.string.batch));
                        break;
                    case R.id.nav_section:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_section).setChecked(true);
                        replaceFragment(new FragmentSection(), getString(R.string.section));
                        break;
                    case R.id.nav_fee_structure:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_fee_structure).setChecked(true);
                        replaceFragment(new FragmentFeeStructure(), getString(R.string.feeStructure));
                        break;
                    case R.id.nav_academic_year:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_academic_year).setChecked(true);
                        replaceFragment(new FragmentAcademicYear(), getString(R.string.academicYear));
                        break;
                    case R.id.nav_holiday:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_holiday).setChecked(true);
                        replaceFragment(new FragmentHoliday(), getString(R.string.holiday));
                        break;
                    /*
                    case R.id.nav_home_work:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_home_work).setChecked(true);
                        //replaceFragment(new FragmentHomeWork(), getString(R.string.homeWork));
                        break;
                    case R.id.nav_competition:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_competition).setChecked(true);
                        //replaceFragment(new FragmentCompetitionWinner(), getString(R.string.competition));
                        break;*/
                    case R.id.nav_calender:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_calender).setChecked(true);
                        replaceFragment(new FragmentCalendar(), getString(R.string.academicCalendar));
                        break;
                    /*case R.id.nav_achievement:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_school).setChecked(true);
                        //replaceFragment(new FragmentAchievement(), getString(R.string.achievement));
                        break;*/
                    case R.id.nav_subject:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_subject).setChecked(true);
                        replaceFragment(new FragmentSubject(), getString(R.string.subject));
                        break;
                    case R.id.nav_event_type:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_event_type).setChecked(true);
                        replaceFragment(new FragmentEventType(), getString(R.string.eventType));
                        break;
                    case R.id.nav_expense_category:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_expense_category).setChecked(true);
                        replaceFragment(new FragmentExpenseCategory(), getString(R.string.expenseCategory));
                        break;
                    case R.id.nav_feedback_category:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_feedback_category).setChecked(true);
                        replaceFragment(new FragmentFeedbackCategory(), getString(R.string.feedbackCategory));
                        break;
                    case R.id.nav_fee_Component:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_fee_Component).setChecked(true);
                        replaceFragment(new FragmentFeeComponent(), getString(R.string.feeComponent));
                        break;
                    case R.id.nav_staff_type:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_staff_type).setChecked(true);
                        replaceFragment(new FragmentStaffType(), getString(R.string.staffType));
                        break;
                    case R.id.nav_document_type:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_document_type).setChecked(true);
                        replaceFragment(new FragmentDocumentType(), getString(R.string.documentType));
                        break;
                    case R.id.nav_aboutus:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_aboutus).setChecked(true);
                        replaceFragment(new FragmentAboutUs(), getString(R.string.aboutUs));
                        break;
                    case R.id.nav_appinfo:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_appinfo).setChecked(true);
                        replaceFragment(new FragmentAppInfo(), getString(R.string.appinfo));
                        break;
                    case R.id.nav_support:
                        unCheckAllMenuItems(navigationView.getMenu());
                        menuNav.findItem(R.id.nav_support).setChecked(true);
                        replaceFragment(new FragmentSupport(), getString(R.string.support));
                        break;
                    case R.id.nav_logout:
                        dialog = new SweetAlertDialog(ActivityHome.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Logout?")
                                .setContentText("Do you really want to logout from the App? ")
                                .setConfirmText("OK")
                                .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();

                                    }
                                })
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();

                                        SessionManager session = new SessionManager(getApplication());
                                        session.remove("loggedInUser");
                                        session.remove("loggedInUserId");
                                        session.remove("currentAcademicYear");
                                        session.remove("currentAcademicYearId");
                                        session.clear();
                                        Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
                                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();
                        break;
                }
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            getSupportActionBar().setTitle(R.string.profile);
            //FragmentProfile fragmentProfile = new FragmentProfile();
            //replaceFragment(fragmentProfile, "FRAGMENT_PROFILE");
        }
        return super.onOptionsItemSelected(item);
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.contentLayout, fragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, drawer)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() <= 0) {
            dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Exit App")
                    .setContentText("Do you really want to exit the App? ")
                    .setConfirmText("OK")
                    .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();

                            FragmentManager manager = getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = manager.beginTransaction();
                            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                            //fragmentTransaction.replace(R.id.contentLayout, new FragmentHome()).addToBackStack(null).commit();

                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();

                            finish();
                        }
                    });
            dialog.setCancelable(false);
            dialog.show();
        }
        else {
        }
    }

}
