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
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.model.StaffAttendance;
import com.padmajeet.techforedu.wowcollege.admin.model.StaffType;
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
public class FragmentTakeStaffAttendance extends Fragment {
    private View view=null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference staffCollectionRef = db.collection("Staff");
    private CollectionReference staffTypeCollectionRef = db.collection("StaffType");
    private CollectionReference staffAttendanceCollectionRef = db.collection("StaffAttendance");
    private String loggedInUserId,academicYearId,schoolId;
    private Gson gson;
    private Staff loggedInUser;
    private StaffType selectedStaffType;
    private int presentStaffs =0;
    private int totalStaffs = 0;
    private RecyclerView rvStaffType;
    private LinearLayout llNoList;
    private GridView gvStaffs;
    private MaterialCardView cvAttendanceSummary;
    private PieChart chart;
    private Button btnSave,btnUpdate;
    private FrameLayout flStaffs;
    private LinearLayout llButtons;
    private TextView tvAttendanceDate,tvAttendanceDateTaken;
    private SweetAlertDialog pDialog;
    private Date attendance_date;
    private List<StaffType> staffTypeList = new ArrayList<>();
    private List<Staff> staffList = new ArrayList<>();
    private List<StaffAttendance> attendanceList = new ArrayList<>();
    private List<EmployeeAttendance> employeeAttendanceList=new ArrayList<>();
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private int []circles = {R.drawable.circle_blue_filled,R.drawable.circle_brown_filled,R.drawable.circle_color_primary_filled,R.drawable.circle_pink_filled,R.drawable.circle_orange_filled};
    private DatePickerDialog picker;
    private boolean isAttendanceTaken;
    private int selectedStaffTypePos = 0;

