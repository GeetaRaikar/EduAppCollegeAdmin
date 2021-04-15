package com.padmajeet.techforedu.wowcollege.admin;


import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Attendance;
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Section;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.model.Student;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentTakeStudentAttendance extends Fragment {
    private RecyclerView rvClasses,rvSections;
    private LinearLayout llNoList;
    private GridView gvStudents;
    private MaterialCardView cvAttendanceSummary;
    private PieChart chart;
    private Button btnSave,btnUpdate;
    private Gson gson;
    private Staff loggedInUser;
    private String loggedInUserId,academicYearId,instituteId;
    private Batch selectedBatch;
    private Section selectedSection;
    private TextView tvAttendanceDate,tvAttendanceDateTaken;
    int presentStudents =0;
    int totalStudents = 0;
    FrameLayout flStudents;
    private LinearLayout llButtons;
    private Date attendance_date;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference sectionCollectionRef = db.collection("Section");
    private CollectionReference attendanceCollectionRef = db.collection("Attendance");

    SweetAlertDialog pDialog;
    private List<Batch> batchList = new ArrayList<>();
    private List<Student> studentList = new ArrayList<>();
    private ArrayList<Section> sectionList = new ArrayList<>();
    private List<Attendance> attendanceList = new ArrayList<>();
    private List<StudentAttendance> studentAttendanceList = new ArrayList<>();
    private Date attendanceDate = new Date();
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    int []circles = {R.drawable.circle_blue_filled,R.drawable.circle_brown_filled,R.drawable.circle_color_primary_filled,R.drawable.circle_pink_filled,R.drawable.circle_orange_filled};
    DatePickerDialog picker;
    private boolean isAttendanceTaken;
    private int selectedClassPos = 0;
    private int selectedSectionPos = 0;

    public FragmentTakeStudentAttendance() {
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
        return inflater.inflate(R.layout.fragment_take_student_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvClasses = view.findViewById(R.id.rvClasses);
        rvSections = view.findViewById(R.id.rvSections);
        gvStudents = view.findViewById(R.id.gvStudents);
        btnSave = view.findViewById(R.id.btnSave);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        cvAttendanceSummary = view.findViewById(R.id.cvAttendanceSummary);
        tvAttendanceDate = view.findViewById(R.id.tvAttendanceDate);
        tvAttendanceDateTaken= view.findViewById(R.id.tvAttendanceDateTaken);
        chart = view.findViewById(R.id.chartAttendanceSummary);
        flStudents = view.findViewById(R.id.flStudents);
        llButtons = view.findViewById(R.id.llButtons);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAttendance();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAttendance();
            }
        });
        llNoList = view.findViewById(R.id.llNoList);
        rvClasses.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSections.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);

        tvAttendanceDate.setText(String.format("%02d", day) + "/" + (String.format("%02d", (month + 1))) + "/" + year);
        tvAttendanceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // date picker dialog
                picker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                tvAttendanceDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                String DOA = tvAttendanceDate.getText().toString().trim();
                                try {
                                    attendanceDate = dateFormat.parse(DOA);
                                    getAttendanceOfClassForDate();
                                } catch (ParseException e) {
                                    tvAttendanceDate.setError("DD/MM/YYYY");
                                    tvAttendanceDate.requestFocus();
                                    e.printStackTrace();
                                }

                            }
                        }, year, month, day);
                picker.setTitle("Select Attendance Date");
                picker.getDatePicker().setMaxDate(new Date().getTime());
                picker.show();
            }
        });
        getAllClasses();
    }

    private void updateAttendance() {
        presentStudents = 0;
        totalStudents = studentAttendanceList.size();
        for(StudentAttendance studentAttendance:studentAttendanceList){
            Attendance attendance = studentAttendance.attendance;
            if(attendance.getStatus().equals("P")){
                presentStudents++;
            }
            attendanceCollectionRef.document(attendance.getId()).set(attendance);

        }
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Attendance updated successfully")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        displayChart();
                    }
                });
        dialog.setCancelable(false);
        dialog.show();

    }

    private void submitAttendance() {
        presentStudents = 0;
        totalStudents = studentAttendanceList.size();
        for(StudentAttendance studentAttendance:studentAttendanceList){
            Attendance attendance = studentAttendance.attendance;
            if(attendance.getStatus().equals("P")){
                presentStudents++;
            }
            attendanceCollectionRef.add(attendance);
        }
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Attendance marked successfully")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        displayChart();
                    }
                });
        dialog.setCancelable(false);
        dialog.show();

    }

    private void getAllClasses() {
        if(batchList.size()!=0){
            batchList.clear();
        }
        batchCollectionRef
                .whereEqualTo("instituteId",instituteId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            Batch batch = documentSnapshot.toObject(Batch.class);
                            batch.setId(documentSnapshot.getId());
                            batchList.add(batch);
                        }
                        if(batchList.size()!=0) {
                            BatchAdapter batchAdapter = new BatchAdapter();
                            rvClasses.setAdapter(batchAdapter);
                            selectedBatch = batchList.get(0);
                            getSectionsOfBatch();
                        }else{

                        }
                    }
                })
                ;
        // [END get_all_users]
    }

    class BatchAdapter extends RecyclerView.Adapter<BatchAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvClassName;
            private ImageView ivClassPic;
            private LinearLayout llImage;
            private View row;
            public MyViewHolder(View view) {
                super(view);
                row = view;
                tvClassName = view.findViewById(R.id.tvClassName);
                ivClassPic = view.findViewById(R.id.ivClassPic);
                llImage = view.findViewById(R.id.llImage);
            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_class, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final Batch batch = batchList.get(position);
            holder.tvClassName.setText("" + batch.getName());
            String url = "" + batch.getImageUrl();
            //System.out.println("Image path" + url);
            if (url != null) {
                Glide.with(getContext())
                        .load(url)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_menu_batch)
                        .into(holder.ivClassPic);
            }
            if(selectedClassPos == position){
                holder.tvClassName.setTextColor(getResources().getColor(R.color.colorGreenDark));
                holder.llImage.setBackground(getResources().getDrawable(R.drawable.circle_green));
            }
            else{
                holder.tvClassName.setTextColor(getResources().getColor(R.color.colorBlack));
                holder.llImage.setBackground(null);
            }
            holder.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedClassPos = position;
                    selectedBatch = batch;
                    cvAttendanceSummary.setVisibility(View.GONE);
                    getSectionsOfBatch();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return batchList.size();
        }
    }

    private void getStudentOfBatchSection() {

        if(pDialog != null) {
            pDialog.show();
        }
        cvAttendanceSummary.setVisibility(View.GONE);
        flStudents.setVisibility(View.VISIBLE);
        gvStudents.setVisibility(View.VISIBLE);
        studentList.clear();
        studentAttendanceList.clear();
        studentCollectionRef
                .whereEqualTo("academicYearId",academicYearId)
                .whereEqualTo("currentBatchId", selectedBatch.getId())
                .whereEqualTo("sectionId", selectedSection.getId())
                .whereIn("status", Arrays.asList("A","N"))
                .orderBy("createdDate", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        // System.out.println("Batch  -");
                        if (task.isSuccessful()) {
                            int index = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Student student = document.toObject(Student.class);
                                student.setId(document.getId());
                                studentList.add(student);
                                System.out.println("student "+student.getId());
                                if(!isAttendanceTaken) {
                                    Attendance tempAttendance = new Attendance();
                                    tempAttendance.setAcademicYearId(academicYearId);
                                    tempAttendance.setBatchId(selectedBatch.getId());
                                    tempAttendance.setCreatorId(loggedInUserId);
                                    tempAttendance.setCreatorType("A");
                                    tempAttendance.setModifierId(loggedInUserId);
                                    tempAttendance.setModifierType("A");
                                    tempAttendance.setStudentId(student.getId());
                                    tempAttendance.setSectionId(selectedSection.getId());
                                    tempAttendance.setStatus("P");
                                    tempAttendance.setLate(false);
                                    tempAttendance.setLateTimeInMin(0);
                                    tempAttendance.setDate(attendance_date);
                                    studentAttendanceList.add(new StudentAttendance(student,tempAttendance));
                                }
                                else {
                                    for(int i = 0;i<attendanceList.size();i++){
                                        if(attendanceList.get(i).getStudentId().equals(document.getId())){
                                            studentAttendanceList.add(new StudentAttendance(student,attendanceList.get(i)));
                                            break;
                                        }
                                    }
                                }


                            }
                            if(studentList.size()!=0) {
                                StudentAdapter studentAdapter = new StudentAdapter(getContext(),studentAttendanceList);
                                gvStudents.setAdapter(studentAdapter);
                                llNoList.setVisibility(View.GONE);
                                gvStudents.setVisibility(View.VISIBLE);
                                llButtons.setVisibility(View.VISIBLE);
                            }else {
                                gvStudents.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                                llButtons.setVisibility(View.GONE);
                            }
                            index++;
                        }
                        else {

                        }
                    }
                });
        // [END get_all_users]

    }

    class StudentAdapter extends ArrayAdapter<StudentAttendance> {
        Context context;
        List<StudentAttendance> studentAttendanceList;
        public StudentAdapter(@NonNull Context context,  @NonNull List<StudentAttendance> objects) {
            super(context, R.layout.row_student, objects);
            this.context = context;
            this.studentAttendanceList = objects;
        }

        @Override
        public int getCount() {
            return studentAttendanceList.size();
        }

        @Override
        public StudentAttendance getItem(int position) {
            return studentAttendanceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Student student = studentAttendanceList.get(position).student;
            final Attendance attendance = studentAttendanceList.get(position).attendance;
            View row ;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.row_student,parent,false);
            TextView tvFirstName = row.findViewById(R.id.tvFirstName);
            TextView tvLastName = row.findViewById(R.id.tvLastName);
            ImageView ivProfilePic = row.findViewById(R.id.ivProfilePic);
            final ImageButton ibChoosePhoto = row.findViewById(R.id.ibChoosePhoto);


            tvFirstName.setText(""+student.getFirstName());
            tvLastName.setText(""+student.getLastName());
            String url = student.getImageUrl();
            Glide.with(getContext())
                    .load(url)
                    .fitCenter()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_student)
                    .into(ivProfilePic);

                if(attendance.getStatus().equalsIgnoreCase("P")){
                    ibChoosePhoto.setVisibility(View.VISIBLE);
                }
                else if(attendance.getStatus().equalsIgnoreCase("A")){
                    ibChoosePhoto.setVisibility(View.GONE);
                }

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(ibChoosePhoto.getVisibility()== View.VISIBLE){
                        ibChoosePhoto.setVisibility(View.GONE);
                        attendance.setStatus("A");
                    }
                    else{
                        ibChoosePhoto.setVisibility(View.VISIBLE);
                        attendance.setStatus("P");
                    }
                }
            });
            return row;
        }

    }

    private void getSectionsOfBatch() {
        if(sectionList.size()!=0){
            sectionList.clear();
        }

        sectionCollectionRef
                .whereEqualTo("batchId",selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            Section section = documentSnapshot.toObject(Section.class);
                            section.setId(documentSnapshot.getId());
                            //System.out.println("Section "+document.getId());
                            sectionList.add(section);
                        }
                        if(sectionList.size()!=0) {
                            SectionAdapter sectionAdapter = new SectionAdapter();
                            rvSections.setAdapter(sectionAdapter);
                            selectedSection = sectionList.get(0);
                            getAttendanceOfClassForDate();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        // [END get_all_users]
    }

    class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSectionName;
            private LinearLayout llImage,llSelected;
            private View row;
            public MyViewHolder(View view) {
                super(view);
                row = view;
                tvSectionName = view.findViewById(R.id.tvSectionName);
                llImage = view.findViewById(R.id.llImage);
                llSelected = view.findViewById(R.id.llSelected);
            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_section_circle, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final Section section = sectionList.get(position);
            holder.tvSectionName.setText("" + section.getName());
            int colorCode = position%5;
            holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
            if(selectedSectionPos==position){
                holder.llSelected.setVisibility(View.VISIBLE);
            }
            else {
                holder.llSelected.setVisibility(View.GONE);
            }
            holder.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedSectionPos = position;
                    selectedSection = section;
                    getAttendanceOfClassForDate();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return sectionList.size();
        }
    }

    private void displayChart(){
        chart.clear();
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);

        chart.setDrawCenterText(true);

        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        chart.setMaxAngle(180f); // HALF CHART
        chart.setRotationAngle(180f);
        chart.setCenterTextOffset(0, -20);

        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        chart.setDescription("Attendance Summary");
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        chart.setEntryLabelColor(Color.WHITE);
        //chart.setEntryLabelTypeface(tfRegular);
        chart.setEntryLabelTextSize(12f);

        flStudents.setVisibility(View.GONE);
        cvAttendanceSummary.setVisibility(View.VISIBLE);

        int absentStudents = totalStudents - presentStudents;

        ArrayList<PieEntry> attendanceList = new ArrayList<>();
        attendanceList.add(new PieEntry(presentStudents,"Presentees"));
        attendanceList.add(new PieEntry(absentStudents,"Absentees"));

        PieDataSet dataSet = new PieDataSet(attendanceList, "- Attendance");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        //data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);

        chart.invalidate();
    }

    private void getAttendanceOfClassForDate(){
        String attendanceDate=tvAttendanceDate.getText().toString().trim();
        try {
            attendance_date = dateFormat.parse(attendanceDate);
            attendance_date = Utility.getDateOnly(attendance_date);
            attendanceList.clear();
            attendanceCollectionRef
                    .whereEqualTo("academicYearId",academicYearId)
                    .whereEqualTo("batchId", selectedBatch.getId())
                    .whereEqualTo("sectionId", selectedSection.getId())
                    .whereEqualTo("date",attendance_date)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Attendance attendance = documentSnapshot.toObject(Attendance.class);
                                attendance.setId(documentSnapshot.getId());
                                System.out.println("attendance "+attendance.getStudentId());
                                attendanceList.add(attendance);
                            }
                            System.out.println("attendanceList "+attendanceList.size());
                            if(attendanceList.size()>0){
                                isAttendanceTaken = true;
                                tvAttendanceDateTaken.setText(R.string.attendanceTaken);
                                btnSave.setVisibility(View.GONE);
                                btnUpdate.setVisibility(View.VISIBLE);
                            }
                            else{
                                isAttendanceTaken = false;
                                tvAttendanceDateTaken.setText(R.string.markAttendance);
                                btnSave.setVisibility(View.VISIBLE);
                                btnUpdate.setVisibility(View.GONE);
                            }
                            getStudentOfBatchSection();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("Fetch attendance failed");
                }
            });
        } catch (ParseException e) {
        }


    }

    class StudentAttendance{
        public Student student;
        public Attendance attendance;
        public StudentAttendance(Student s, Attendance a){
            this.student = s;
            this.attendance = a;
        }
    }

}
