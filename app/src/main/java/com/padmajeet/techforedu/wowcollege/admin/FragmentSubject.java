package com.padmajeet.techforedu.wowcollege.admin;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.eduapp.wowcollege.admin.model.Batch;
import com.padmajeet.eduapp.wowcollege.admin.model.Staff;
import com.padmajeet.eduapp.wowcollege.admin.model.Subject;
import com.padmajeet.eduapp.wowcollege.admin.util.SessionManager;
import com.padmajeet.eduapp.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSubject extends Fragment {
    private View view;
    private LinearLayout llNoList;
    private ArrayList<Batch> batchList = new ArrayList<>();
    private ArrayList<Subject> subjectList = new ArrayList<>();
    private String academicYearId,loggedInUserId,instituteId;
    private Batch batch,selectedBatch;
    private Subject subject;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference homeworkCollectionRef = db.collection("HomeWork");
    private ListenerRegistration subjectListener;
    private List<String> alreadySubjectList = new ArrayList<>();
    private EditText etSubjectName;
    private RecyclerView rvSubject;
    private RecyclerView.Adapter subjectAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Spinner spBatch;
    private Staff loggedInUser;
    private Gson gson;
    int subSpinnerPos;
    int mainSpinnerPos;
    private SweetAlertDialog pDialog;
    private ImageButton btnSubmit;
    private String name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(userJson, Staff.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
        academicYearId = sessionManager.getString("academicYearId");
        instituteId =sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (subjectListener != null) {
            subjectListener.remove();
        }
    }
    public void getBatches() {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo("instituteId", instituteId)
                //.orderBy("eligibleYears", Query.Direction.ASCENDING)
                //.orderBy("eligibleMonths", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (batchList.size() != 0) {
                            batchList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            batch = document.toObject(Batch.class);
                            batch.setId(document.getId());
                            batchList.add(batch);
                        }
                        if (batchList.size() != 0) {
                            llNoList.setVisibility(View.GONE);
                            rvSubject.setVisibility(View.VISIBLE);
                            spBatch.setEnabled(true);
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
                                    getSubject();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        } else {
                            llNoList.setVisibility(View.VISIBLE);
                            rvSubject.setVisibility(View.GONE);
                            spBatch.setEnabled(false);
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        }
                    }
                });

    }


    public FragmentSubject() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_subject, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.subject));
        spBatch = view.findViewById(R.id.spBatch);
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        etSubjectName = (EditText) view.findViewById(R.id.etSubjectName);
        btnSubmit = (ImageButton) view.findViewById(R.id.btnSubmit);
        rvSubject = (RecyclerView) view.findViewById(R.id.rvSubject);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvSubject.setLayoutManager(layoutManager);
        getBatches();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etSubjectName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etSubjectName.setError("Enter Section Name");
                    etSubjectName.requestFocus();
                    return;
                } else {
                    //TODO
                    String Name = name.replaceAll("\\s+", "");
                    System.out.println("Name " + Name);
                    for (int i = 0; i < alreadySubjectList.size(); i++) {
                        String subjectName = alreadySubjectList.get(i).replaceAll("\\s+", "");
                        if (Name.equalsIgnoreCase(subjectName)) {
                            System.out.println("equal");
                            etSubjectName.setError("Already this subject is saved ");
                            etSubjectName.requestFocus();
                            return;
                        }
                    }
                }

                if (pDialog == null && !pDialog.isShowing()) {
                    pDialog.show();
                }
                subject = new Subject();
                subject.setBatchId(selectedBatch.getId());
                subject.setStatus("A");
                subject.setName(name);
                subject.setCreatorId(loggedInUserId);
                subject.setModifierId(loggedInUserId);
                subject.setCreatorType("A");
                subject.setModifierType("A");
                addSubject();
            }
        });
        return view;
    }

    private void getSubject() {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        subjectListener = subjectCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .orderBy("createdDate", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (subjectList.size() != 0) {
                            subjectList.clear();
                        }
                        if (alreadySubjectList.size() != 0) {
                            alreadySubjectList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            subject = document.toObject(Subject.class);
                            subject.setId(document.getId());
                            alreadySubjectList.add(subject.getName());
                            subjectList.add(subject);
                        }
                        if (subjectList.size() != 0) {
                            subjectAdapter = new SubjectAdapter(subjectList);
                            rvSubject.setAdapter(subjectAdapter);
                            rvSubject.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        } else {
                            rvSubject.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.MyViewHolder> {
        private List<Subject> subjectList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubjectName;
            public ImageView ivDeleteSubject, ivEditSubject;

            public MyViewHolder(View view) {
                super(view);
                tvSubjectName = view.findViewById(R.id.tvSubjectName);
                ivDeleteSubject = view.findViewById(R.id.ivDeleteSubject);
                ivEditSubject = view.findViewById(R.id.ivEditSubject);
            }
        }


        public SubjectAdapter(List<Subject> subjectList) {
            this.subjectList = subjectList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_subject, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Subject subject = subjectList.get(position);
            holder.tvSubjectName.setText("" + subject.getName());

            holder.ivEditSubject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText editText = new EditText(getContext());
                    editText.setText(subject.getName());
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit subject")
                            .setConfirmText("Update")
                            .setCustomView(editText)
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    String name = editText.getText().toString().trim();
                                    if (TextUtils.isEmpty(name)) {
                                        editText.setError("Enter subject");
                                        editText.requestFocus();
                                        return;
                                    }
                                    String Name = subject.getName();
                                    Name = Name.replaceAll("\\s+", "");
                                    System.out.println("Name " + Name);
                                    String EditName = name.replaceAll("\\s+", "");
                                    System.out.println("EditName " + EditName);
                                    if (!Name.equalsIgnoreCase(EditName)) {
                                        for (int i = 0; i < alreadySubjectList.size(); i++) {
                                            String subject_name = alreadySubjectList.get(i).replaceAll("\\s+", "");
                                            if (EditName.equalsIgnoreCase(subject_name)) {
                                                editText.setError("Already this subject is saved");
                                                editText.requestFocus();
                                                return;
                                            }
                                        }
                                    }
                                    subject.setName(name);
                                    subject.setModifiedDate(new Date());
                                    subject.setModifierId(loggedInUserId);
                                    if (pDialog == null && !pDialog.isShowing()) {
                                        pDialog.show();
                                    }
                                    sDialog.dismissWithAnimation();
                                    subjectCollectionRef.document(subject.getId()).set(subject).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (pDialog != null) {
                                                pDialog.dismiss();
                                            }
                                            if (task.isSuccessful()) {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Updated")
                                                        .setContentText("Subject has been updated.")
                                                        .setConfirmText("Ok")
                                                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                sweetAlertDialog.dismissWithAnimation();
                                                                //getSubject();
                                                            }
                                                        });
                                                dialog.setCancelable(false);
                                                dialog.show();
                                            } else {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Unable to update subject")
                                                        .setContentText("Network issue, please check it.")
                                                        .setConfirmText("Ok");
                                                dialog.setCancelable(false);
                                                dialog.show();
                                            }
                                        }
                                    });

                                }
                            });
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });
            holder.ivDeleteSubject.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    deleteSubject(subject);
                }
            });

        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }
    }

    private void deleteSubject(Subject subject) {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        homeworkCollectionRef
                .whereEqualTo("subjectId", subject.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (documentSnapshots.size() == 0) {
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Delete")
                                    .setContentText("Do you want to Delete subject " + subject.getName() + "?")
                                    .setConfirmText("Confirm")
                                    .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                        }
                                    })
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            subjectCollectionRef.document(subject.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                                    .setTitleText("Deleted")
                                                                    .setContentText("Subject has been deleted.")
                                                                    .setConfirmText("Ok")
                                                                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                        @Override
                                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                            sweetAlertDialog.dismissWithAnimation();
                                                                            //getSubject();
                                                                        }
                                                                    });
                                                            dialog.setCancelable(false);
                                                            dialog.show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                                    .setTitleText("Unable to delete subject")
                                                                    .setContentText("Network issue, please check it.")
                                                                    .setConfirmText("Ok");
                                                            dialog.setCancelable(false);
                                                            dialog.show();
                                                        }
                                                    });
                                            sDialog.dismissWithAnimation();
                                        }
                                    });
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Unable to delete subject")
                                    .setContentText("In this subject some assignment are there.")
                                    .setConfirmText("Ok");
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        System.out.println("Error getting documents:" + e);
                    }
                });
    }

    private void addSubject() {
        subjectCollectionRef
                .add(subject)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Subject successfully added")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        getSubject();
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                    }
                });
        // [END add_document]
        etSubjectName.setText("");
    }


}

