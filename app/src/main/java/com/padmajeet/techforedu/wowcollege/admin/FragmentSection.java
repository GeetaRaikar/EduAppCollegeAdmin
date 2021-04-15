package com.padmajeet.techforedu.wowcollege.admin;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.pedant.SweetAlert.SweetAlertDialog;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Section;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSection extends Fragment {
    private View view;
    private LinearLayout llNoList;
    private ArrayList<Batch> batchList = new ArrayList<>();
    private ArrayList<Section> sectionList = new ArrayList<>();
    private String academicYearId,loggedInUserId,instituteId;
    private Batch batch,selectedBatch;
    private Section section;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference sectionCollectionRef = db.collection("Section");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference homeworkCollectionRef = db.collection("HomeWork");
    private ListenerRegistration sectionListener;
    private List<String> alreadySectionList = new ArrayList<>();
    private EditText etSectionName;
    private RecyclerView rvSection;
    private RecyclerView.Adapter sectionAdapter;
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
        instituteId = sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (sectionListener != null) {
            sectionListener.remove();
        }
    }
    public void getBatches() {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo("instituteId", instituteId)
                .orderBy("name", Query.Direction.ASCENDING)
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
                            rvSection.setVisibility(View.VISIBLE);
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
                                    getSection();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        } else {
                            llNoList.setVisibility(View.VISIBLE);
                            rvSection.setVisibility(View.GONE);
                            spBatch.setEnabled(false);
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        }
                    }
                });

    }


    public FragmentSection() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_section, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.section));
        spBatch = view.findViewById(R.id.spBatch);
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        etSectionName = (EditText) view.findViewById(R.id.etSectionName);
        btnSubmit = (ImageButton) view.findViewById(R.id.btnSubmit);
        rvSection = (RecyclerView) view.findViewById(R.id.rvSection);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvSection.setLayoutManager(layoutManager);
        getBatches();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etSectionName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etSectionName.setError("Enter section name");
                    etSectionName.requestFocus();
                    return;
                } else {
                    if(Utility.isNumericWithSpace(name)){
                        etSectionName.setError("Invalid section name");
                        etSectionName.requestFocus();
                        return;
                    }else {
                        //TODO
                        String Name = name.replaceAll("\\s+", "");
                        System.out.println("Name " + Name);
                        for (int i = 0; i < alreadySectionList.size(); i++) {
                            String sectionName = alreadySectionList.get(i).replaceAll("\\s+", "");
                            if (Name.equalsIgnoreCase(sectionName)) {
                                System.out.println("equal");
                                etSectionName.setError("Already this section is saved ");
                                etSectionName.requestFocus();
                                return;
                            }
                        }
                    }
                }

                if (pDialog == null && !pDialog.isShowing()) {
                    pDialog.show();
                }
                section = new Section();
                section.setBatchId(selectedBatch.getId());
                section.setStatus("A");
                section.setName(name);
                section.setCreatorId(loggedInUserId);
                section.setModifierId(loggedInUserId);
                section.setCreatorType("A");
                section.setModifierType("A");
                addSection();
            }
        });
        return view;
    }

    private void getSection() {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        sectionListener = sectionCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (sectionList.size() != 0) {
                            sectionList.clear();
                        }
                        if (alreadySectionList.size() != 0) {
                            alreadySectionList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            section = document.toObject(Section.class);
                            section.setId(document.getId());
                            alreadySectionList.add(section.getName());
                            sectionList.add(section);
                        }
                        //System.out.println("Section  -" + sectionList.size());
                        if (sectionList.size() != 0) {
                            sectionAdapter = new SectionAdapter(sectionList);
                            rvSection.setAdapter(sectionAdapter);
                            rvSection.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        } else {
                            rvSection.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.MyViewHolder> {
        private List<Section> sectionList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSectionName;
            public ImageView ivDeleteSection, ivEditSection;

            public MyViewHolder(View view) {
                super(view);
                tvSectionName = view.findViewById(R.id.tvSectionName);
                ivDeleteSection = view.findViewById(R.id.ivDeleteSection);
                ivEditSection = view.findViewById(R.id.ivEditSection);
            }
        }


        public SectionAdapter(List<Section> sectionList) {
            this.sectionList = sectionList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_section, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Section section = sectionList.get(position);
            holder.tvSectionName.setText("" + section.getName());

            holder.ivEditSection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText editText = new EditText(getContext());
                    editText.setText(section.getName());
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit section")
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
                                        editText.setError("Enter section name");
                                        editText.requestFocus();
                                        return;
                                    }else {
                                        if(Utility.isNumericWithSpace(name)){
                                            editText.setError("Invalid section name");
                                            editText.requestFocus();
                                            return;
                                        }else {
                                            String Name = section.getName();
                                            Name = Name.replaceAll("\\s+", "");
                                            System.out.println("Name " + Name);
                                            String EditName = name.replaceAll("\\s+", "");
                                            System.out.println("EditName " + EditName);
                                            if (!Name.equalsIgnoreCase(EditName)) {
                                                for (int i = 0; i < alreadySectionList.size(); i++) {
                                                    String section_name = alreadySectionList.get(i).replaceAll("\\s+", "");
                                                    if (EditName.equalsIgnoreCase(section_name)) {
                                                        editText.setError("Already this section is saved");
                                                        editText.requestFocus();
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    section.setName(name);
                                    section.setModifiedDate(new Date());
                                    section.setModifierId(loggedInUserId);
                                    if (pDialog == null && !pDialog.isShowing()) {
                                        pDialog.show();
                                    }
                                    sDialog.dismissWithAnimation();
                                    sectionCollectionRef.document(section.getId()).set(section).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (pDialog != null) {
                                                pDialog.dismiss();
                                            }
                                            if (task.isSuccessful()) {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Updated")
                                                        .setContentText("Section has been updated.")
                                                        .setConfirmText("Ok")
                                                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                sweetAlertDialog.dismissWithAnimation();
                                                                //getSection();
                                                            }
                                                        });
                                                dialog.setCancelable(false);
                                                dialog.show();
                                            } else {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Unable to update section")
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
            holder.ivDeleteSection.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    deleteSection(section);
                }
            });

        }

        @Override
        public int getItemCount() {
            return sectionList.size();
        }
    }

    private void deleteSection(Section section) {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        if(sectionList.size()>1) {
            studentCollectionRef
                    .whereEqualTo("sectionId", section.getId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            if (documentSnapshots.size() == 0) {
                                homeworkCollectionRef
                                        .whereEqualTo("sectionId", section.getId())
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
                                                            .setContentText("Do you want to Delete section " + section.getName() + "?")
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
                                                                    sectionCollectionRef.document(section.getId())
                                                                            .delete()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                                                            .setTitleText("Deleted")
                                                                                            .setContentText("Section has been deleted.")
                                                                                            .setConfirmText("Ok")
                                                                                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                                                @Override
                                                                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                                                    sweetAlertDialog.dismissWithAnimation();
                                                                                                    getSection();
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
                                                                                            .setTitleText("Unable to delete section")
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
                                                            .setTitleText("Unable to delete section")
                                                            .setContentText("In this section some assignment are there.")
                                                            .setConfirmText("Ok");
                                                    dialog.setCancelable(false);
                                                    dialog.show();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                System.out.println("Error getting documents:" + e);
                                            }
                                        });
                            } else {
                                if (pDialog != null) {
                                    pDialog.dismiss();
                                }
                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Unable to delete section")
                                        .setContentText("In this section some students are there.")
                                        .setConfirmText("Ok");
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("Error getting documents:" + e);
                        }
                    });
        }else{
            if(sectionList.size()==1){
                if (pDialog != null) {
                    pDialog.dismiss();
                }
                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable to delete section")
                        .setContentText("In this class, at least one section should be there.")
                        .setConfirmText("Ok");
                dialog.setCancelable(false);
                dialog.show();
            }
        }
    }

    private void addSection() {
        sectionCollectionRef
                .add(section)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Section successfully added")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        getSection();
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
        etSectionName.setText("");
    }


}

