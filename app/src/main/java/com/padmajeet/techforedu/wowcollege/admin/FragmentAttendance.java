package com.padmajeet.techforedu.wowcollege.admin;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Attendance;
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Section;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.model.Student;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAttendance extends Fragment {

    private View view = null;
    private Spinner spBatch;
    Batch selectedBatch;
    private LinearLayout llNoList;
    Bundle bundle = new Bundle();
    String date;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference attendanceCollectionRef = db.collection("Attendance");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference sectionCollectionRef = db.collection("Section");
    private Batch batch;
    private Attendance attendance;
    private Section section;
    private Student student;
    private ArrayList<Batch> batchList = new ArrayList<>();
    private ArrayList<Attendance> attendanceList = new ArrayList<>();
    private ArrayList<Student> studentList = new ArrayList<>();
    private ArrayList<Section> sectionList = new ArrayList<>();
    private RecyclerView rvAttendance;
    private RecyclerView.Adapter attendanceAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Section selectedSection;
    private Spinner spSection;
    StudentAttendance studentAttendance;
    List<StudentAttendance> studentAttendanceList=new ArrayList<>();
    Gson gson;
    Staff loggedInUser;
    String instituteId;
    SweetAlertDialog pDialog;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson=sessionManager.getString("loggedInUser");
        loggedInUser=gson.fromJson(userJson, Staff.class);
        instituteId=sessionManager.getString("instituteId");

    }

    public FragmentAttendance() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_attendance, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.studentAttendance));
        spBatch = view.findViewById(R.id.spBatch);
        spSection = view.findViewById(R.id.spSection);
        rvAttendance = (RecyclerView) view.findViewById(R.id.rvAttendance);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvAttendance.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        pDialog = Utility.createSweetAlertDialog(getContext());
        getBatches();
        return view;
    }

    public class StudentAttendance{
        int totalCount;
        int presentCount;
        Student student;
    }
    private void getBatches() {
        if(batchList.size()!=0){
            batchList.clear();
        }
        if(pDialog!=null && !pDialog.isShowing()) {
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo("instituteId",instituteId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            batch = documentSnapshot.toObject(Batch.class);
                            batch.setId(documentSnapshot.getId());
                            // System.out.println("Batch Name-" +document.getId());
                            batchList.add(batch);
                        }
                        if(batchList.size()!=0) {
                            llNoList.setVisibility(View.GONE);
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
                                    selectedBatch = batchList.get(position);
                                    // gson = Utility.getGson();
                                    // selectedBatchCalender = gson.toJson(selectedBatch);
                                    getSectionsOfBatch();

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                        else{
                            if(pDialog!=null){
                                pDialog.dismiss();
                            }
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]
    }

    private void getSectionsOfBatch() {
        if(sectionList.size()!=0){
            sectionList.clear();
        }
        if(studentAttendanceList.size()!=0){
            studentAttendanceList.clear();
        }
        if(!pDialog.isShowing()) {
            pDialog.show();
        }
        sectionCollectionRef
                .whereEqualTo("batchId",selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            section = documentSnapshot.toObject(Section.class);
                            section.setId(documentSnapshot.getId());
                            //System.out.println("Section "+document.getId());
                            sectionList.add(section);
                        }
                        if(sectionList.size()!=0) {
                            llNoList.setVisibility(View.GONE);
                            List<String> sectionNameList = new ArrayList<>();
                            for (Section section : sectionList) {
                                sectionNameList.add(section.getName());
                            }
                            ArrayAdapter<String> sectionAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sectionNameList);
                            sectionAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spSection.setAdapter(sectionAdaptor);

                            spSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedSection = sectionList.get(position);
                                    // gson = Utility.getGson();
                                    // selectedBatchCalender = gson.toJson(selectedBatch);
                                    getStudentOfBatch();

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                        else{
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]
    }

    private void getStudentOfBatch() {
        if(studentList.size()!=0){
            studentList.clear();
        }
        if(studentAttendanceList.size()!=0){
            studentAttendanceList.clear();
        }
        if(!pDialog.isShowing()) {
            pDialog.show();
        }
        studentCollectionRef
                .whereEqualTo("currentBatchId",selectedBatch.getId())
                .whereEqualTo("sectionId",selectedSection.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            student = documentSnapshot.toObject(Student.class);
                            student.setId(documentSnapshot.getId());
                            // System.out.println("Student "+document.getId());
                            studentList.add(student);
                        }
                        if(studentList.size()!=0) {
                            rvAttendance.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            getAttendanceList();

                        }else{
                            rvAttendance.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]
    }
    private  void  getAttendanceList(){
        if(attendanceList.size()!=0){
            attendanceList.clear();
        }
        if(studentAttendanceList.size()!=0){
            studentAttendanceList.clear();
        }

        if(!pDialog.isShowing()){
            pDialog.show();
        }

       // System.out.println("CurrentBatch "+selectedBatch.getId());
       // System.out.println("CurrentSection "+selectedSection.getId());
        attendanceCollectionRef
                .whereEqualTo("batchId",selectedBatch.getId())
                .whereEqualTo("sectionId",selectedSection.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            attendance = documentSnapshot.toObject(Attendance.class);
                            attendance.setId(documentSnapshot.getId());
                            //  System.out.println("Attendance "+document.getId());
                            attendanceList.add(attendance);
                        }


                        if(attendanceList.size()!=0) {
                            rvAttendance.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            //totalClassConducted=(attendanceList.size()/studentList.size());
                            for(Student student:studentList){
                                int totalClass=0;
                                int totalPresence=0;
                                studentAttendance=new StudentAttendance();
                                for (Attendance attendance:attendanceList){
                                    if(student.getId().equals(attendance.getStudentId())) {
                                        totalClass++;
                                        if (attendance.getStatus().equalsIgnoreCase("P")) {
                                            totalPresence++;
                                        }
                                    }
                                }
                                //System.out.println("TotalPresent class - "+totalPresence);
                                studentAttendance.totalCount=totalClass;
                                studentAttendance.presentCount=totalPresence;
                                studentAttendance.student=student;
                                studentAttendanceList.add(studentAttendance);
                                // System.out.println("TotalPresent student - "+studentAttendance.presentCount+" / "+studentAttendance.totalCount);
                                if(studentAttendanceList.size()==studentList.size()){
                                    for(int i=0;i<studentAttendanceList.size();i++){
                                        if(studentAttendanceList.get(i).totalCount==0){
                                            studentAttendanceList.remove(studentAttendanceList.get(i));
                                        }
                                    }
                                    //adpter
                                    attendanceAdapter = new AttendanceAdapter(studentAttendanceList);
                                    rvAttendance.setAdapter(attendanceAdapter);
                                }
                            }

                        }else{
                            rvAttendance.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]
    }


    class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.MyViewHolder> {
        private List<StudentAttendance> studentAttendanceList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName, tvTotalClass,tvPresentClass,tvAttendancePercentage;
            private ImageView ivProfilePic;
            private ImageButton ibDaywiseAttendance;

            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
                tvTotalClass = view.findViewById(R.id.tvTotalClass);
                tvPresentClass=view.findViewById(R.id.tvPresentClass);
                ivProfilePic=view.findViewById(R.id.ivProfilePic);
                tvAttendancePercentage = view.findViewById(R.id.tvAttendancePercentage);
                ibDaywiseAttendance = view.findViewById(R.id.ibDaywiseAttendance);
            }
        }


        public AttendanceAdapter(List<StudentAttendance> studentAttendanceList) {
            this.studentAttendanceList = studentAttendanceList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_attendance, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final StudentAttendance studentAttendance = studentAttendanceList.get(position);
            holder.tvName.setText(""+studentAttendance.student.getFirstName()+" "+studentAttendance.student.getLastName());
            holder.tvTotalClass.setText(""+studentAttendance.totalCount);
            holder.tvPresentClass.setText(""+studentAttendance.presentCount);
            float percentage = 0f;
            if(studentAttendance.totalCount>0) {
                percentage = (studentAttendance.presentCount * 100f) / studentAttendance.totalCount;
            }
            holder.tvAttendancePercentage.setText(""+percentage+"%");
            //System.out.println("StudentAttendance - "+studentAttendance.presentCount);
            String url = "" + studentAttendance.student.getImageUrl();
            //System.out.println("Image path" + url);
            if (!TextUtils.isEmpty(url)) {
                Glide.with(getContext())
                        .load(url)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_student)
                        .into(holder.ivProfilePic);
            }

            holder.ibDaywiseAttendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("studentId", studentAttendance.student.getId());
                    FragmentAttendanceDaywise fragmentAttendanceDaywise = new FragmentAttendanceDaywise();
                    fragmentAttendanceDaywise.setArguments(bundle);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.contentLayout, fragmentAttendanceDaywise);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return studentAttendanceList.size();
        }
    }


}
