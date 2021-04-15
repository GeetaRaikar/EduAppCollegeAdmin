package com.padmajeet.techforedu.wowcollege.admin;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Attendance;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAttendanceDaywise extends Fragment {

    private View view = null;
    private String loggedInUserStudentId;
    private LinearLayout llNoList;
    Bundle bundle = new Bundle();
    String academicYearId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference attendanceCollectionRef = db.collection("Attendance");
    private Attendance attendance;
    private ArrayList<Attendance> attendanceList = new ArrayList<>();
    private CalendarView mCalendarView;
    private String studentId;
    private Gson gson;
    private Staff loggedInUser;
    private String loggedInUserId,instituteId;
    private SweetAlertDialog pDialog;

    public FragmentAttendanceDaywise() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson=sessionManager.getString("loggedInUser");
        loggedInUser=gson.fromJson(userJson, Staff.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
        academicYearId= sessionManager.getString("academicYearId");
        instituteId=sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attendance_daywise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.attendanceDaywise));
        mCalendarView=view.findViewById(R.id.calendarView);
        llNoList = view.findViewById(R.id.llNoList);
        studentId = getArguments().getString("studentId");
        getAttendanceList();
    }

    private  void  getAttendanceList(){
        if(attendanceList.size()!=0){
            attendanceList.clear();
        }
        if(pDialog!=null) {
            pDialog.show();
        }
        attendanceCollectionRef
                .whereEqualTo("academicYearId",academicYearId)
                .whereEqualTo("studentId",studentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                attendance = document.toObject(Attendance.class);
                                attendance.setId(document.getId());
                                System.out.println("Attendance "+attendance.getStatus());
                                attendanceList.add(attendance);
                            }
                            if(attendanceList.size()!=0){
                                Collections.sort(attendanceList, new Comparator<Attendance>() {
                                    DateFormat f = new SimpleDateFormat("MM/dd");

                                    @Override
                                    public int compare(Attendance o1, Attendance o2) {
                                        try {
                                            String date1 = String.format("%02d", o1.getCreatedDate().getMonth() + 1) + "/" + String.format("%02d", o1.getCreatedDate().getDate());
                                            String date2 = String.format("%02d", o2.getCreatedDate().getMonth() + 1) + "/" + String.format("%02d", o2.getCreatedDate().getDate());
                                            return f.parse(date1).compareTo(f.parse(date2));
                                        } catch (ParseException e) {
                                            throw new IllegalArgumentException(e);
                                        }
                                    }
                                });

                                List<EventDay> events = new ArrayList<>();
                                for (Attendance attendance : attendanceList) {
                                    if (attendance.getStatus().equalsIgnoreCase("P")) {
                                        Calendar calendar = new GregorianCalendar();
                                        calendar.setTime(attendance.getDate());
                                        events.add(new EventDay(calendar, R.drawable.circle_green_filled,getResources().getColor(R.color.colorBlack)));
                                    } else {
                                        Calendar calendar = new GregorianCalendar();
                                        calendar.setTime(attendance.getDate());
                                        events.add(new EventDay(calendar, R.drawable.circle_red_filled,getResources().getColor(R.color.colorBlack)));
                                    }
                                }


                                mCalendarView.setEvents(events);
                            }else{
                                llNoList.setVisibility(View.VISIBLE);

                            }

                        } else {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            System.out.println("Error getting documents: " + task.getException());
                        }
                    }
                });
        // [END get_all_users]
    }

}