    public FragmentTakeStaffAttendance() {
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
        schoolId=sessionManager.getString("schoolId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_take_staff_attendance, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.staffAttendance));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.staffAttendance));
        rvStaffType = view.findViewById(R.id.rvStaffType);
        gvStaffs = view.findViewById(R.id.gvStaffs);
        btnSave = view.findViewById(R.id.btnSave);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        cvAttendanceSummary = view.findViewById(R.id.cvAttendanceSummary);
        tvAttendanceDate = view.findViewById(R.id.tvAttendanceDate);
        tvAttendanceDateTaken= view.findViewById(R.id.tvAttendanceDateTaken);
        chart = view.findViewById(R.id.chartAttendanceSummary);
        flStaffs = view.findViewById(R.id.flStaffs);
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
        rvStaffType.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

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
                                    attendance_date = dateFormat.parse(DOA);
                                    getAttendanceOfStaffTypeForDate();
                                } catch (ParseException e) {
                                    tvAttendanceDate.setError("DD/MM/YYYY");
                                    tvAttendanceDate.requestFocus();
                                    e.printStackTrace();
                                }

                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());
                picker.setTitle("Select Attendance Date");
                picker.getDatePicker().setMaxDate(new Date().getTime());
                picker.show();
            }
        });
        String DOA = tvAttendanceDate.getText().toString().trim();
        try {
            attendance_date = dateFormat.parse(DOA);
            getAllStaffTypes();
        } catch (ParseException e) {
            tvAttendanceDate.setError("DD/MM/YYYY");
            tvAttendanceDate.requestFocus();
            e.printStackTrace();
        }
    }

    private void updateAttendance() {
        presentStaffs = 0;
        totalStaffs = employeeAttendanceList.size();
        for(EmployeeAttendance employeeAttendance:employeeAttendanceList){
            StaffAttendance staffAttendance = employeeAttendance.staffAttendance;
            if(staffAttendance.getStatus().equals("F")){
                presentStaffs++;
            }
            staffAttendanceCollectionRef.document(staffAttendance.getId()).set(staffAttendance);

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
        presentStaffs = 0;
        totalStaffs = employeeAttendanceList.size();
        for(EmployeeAttendance employeeAttendance:employeeAttendanceList){
            StaffAttendance staffAttendance = employeeAttendance.staffAttendance;
            if(staffAttendance.getStatus().equals("F")){
                presentStaffs++;
            }
            staffAttendanceCollectionRef.add(staffAttendance);
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

    private void getAllStaffTypes() {
        if(pDialog==null && !pDialog.isShowing()){
            pDialog.show();
        }
        if(staffTypeList.size()!=0){
            staffTypeList.clear();
        }
        staffTypeCollectionRef
                .whereEqualTo("schoolId",schoolId)
                .orderBy("type", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(pDialog!=null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        if(task.isSuccessful()){
                            for (DocumentSnapshot documentSnapshot:task.getResult()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                StaffType staffType = documentSnapshot.toObject(StaffType.class);
                                staffType.setId(documentSnapshot.getId());
                                staffTypeList.add(staffType);
                            }
                            if(staffTypeList.size()!=0) {
                                StaffTypeAdapter staffTypeAdapter = new StaffTypeAdapter();
                                rvStaffType.setAdapter(staffTypeAdapter);
                                selectedStaffType = staffTypeList.get(0);
                                getAttendanceOfStaffTypeForDate();
                            }else{
                                gvStaffs.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                                btnSave.setVisibility(View.GONE);
                                btnUpdate.setVisibility(View.GONE);
                            }
                        }
                    }
                });
        // [END get_all_users]
    }

    class StaffTypeAdapter extends RecyclerView.Adapter<StaffTypeAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvEmployeeTypeName;
            private ImageView ivEmployeeTypePic;
            private LinearLayout llImage;
            private View row;
            public MyViewHolder(View view) {
                super(view);
                row = view;
                ivEmployeeTypePic = view.findViewById(R.id.ivEmployeeTypePic);
                tvEmployeeTypeName = view.findViewById(R.id.tvEmployeeTypeName);
                llImage = view.findViewById(R.id.llImage);
            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_employee_type, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final StaffType staffType = staffTypeList.get(position);
            holder.tvEmployeeTypeName.setText("" + staffType.getType());
            String url = "" + staffType.getImageUrl();
            //System.out.println("Image path" + url);
            if (url != null) {
                Glide.with(getContext())
                        .load(url)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_male_profile)
                        .into(holder.ivEmployeeTypePic);
            }
            if(selectedStaffTypePos == position){
                holder.tvEmployeeTypeName.setTextColor(getResources().getColor(R.color.colorGreenDark));
                holder.llImage.setBackground(getResources().getDrawable(R.drawable.circle_green));
            }
            else{
                holder.tvEmployeeTypeName.setTextColor(getResources().getColor(R.color.colorBlack));
                holder.llImage.setBackground(null);
            }
            holder.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedStaffTypePos = position;
                    selectedStaffType = staffType;
                    cvAttendanceSummary.setVisibility(View.GONE);
                    getAttendanceOfStaffTypeForDate();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return staffTypeList.size();
        }
    }

    private void getStaffOfStaffType() {

        if(pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        cvAttendanceSummary.setVisibility(View.GONE);
        flStaffs.setVisibility(View.VISIBLE);
        gvStaffs.setVisibility(View.VISIBLE);
        if(staffList.size()>0) {
            staffList.clear();
        }
        if(employeeAttendanceList.size()>0){
            employeeAttendanceList.clear();
        }
        staffCollectionRef
                .whereEqualTo("schoolId",schoolId)
                .whereEqualTo("staffTypeId", selectedStaffType.getId())
                .whereIn("status", Arrays.asList("A","F"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        if (task.isSuccessful()) {
                            int index = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Staff staff = document.toObject(Staff.class);
                                staff.setId(document.getId());
                                staffList.add(staff);
                                if(!isAttendanceTaken) {
                                    StaffAttendance tempAttendance = new StaffAttendance();
                                    tempAttendance.setAcademicYearId(academicYearId);
                                    tempAttendance.setDate(attendance_date);
                                    tempAttendance.setIsLate(false);
                                    tempAttendance.setLateTimeInMin(0);
                                    tempAttendance.setSchoolId(schoolId);
                                    tempAttendance.setStatus("F");
                                    tempAttendance.setUserId(staff.getId());
                                    tempAttendance.setUserTypeId(selectedStaffType.getId());
                                    tempAttendance.setCreatorId(loggedInUserId);
                                    tempAttendance.setModifierId(loggedInUserId);
                                    tempAttendance.setCreatorType("A");
                                    tempAttendance.setModifierType("A");
                                    employeeAttendanceList.add(new EmployeeAttendance(staff,tempAttendance));
                                }
                                else {
                                    for(int i = 0;i<attendanceList.size();i++){
                                        if(attendanceList.get(i).getUserId().equals(document.getId())){
                                            employeeAttendanceList.add(new EmployeeAttendance(staff,attendanceList.get(i)));
                                            break;
                                        }
                                    }
                                }
                            }
                            if(employeeAttendanceList.size()!=0) {
                                System.out.println("employeeAttendanceList "+employeeAttendanceList.size());
                                StaffAdapter staffAdapter = new StaffAdapter(getContext(),employeeAttendanceList);
                                gvStaffs.setAdapter(staffAdapter);
                                llNoList.setVisibility(View.GONE);
                                gvStaffs.setVisibility(View.VISIBLE);
                                llButtons.setVisibility(View.VISIBLE);
                            }else {
                                gvStaffs.setVisibility(View.GONE);
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

    class StaffAdapter extends ArrayAdapter<EmployeeAttendance> {
        Context context;
        List<EmployeeAttendance> employeeAttendanceList;
        public StaffAdapter(@NonNull Context context,  @NonNull List<EmployeeAttendance> objects) {
            super(context, R.layout.row_employee, objects);
            this.context = context;
            this.employeeAttendanceList = objects;
        }

        @Override
        public int getCount() {
            return employeeAttendanceList.size();
        }

        @Override
        public EmployeeAttendance getItem(int position) {
            return employeeAttendanceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Staff staff = employeeAttendanceList.get(position).staff;
            final StaffAttendance attendance = employeeAttendanceList.get(position).staffAttendance;
            View row ;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.row_employee,parent,false);
            TextView tvFirstName = row.findViewById(R.id.tvFirstName);
            TextView tvLastName = row.findViewById(R.id.tvLastName);
            ImageView ivProfilePic = row.findViewById(R.id.ivProfilePic);
            final ImageButton ibChoosePhoto = row.findViewById(R.id.ibChoosePhoto);


            tvFirstName.setText(""+staff.getFirstName());
            tvLastName.setText(""+staff.getLastName());
            String url = staff.getImageUrl();
            Glide.with(getContext())
                    .load(url)
                    .fitCenter()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_male_profile)
                    .into(ivProfilePic);

            if(attendance.getStatus().equalsIgnoreCase("F")){
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
                        attendance.setStatus("F");
                    }
                }
            });
            return row;
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

        flStaffs.setVisibility(View.GONE);
        cvAttendanceSummary.setVisibility(View.VISIBLE);

        int absentStudents = totalStaffs - presentStaffs;

        ArrayList<PieEntry> attendanceList = new ArrayList<>();
        attendanceList.add(new PieEntry(presentStaffs,"Presents"));
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

    private void getAttendanceOfStaffTypeForDate(){
        if(attendanceList.size()>0) {
            attendanceList.clear();
        }
        staffAttendanceCollectionRef
                .whereEqualTo("schoolId",schoolId)
                .whereEqualTo("academicYearId", academicYearId)
                .whereEqualTo("userTypeId", selectedStaffType.getId())
                .whereEqualTo("date",attendance_date)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            StaffAttendance staffAttendance = documentSnapshot.toObject(StaffAttendance.class);
                            staffAttendance.setId(documentSnapshot.getId());
                            System.out.println("attendance "+staffAttendance.getUserId());
                            attendanceList.add(staffAttendance);
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
                        getStaffOfStaffType();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Fetch attendance failed");
            }
        });
    }

    class EmployeeAttendance{
        public Staff staff;
        public StaffAttendance staffAttendance;
        public EmployeeAttendance(Staff s, StaffAttendance a){
            this.staff = s;
            this.staffAttendance = a;
        }
    }

}
