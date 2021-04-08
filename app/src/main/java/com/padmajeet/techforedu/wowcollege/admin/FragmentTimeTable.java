package com.padmajeet.techforedu.wowcollege.admin;


import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.eduapp.wowcollege.admin.model.Batch;
import com.padmajeet.eduapp.wowcollege.admin.model.Staff;
import com.padmajeet.eduapp.wowcollege.admin.model.Subject;
import com.padmajeet.eduapp.wowcollege.admin.model.TimeTable;
import com.padmajeet.eduapp.wowcollege.admin.util.SessionManager;
import com.padmajeet.eduapp.wowcollege.admin.util.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentTimeTable extends Fragment {
    private View view = null;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private CollectionReference timeTableCollectionRef=db.collection("TimeTable");
    private CollectionReference batchCollectionRef=db.collection("Batch");
    private CollectionReference subjectCollectionRef=db.collection("Subject");
    private CollectionReference staffCollectionRef=db.collection("Staff");
    private TimeTable timeTable;
    private List<TimeTable> timeTableList=new ArrayList<>();
    private Batch batch;
    private List<Batch> batchList=new ArrayList<>();
    private Staff staff;
    private List<Staff> staffList=new ArrayList<>();
    private Subject subject;
    private List<Subject> subjectList=new ArrayList<>();
    private Spinner spBatch, spDay;
    private TextView tvError;
    private Gson gson;
    private LinearLayout llNoList;
    private Staff loggedInUser;
    private String schoolId,academicYearId;
    private Fragment currentFragment = this;
    private List<TimeTable> timeTableDayWiseList = new ArrayList<>();
    private String selectedBatchTimeTable;
    private Subject selectedSubject;
    private Staff selectedStaff;
    private Bundle bundle = new Bundle();
    private Batch selectedBatch;
    private String selectedDay;
    private Spinner spSubject,spStaff;
    private String todaysDay;
    private ArrayAdapter<String> dayAdaptor;
    private SessionManager sessionManager;
    private SweetAlertDialog pDialog;
    private RecyclerView rvTimetable;
    private RecyclerView.Adapter timeTableAdapter;
    private RecyclerView.LayoutManager layoutManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        String userJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(userJson, Staff.class);
        schoolId = sessionManager.getString("schoolId");
        academicYearId = sessionManager.getString("academicYearId");
        pDialog=Utility.createSweetAlertDialog(getContext());
    }
    public FragmentTimeTable() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_time_table, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.timetable));
        rvTimetable = (RecyclerView) view.findViewById(R.id.rvTimeTable);
        layoutManager = new LinearLayoutManager(getContext());
        rvTimetable.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        spBatch = view.findViewById(R.id.spBatch);
        spDay = view.findViewById(R.id.spDay);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addTimeTable);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString("selectedBatchTimeTable", selectedBatchTimeTable);
                /*
                FragmentAddTimeTable fragmentAddTimeTable = new FragmentAddTimeTable();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentAddTimeTable.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.contentLayout, fragmentAddTimeTable).addToBackStack(null).commit();
            */

            }
        });
        // Spinner Drop down elements
        final List<String> days = new ArrayList<String>();
        //days.add("Sunday");
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
        days.add("Saturday");

        dayAdaptor = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, days);
        dayAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDay.setAdapter(dayAdaptor);



        spDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedDay = days.get(position);
               // Toast.makeText(parent.getContext(), "Selected Day: " + selectedDay, Toast.LENGTH_LONG).show();
                if (selectedBatch != null) {
                    //getTimeTableOfBatch();
                    if (timeTableDayWiseList != null) {
                        timeTableDayWiseList.clear();
                    }
                    System.out.println("timeTable List size   "+timeTableList.size());
                    for (TimeTable timeTable : timeTableList) {
                        if (timeTable.getDay().equalsIgnoreCase(selectedDay)) {
                            timeTableDayWiseList.add(timeTable);
                            System.out.println("Equal Day   "+timeTable.getDay());
                            System.out.println("Equal FacultyId   "+timeTable.getFacultyId());
                            System.out.println("Equal SubjectId   "+timeTable.getSubjectId().getId());
                        }
                    }
                    if(timeTableDayWiseList.isEmpty()){
                        //lvTimeTable.setVisibility(View.GONE);
                        llNoList.setVisibility(View.VISIBLE);
                    }
                    else {
                        System.out.println("Inside method");
                        rvTimetable.setVisibility(View.VISIBLE);
                        llNoList.setVisibility(View.GONE);
                        timeTableAdapter = new TimeTableAdapter(timeTableDayWiseList);
                        rvTimetable.setAdapter(timeTableAdapter);
                        timeTableAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getBatchesOfInstitute();
        return view;
    }
    class TimeTableAdapter extends RecyclerView.Adapter<TimeTableAdapter.MyViewHolder> {
        private List<TimeTable> timeTableList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubject,tvTime,tvFaculty,tvMins;
            public ImageView ivEditSubject;

            public MyViewHolder(View view) {
                super(view);
                tvSubject =view.findViewById(R.id.tvSubject);
                tvTime = view.findViewById(R.id.tvTime);
                tvFaculty = view.findViewById(R.id.tvFaculty);
                tvMins =view.findViewById(R.id.tvMins);
                ivEditSubject = view.findViewById(R.id.ivEditSubject);
            }
        }


        public TimeTableAdapter(List<TimeTable> timeTableList) {
            this.timeTableList = timeTableList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_period, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final TimeTable timeTable = timeTableList.get(position);

            holder.tvFaculty.setVisibility(View.VISIBLE);
            holder.tvSubject.setText("" + timeTable.getSubjectId().getName());
            String faculty="";
            if(!TextUtils.isEmpty(timeTable.getFacultyId().getId())) {
                faculty = timeTable.getFacultyId().getFirstName() + " " + timeTable.getFacultyId().getLastName();
                holder.tvFaculty.setText("" + faculty);
            }
            else{
                holder.tvFaculty.setVisibility(View.GONE);
            }
            holder.tvTime.setText("" + timeTable.getPeriodId().getTime());
            holder.tvMins.setText(""+timeTable.getPeriodId().getDuration());
            holder.ivEditSubject.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    //System.out.println("Edit clicked");
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialogLayout = inflater.inflate(R.layout.dialog_update_timetable, null);
                    spStaff=dialogLayout.findViewById(R.id.spStaff);
                    spSubject=dialogLayout.findViewById(R.id.spSubject);
                    getSubjectsOfBatch();
                    getFaculties();
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Replace Faculty")
                            .setConfirmText("Update")
                            .setCustomView(dialogLayout)
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

                                    timeTable.setFacultyId(selectedStaff);
                                    timeTable.setSubjectId(selectedSubject);
                                    timeTable.setModifiedDate(new Date());
                                    timeTable.setModifierId(loggedInUser.getId());
                                    timeTable.setModifierType("A");
                                    updateTimeTable(timeTable);

                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();


                }
            });


        }

        @Override
        public int getItemCount() {
            return batchList.size();
        }
    }
    private void updateTimeTable(final TimeTable currentTimeTable) {
        timeTableCollectionRef
                .document(currentTimeTable.getId())
                .set(currentTimeTable)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"Error",Toast.LENGTH_LONG);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
                    }
                });
    }
    private void getSubjectsOfBatch() {
        if(pDialog == null && !pDialog.isShowing()){
            pDialog.show();
        }
        subjectCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            subject = document.toObject(Subject.class);
                            subject.setId(document.getId());
                            subjectList.add(subject);
                        }
                        if (subjectList.size()!=0) {
                            List<String> subjectNameList = new ArrayList<>();
                            for (Subject subject : subjectList) {
                                subjectNameList.add(subject.getName());
                            }
                            ArrayAdapter<String> subjectAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, subjectNameList);
                            subjectAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spSubject.setAdapter(subjectAdaptor);
                            spSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedSubject = subjectList.get(position);
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        } else {
                            spSubject.setEnabled(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }
                });
    }
    private void getFaculties() {
        if(pDialog == null && !pDialog.isShowing()){
            pDialog.show();
        }
        staffCollectionRef
                .whereEqualTo("schoolId",schoolId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            staff = document.toObject(Staff.class);
                            staff.setId(document.getId());
                            staffList.add(staff);
                        }
                        if (staffList.size()!=0) {
                            List<String> facultyNameList = new ArrayList<>();
                            facultyNameList.add(null);
                            for (Staff staff : staffList) {
                                if(!TextUtils.isEmpty(staff.getFirstName())) {
                                    facultyNameList.add(staff.getFirstName() + " " + staff.getLastName());
                                }
                                else{
                                    staffList.remove(staff);
                                }
                            }
                            ArrayAdapter<String> facultyAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, facultyNameList);
                            facultyAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spStaff.setAdapter(facultyAdaptor);

                            spStaff.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedStaff = staffList.get(position);

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        } else {

                        }


                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }
                });

    }
    private void getBatchesOfInstitute() {
        if(pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo("schoolId",schoolId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            batch = document.toObject(Batch.class);
                            batch.setId(document.getId());
                            batchList.add(batch);
                        }
                        if (batchList.size()!=0) {
                            List<String> batchNameList = new ArrayList<>();
                            for (Batch batch : batchList) {
                                batchNameList.add(batch.getName());
                            }
                            ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                            batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spBatch.setAdapter(batchAdaptor);

                            spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (selectedBatch == null || batchList.get(position).getId() != selectedBatch.getId()) {
                                        if (timeTableList != null) {
                                            timeTableList.clear();
                                        }
                                        if (timeTableDayWiseList != null) {
                                            timeTableDayWiseList.clear();
                                        }
                                        selectedBatch = batchList.get(position);
                                        selectedBatchTimeTable = gson.toJson(selectedBatch);
                                        getTimeTableOfBatch();
                                    }
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });

                        } else {
                            spBatch.setEnabled(false);
                        }


                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        spBatch.setEnabled(false);
                    }
                });
    }
    private void getTimeTableOfBatch() {
        if(pDialog == null && !pDialog.isShowing()){
            pDialog.show();
        }
        timeTableCollectionRef
                .whereEqualTo("batchId",selectedBatch.getId())
                .whereEqualTo("academicYearId",academicYearId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            timeTable = document.toObject(TimeTable.class);
                            timeTable.setId(document.getId());
                            timeTableList.add(timeTable);
                        }
                        if (timeTableList.size()!=0) {
                            java.util.Calendar calendar = java.util.Calendar.getInstance();
                            Date date = calendar.getTime();
                            todaysDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
                            int pos = dayAdaptor.getPosition(todaysDay);
                            spDay.setSelection(pos);
                        } else {
                            rvTimetable.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }


                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }
                });

    }
}
