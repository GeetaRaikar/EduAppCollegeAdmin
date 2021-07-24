package com.padmajeet.techforedu.wowcollege.admin;


import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Parent;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.model.Student;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;
import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentStudent extends Fragment {
    private View view = null;
    private Spinner spBatch,spSection;
    private String loggedInUserId;
    private List<Batch> batchList = new ArrayList<Batch>();
    private Batch batch;
    private Batch selectedBatch;
    //private List<Section> sectionList = new ArrayList<Section>();
    //private Section section;
    //Section selectedSection;
    private TextView tvCount;
    private Bundle bundle = new Bundle();
    private LinearLayout llNoList;
    private List<Student> studentList = new ArrayList<Student>();
    private Student student;
    private String academicYearId;
    private String selectedBatchStudent;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference parentCollectionRef = db.collection("Parent");
    //private CollectionReference sectionCollectionRef = db.collection("Section");
    private RecyclerView rvStudent;
    private StudentAdapter studentAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Gson gson;
    private SearchView search_view;
    private LinearLayout llTotalStudents;
    private Parent parent;
    private  List<StudentParent> studentParentList=new ArrayList<StudentParent>();
    private StudentParent studentParent;
    private Staff loggedInUser;
    private String instituteId;
    private SweetAlertDialog pDialog;

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
        pDialog=Utility.createSweetAlertDialog(getContext());
    }

    public FragmentStudent() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_student, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.students));
        spBatch = view.findViewById(R.id.spBatch);
        //spSection = view.findViewById(R.id.spSection);
        tvCount = view.findViewById(R.id.tvCount);
        search_view = view.findViewById(R.id.search_view);
        rvStudent = view.findViewById(R.id.rvStudent);
        llTotalStudents = view.findViewById(R.id.llTotalStudents);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvStudent.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addStudent);
        //fab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#mycolor")));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString("selectedBatchStudent", selectedBatchStudent);
                FragmentAddStudent fragmentAddStudent = new FragmentAddStudent();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentAddStudent.setArguments(bundle);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.contentLayout, fragmentAddStudent).addToBackStack(null).commit();
            }
        });


        getBatches();


        return view;
    }

    private void getBatches() {
        if(batchList.size()!=0){
            batchList.clear();
        }
        if(studentList.size()!=0){
            studentList.clear();
        }
        if(studentParentList.size()!=0){
            studentParentList.clear();
        }
        if(pDialog == null && !pDialog.isShowing()){
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo("instituteId",instituteId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                batch = document.toObject(Batch.class);
                                batch.setId(document.getId());
                               // System.out.println("Batch Name-" + batch.getName());
                                batchList.add(batch);
                            }
                            if(batchList.size()!=0) {
                                List<String> batchNameList = new ArrayList<String>();
                                for (Batch batch : batchList) {
                                    batchNameList.add(batch.getName());
                                }
                                ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                                batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spBatch.setAdapter(batchAdaptor);

                                spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        if (studentList != null) {
                                            studentList.clear();
                                        }
                                       // System.out.println("Position " + position);
                                        selectedBatch = batchList.get(position);
                                        gson = Utility.getGson();
                                        selectedBatchStudent = gson.toJson(selectedBatch);
                                        //getSection();
                                        getStudentOfBatch();
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            }else{
                                spBatch.setEnabled(false);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        } else {

                            spBatch.setEnabled(false);
                            llNoList.setVisibility(View.VISIBLE);
                            //Log.w(TAG, "Error getting documents.", task.getException());
                           // System.out.println("Error getting documents: " + task.getException());
                        }
                    }
                });
        // [END get_all_users]

    }
    /*
    private void getSection(){
        if(sectionList.size()!=0){
            sectionList.clear();
        }

        final SweetAlertDialog pDialog;
        pDialog = Utility.createSweetAlertDialog(getContext());
        pDialog.show();
        sectionCollectionRef
                .whereEqualTo("batchId",selectedBatch.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                section = document.toObject(Section.class);
                                section.setId(document.getId());
                                //System.out.println("Section Name-" + section.getName());
                                sectionList.add(section);
                            }
                            if(sectionList.size()!=0) {
                                rvStudent.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                                spSection.setEnabled(true);
                                List<String> sectionNameList = new ArrayList<String>();
                                for (Section section : sectionList) {
                                    sectionNameList.add(section.getName());
                                }
                                ArrayAdapter<String> sectionAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sectionNameList);
                                sectionAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spSection.setAdapter(sectionAdaptor);

                                spSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        if (studentList != null) {
                                            studentList.clear();
                                        }
                                        //System.out.println("Position " + position);
                                        selectedSection = sectionList.get(position);
                                        gson = Utility.getGson();
                                        getStudentOfBatch();

                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            }else{
                                spSection.setEnabled(false);
                                rvStudent.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        } else {

                            spSection.setEnabled(false);
                            llNoList.setVisibility(View.VISIBLE);
                            //Log.w(TAG, "Error getting documents.", task.getException());
                            //System.out.println("Error getting documents: " + task.getException());
                        }
                    }
                });
        // [END get_all_users]

    }
    */
    private class StudentParent{
        private Student student;
        private Parent parent;
    }
    private void getStudentOfBatch() {
        if (studentParentList.size() != 0) {
            studentParentList.clear();
        }
        if(studentList.size()!=0){
            studentList.clear();
        }
        if(pDialog == null && !pDialog.isShowing()){
            pDialog.show();
        }
        studentCollectionRef
                .whereEqualTo("currentBatchId", selectedBatch.getId())
                //.whereEqualTo("sectionId",selectedSection.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                       // System.out.println("Batch  -");
                        if (task.isSuccessful()) {

                            if(studentList.size()!=0){
                                studentList.clear();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                student = document.toObject(Student.class);
                                student.setId(document.getId());
                                studentList.add(student);
                            }
                            if(studentList.size()!=0) {
                                tvCount.setText("");
                                rvStudent.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                                getParentListOfStudentList(studentList);
                            }else {
                                tvCount.setText("");
                                rvStudent.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }


                        } else {
                            //Log.w(TAG, "Error getting documents.", task.getException());
                            rvStudent.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                           // System.out.println("Error getting documents: " + task.getException());
                        }
                    }
                });
        // [END get_all_users]

    }

    private  void  getParentListOfStudentList(List<Student> students){
        if(studentParentList.size()!=0){
            studentParentList.clear();
        }
        for (final Student student:students){
            parentCollectionRef
                    .whereEqualTo("studentId", student.getId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                parent = document.toObject(Parent.class);
                                parent.setId(document.getId());
                               // System.out.println("Student - "+student.getFirstName()+" "+student.getLastName());
                               // System.out.println("Parent - "+parent.getFather()+" "+parent.getMother());
                                studentParent = new StudentParent();
                                studentParent.student = student;
                                studentParent.parent = parent;
                               // System.out.println("StudentParent - "+studentParent.student.getFirstName()+" "+studentParent.parent.getFather()+" "+studentParent.student.getLastName());
                                studentParentList.add(studentParent);
                                if(studentParentList.size() == studentList.size()){
                                    getStudentParentList(studentParentList);
                                }
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }

    }
    private void getStudentParentList(List<StudentParent> studentParentList){
       // System.out.println("Inside getStudentParentList");
        if (studentParentList.size() != 0) {
            rvStudent.setVisibility(View.VISIBLE);
            llNoList.setVisibility(View.GONE);
            llTotalStudents.setVisibility(View.VISIBLE);
            tvCount.setText("" + studentParentList.size());
            studentAdapter = new StudentAdapter(studentParentList);
            rvStudent.setAdapter(studentAdapter);
            search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    //System.out.println("query" + query);
                    studentAdapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    //System.out.println("NewText" + newText);
                    studentAdapter.getFilter().filter(newText);
                    return false;
                }
            });

        } else {
            llTotalStudents.setVisibility(View.GONE);
            rvStudent.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
    }
    class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.MyViewHolder> implements Filterable {
        private List<StudentParent> studentParentList;
        private List<StudentParent> studentParentListFull;

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.folding_cell_student_parent, parent, false);

            return new MyViewHolder(v);
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName,tvMobileNumber,tvFather,tvMother,tvStudentName,tvDOB,tvEmailId,tvMotherMobileNumber,tvAddress,tvEmergencyContact;
            public ImageView ivEditStudentEdit, ivProfilePic,ivParentEdit,ivStudentProfilePic;
            MaterialCardView cv;

           // View mView;
            public MyViewHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvMobileNumber = itemView.findViewById(R.id.tvMobileNumber);
                tvDOB = itemView.findViewById(R.id.tvDOB);
                ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
                ivEditStudentEdit = (ImageView) itemView.findViewById(R.id.ivStudentEdit);
                tvStudentName=itemView.findViewById(R.id.tvStudentName);
                ivStudentProfilePic=itemView.findViewById(R.id.ivStudentProfilePic);
                tvFather=itemView.findViewById(R.id.tvFather);
                tvMotherMobileNumber=itemView.findViewById(R.id.tvMotherMobileNumber);
                tvAddress=itemView.findViewById(R.id.tvAddress);
                tvEmergencyContact=itemView.findViewById(R.id.tvEmergencyContact);
                tvMother=itemView.findViewById(R.id.tvMother);
                tvEmailId = itemView.findViewById(R.id.tvEmailId);
                //ivParentEdit=itemView.findViewById(R.id.ivParentEdit);
                cv =  itemView.findViewById(R.id.card_view);
                final FoldingCell fc = (FoldingCell)itemView.findViewById(R.id.folding_cell);
                fc.initialize(30,1000, Color.DKGRAY, 0);
                fc.setTag(cv);


                fc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fc.toggle(false);
                    }
                });
                //mView=itemView;
            }
        }


        public StudentAdapter(List<StudentParent> studentParentList) {
            this.studentParentList = studentParentList;
            this.studentParentListFull = new ArrayList<>(studentParentList);
        }


        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final StudentParent studentParent = studentParentList.get(position);
            holder.tvName.setText("" + studentParent.student.getFirstName() + " " +studentParent.student.getLastName());

            // [END get_all_users]

            String url = "" + studentParent.student.getImageUrl();
           // System.out.println("Image path" + url);
            if (!TextUtils.isEmpty(url)) {
                Glide.with(getContext())
                        .load(url)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_student)
                        .into(holder.ivProfilePic);
            }
            holder.ivEditStudentEdit.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    //System.out.println("Edit clicked");
                    Bundle bundle = new Bundle();
                    Gson gson = Utility.getGson();
                    String selectedStudentParent = gson.toJson(studentParent);
                    bundle.putString("selectedStudentParent", selectedStudentParent);
                    /*FragmentUpdateStudent fragmentUpdateStudent = new FragmentUpdateStudent();
                    fragmentUpdateStudent.setArguments(bundle);
                    FragmentManager manager =getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.contentLayout, fragmentUpdateStudent).addToBackStack(null).commit();*/


                }
            });
            holder.tvStudentName.setText("" + studentParent.student.getFirstName() + " " +studentParent.student.getLastName());
            holder.tvDOB.setText(""+ Utility.formatDateToString(studentParent.student.getDob().getTime()));
            if (!TextUtils.isEmpty(url)) {
                Glide.with(getContext())
                        .load(url)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_student)
                        .into(holder.ivStudentProfilePic);
            }
            holder.tvFather.setText(""+studentParent.parent.getFirstName());
            holder.tvMobileNumber.setText(""+ studentParent.parent.getMobileNumber());
            holder.tvEmergencyContact.setText(""+studentParent.student.getMobileNumber());
            holder.tvAddress.setText(""+studentParent.parent.getAddress());
            holder.tvEmailId.setText(""+studentParent.parent.getEmailId());
            /*
            holder.ivParentEdit.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    //System.out.println("Edit clicked");
                    Bundle bundle = new Bundle();
                    Gson gson = Utility.getGson();
                    String selectedParent = gson.toJson(studentParent.parent);
                    bundle.putString("selectedParent", selectedParent);
                    FragmentUpdateParent fragmentUpdateParent = new FragmentUpdateParent();
                    fragmentUpdateParent.setArguments(bundle);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.contentLayout, fragmentUpdateParent).addToBackStack(null).commit();


                }
            });

             */

        }

        @Override
        public int getItemCount() {
            return studentParentList.size();
        }

        @Override
        public Filter getFilter() {
            System.out.println("getFilter -");
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    //System.out.println("constraint -" + constraint);
                    FilterResults results = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        results.values = studentParentListFull;
                        results.count = studentParentListFull.size();
                    } else {
                        // Some search copnstraint has been passed
                        // so let's filter accordingly
                        ArrayList<StudentParent> filteredStudents = new ArrayList<StudentParent>();

                        // We'll go through all the contacts and see
                        // if they contain the supplied string
                       // System.out.println("filterPattern -" + constraint.toString().toUpperCase());
                        for (StudentParent sp : studentParentListFull) {
                            if (sp.student.getFirstName().toUpperCase().trim().contains(constraint.toString().toUpperCase())) {
                                // if `contains` == true then add it
                                // to our filtered list
                                //System.out.println("filterPattern Matched -" + sp.student.getFirstName().toUpperCase().trim());
                                filteredStudents.add(sp);
                            }
                        }

                        // Finally set the filtered values and size/count
                        results.values = filteredStudents;
                        results.count = filteredStudents.size();
                    }

                    // Return our FilterResults object
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    studentParentList.clear();
                    studentParentList.addAll((List<StudentParent>) results.values);
                    notifyDataSetChanged();
                }
            };
        }


    }
}
