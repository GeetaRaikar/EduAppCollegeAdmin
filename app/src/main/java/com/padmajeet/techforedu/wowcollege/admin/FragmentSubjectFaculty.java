package com.padmajeet.techforedu.wowcollege.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Section;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.model.StaffType;
import com.padmajeet.techforedu.wowcollege.admin.model.Subject;
import com.padmajeet.techforedu.wowcollege.admin.model.SubjectFaculty;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSubjectFaculty extends Fragment {

    private View view = null;
    private Spinner spBatch;
    private List<Batch> batchList = new ArrayList<>();
    private LinearLayout llNoList;
    private Batch selectedBatch;
    private Batch batch;
    private Bundle bundle = new Bundle();
    private Spinner spFaculty;
    private ArrayList<Staff> staffList = new ArrayList<>();
    private Staff selectedStaff = null;
    private Fragment currentFragment = this;
    private List<SubjectFaculty> subjectFacultyList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference subjectFacultyCollectionRef = db.collection("SubjectFaculty");
    private CollectionReference staffCollectionRef = db.collection("Staff");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private DocumentReference staffDocRef;
    private SubjectFaculty subjectFaculty;
    private String loggedInUserId, instituteId;
    private Spinner spSubject;
    private Subject subject;
    private String subjectId;
    private List<Subject> subjectList = new ArrayList<>();
    private Staff staff;
    private RecyclerView rvSubjectFaculty;
    private RecyclerView.Adapter subjectFacultyAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Button btnSave;
    private Spinner spClass;
    private Subject selectedSubject;
    private TextView tvError;
    private Staff loggedInUser;
    private Gson gson;
    private SweetAlertDialog pDialog;
    int subSpinnerPos;
    int mainSpinnerPos;
    private String staffTypeId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(userJson, Staff.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
        instituteId = sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    public FragmentSubjectFaculty() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_subject_faculty, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.batchFaculty));

        rvSubjectFaculty = (RecyclerView) view.findViewById(R.id.rvSubjectFaculty);
        spBatch = view.findViewById(R.id.spBatch);
        getBatches();

        db.collection("StaffType")
                .whereEqualTo("instituteId", instituteId)
                .whereEqualTo("isMandatory",true)
                .whereEqualTo("staffCode","F")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            staffTypeId=documentSnapshot.getId();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
        // [END get_all_users]


        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvSubjectFaculty.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addSubjectFaculty);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBottomSheet();
                bottomSheetDialog.show();
            }
        });
        //getBatches();

        return view;
    }


    BottomSheetDialog bottomSheetDialog;

    private void createBottomSheet() {
        if (bottomSheetDialog == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_subject_faculty, null);
            bottomSheetDialog = new BottomSheetDialog(getContext());//new BottomSheetDialog(this,R.style.BottomSheetDialog)
            bottomSheetDialog.setContentView(view);
            tvError = view.findViewById(R.id.tvError);
            spClass = view.findViewById(R.id.spClass);
            spSubject = view.findViewById(R.id.spSection);
            spFaculty = view.findViewById(R.id.spFaculty);
            btnSave = view.findViewById(R.id.btnSave);
            if (batchList.size() != 0) {
                spClass.setEnabled(true);
                List<String> batchNameList = new ArrayList<>();
                for (Batch batch : batchList) {
                    batchNameList.add(batch.getName());
                }
                ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spClass.setAdapter(batchAdaptor);

                spClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedBatch = batchList.get(position);
                        subSpinnerPos = position;
                        getSubjects();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
            getFaculties();
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvError.setVisibility(View.GONE);
                    if (selectedBatch == null) {
                        tvError.setText("Select the batch");
                        tvError.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (selectedSubject == null) {
                        tvError.setText("Select the subject");
                        tvError.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (selectedStaff == null) {
                        tvError.setText("Select the faculty");
                        tvError.setVisibility(View.VISIBLE);
                        return;
                    }
                    subjectId = selectedSubject.getId();
                    if (pDialog == null && !pDialog.isShowing()) {
                        pDialog.show();
                    }
                    // System.out.println("Section ID - "+sectionId);
                    subjectFaculty = new SubjectFaculty();
                    subjectFaculty.setFacultyId(selectedStaff.getId());
                    subjectFaculty.setSubjectId(subjectId);
                    subjectFaculty.setCreatorId(loggedInUserId);
                    subjectFaculty.setModifierId(loggedInUserId);
                    subjectFaculty.setStatus("A");
                    subjectFaculty.setCreatorType("A");
                    subjectFaculty.setModifierType("A");

                    addSubjectFaculty(subjectFaculty);
                }
            });
        }
    }

    private void addSubjectFaculty(SubjectFaculty subjectFaculty) {
        bottomSheetDialog.dismiss();
        subjectFacultyCollectionRef
                .whereEqualTo("subjectId", selectedSubject.getId())
                .whereEqualTo("staffId", selectedStaff.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                if (pDialog != null) {
                                    pDialog.dismiss();
                                }
                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("Already one teacher has been assigned to this subject")
                                        .setContentText("Please edit it")
                                        .setConfirmText("Ok")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                sDialog.dismissWithAnimation();

                                            }
                                        });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                subjectFacultyCollectionRef
                                        .add(subjectFaculty)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Add")
                                                        .setContentText("Subject teacher successfully added")
                                                        .setConfirmText("Ok")
                                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sDialog) {
                                                                sDialog.dismissWithAnimation();
                                                                if (mainSpinnerPos == subSpinnerPos) {
                                                                    getSubjectFaculty();
                                                                } else {
                                                                    spBatch.setSelection(subSpinnerPos, true);
                                                                }
                                                            }
                                                        });
                                                dialog.setCancelable(false);
                                                dialog.show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                if (pDialog != null) {
                                                    pDialog.dismiss();
                                                }
                                                //Log.w(TAG, "Error adding document", e);
                                                Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                // [END add_document]
                            }
                        }
                    }
                });


    }

    private void getBatches() {
        if (batchList.size() != 0) {
            batchList.clear();
        }
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo("instituteId", instituteId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            batch = documentSnapshot.toObject(Batch.class);
                            batch.setId(documentSnapshot.getId());
                            batchList.add(batch);
                        }
                        if (batchList.size() != 0) {
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
                                    mainSpinnerPos = position;
                                    getSubjectFaculty();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]


    }

    private void getSubjectFaculty() {
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        subjectFacultyCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (subjectFacultyList.size() != 0) {
                            subjectFacultyList.clear();
                        }
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            subjectFaculty = document.toObject(SubjectFaculty.class);
                            subjectFaculty.setId(document.getId());
                            subjectFacultyList.add(subjectFaculty);
                        }
                        if (subjectFacultyList.size() != 0) {
                            // System.out.println("BatchFaculty size-" + batchFacultyList.size());
                            rvSubjectFaculty.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            subjectFacultyAdapter = new SubjectFacultyAdapter(subjectFacultyList);
                            rvSubjectFaculty.setAdapter(subjectFacultyAdapter);
                        } else {
                            rvSubjectFaculty.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        rvSubjectFaculty.setVisibility(View.GONE);
                        llNoList.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void getSubjects() {
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        subjectCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        if (task.isSuccessful()) {

                            if (subjectList.size() != 0) {
                                subjectList.clear();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                subject = document.toObject(Subject.class);
                                subject.setId(document.getId());
                                subjectList.add(subject);
                            }
                            if (subjectList.size() != 0) {
                                spSubject.setEnabled(true);
                                //System.out.println("BatchFaculty sections-" + sectionList.size());
                                List<String> subjectNameList = new ArrayList<>();
                                for (Subject subject : subjectList) {
                                    subjectNameList.add(subject.getName());
                                }
                                ArrayAdapter<String> sectionAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, subjectNameList);
                                sectionAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spSubject.setAdapter(sectionAdaptor);

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
                        } else {
                            //Log.w(TAG, "Error getting documents.", task.getException());
                            // System.out.println("Error getting documents: " + task.getException());
                        }
                    }
                });
        // [END get_all_users]

    }

    private void getFaculties() {
        if (staffList.size() != 0) {
            staffList.clear();
        }
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        staffCollectionRef
                .whereEqualTo("instituteId", instituteId)
                .whereEqualTo("staffTypeId", staffTypeId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            staff = documentSnapshot.toObject(Staff.class);
                            staff.setId(documentSnapshot.getId());
                            staffList.add(staff);
                        }
                        List<String> facultyNameList = new ArrayList<>();
                        for (Staff staff : staffList) {
                            facultyNameList.add(staff.getFirstName() + " " + staff.getLastName());
                        }
                        ArrayAdapter<String> facultyAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, facultyNameList);
                        facultyAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spFaculty.setAdapter(facultyAdaptor);

                        spFaculty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedStaff = staffList.get(position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]


    }

    class SubjectFacultyAdapter extends RecyclerView.Adapter<SubjectFacultyAdapter.MyViewHolder> {
        private List<SubjectFaculty> subjectFacultyList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvFaculty, tvSubject;
            public ImageView ivEditSubjectFaculty, ivProfilePic;

            public MyViewHolder(View view) {
                super(view);
                tvSubject = view.findViewById(R.id.tvSubject);
                tvFaculty = (TextView) view.findViewById(R.id.tvFaculty);
                ivEditSubjectFaculty = view.findViewById(R.id.ivEditSubjectFaculty);
                ivProfilePic = view.findViewById(R.id.ivProfilePic);
            }
        }


        public SubjectFacultyAdapter(List<SubjectFaculty> batchFacultyList) {
            this.subjectFacultyList = batchFacultyList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_subject_faculty, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final SubjectFaculty subjectFaculty = subjectFacultyList.get(position);
            subjectCollectionRef.document(subjectFaculty.getSubjectId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            subject = documentSnapshot.toObject(Subject.class);
                            subject.setId(documentSnapshot.getId());
                            //System.out.println("FacultyId - "+faculty.getFirstName());
                            holder.tvSubject.setText("" + subject.getName());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
            staffCollectionRef.document(subjectFaculty.getFacultyId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            staff = documentSnapshot.toObject(Staff.class);
                            staff.setId(documentSnapshot.getId());
                            //System.out.println("FacultyId - "+faculty.getFirstName());
                            holder.tvFaculty.setText("" + staff.getTitle() + staff.getFirstName() + " " + staff.getLastName());
                            String url = "" + staff.getImageUrl();
                            //System.out.println("Image path" + url);
                            if (url != null) {
                                Glide.with(getContext())
                                        .load(url)
                                        .fitCenter()
                                        .apply(RequestOptions.circleCropTransform())
                                        .placeholder(R.drawable.ic_faculty)
                                        .into(holder.ivProfilePic);
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


            holder.ivEditSubjectFaculty.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    //System.out.println("Edit clicked");

                    spFaculty = new Spinner(getContext(), Spinner.MODE_DIALOG);
                    getFaculties();
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Replace Faculty")
                            .setConfirmText("Update")
                            .setCustomView(spFaculty)
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    for (SubjectFaculty sf : subjectFacultyList) {
                                        if (sf.getFacultyId().equalsIgnoreCase(selectedStaff.getId()) && sf.getSubjectId().equalsIgnoreCase(subjectFaculty.getSubjectId())) {
                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                                    .setTitleText("Already this teacher has been assigned to this subject")
                                                    .setConfirmText("Ok")
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            sweetAlertDialog.dismissWithAnimation();
                                                        }
                                                    });
                                            dialog.setCancelable(false);
                                            dialog.show();
                                            return;
                                        }
                                    }
                                    sDialog.dismissWithAnimation();
                                    if(pDialog==null && !pDialog.isShowing()){
                                        pDialog.show();
                                    }
                                    subjectFaculty.setFacultyId(selectedStaff.getId());
                                    subjectFaculty.setModifiedDate(new Date());
                                    subjectFaculty.setModifierId(loggedInUserId);
                                    subjectFaculty.setModifierType("A");
                                    subjectFacultyCollectionRef.document(subjectFaculty.getId()).set(subjectFaculty).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(pDialog!=null && pDialog.isShowing()){
                                                pDialog.dismiss();
                                            }
                                            if (task.isSuccessful()) {
                                                getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();

                                            } else {
                                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();


                }
            });


        }

        @Override
        public int getItemCount() {
            return subjectFacultyList.size();
        }
    }

}
