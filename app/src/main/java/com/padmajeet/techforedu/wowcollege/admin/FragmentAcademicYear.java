package com.padmajeet.techforedu.wowcollege.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.padmajeet.techforedu.wowcollege.admin.model.AcademicYear;
import com.padmajeet.techforedu.wowcollege.admin.model.Institute;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.Calendar;
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
public class FragmentAcademicYear extends Fragment {
    private String loggedInUserId, instituteId;
    private View view=null;
    private LinearLayout llNoList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference academicYearCollectionRef = db.collection("AcademicYear");
    private CollectionReference instituteCollectionRef = db.collection("Institute");
    private ListenerRegistration academicYearListener;
    private AcademicYear academicYear;
    private List<AcademicYear> academicYearList = new ArrayList<>();
    private List<String> alreadyAcademicYearList = new ArrayList<>();
    private Spinner spYear;
    private String selectedYear;
    private RecyclerView rvAcademicYear;
    private RecyclerView.Adapter academicYearAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ImageButton btnSubmit;
    private int establishmentYear;
    private SweetAlertDialog pDialog;
    private List<String> year = new ArrayList<>();

    @Override
    public void onStart() {
        super.onStart();
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        academicYearListener = academicYearCollectionRef
                .whereEqualTo("instituteId", instituteId)
                .orderBy("year", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (academicYearList.size() != 0) {
                            academicYearList.clear();
                        }
                        if (alreadyAcademicYearList.size() != 0) {
                            alreadyAcademicYearList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            academicYear = document.toObject(AcademicYear.class);
                            academicYear.setId(document.getId());
                            alreadyAcademicYearList.add(academicYear.getYear());
                            academicYearList.add(academicYear);
                        }
                        if (academicYearList.size() != 0) {
                            rvAcademicYear.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            academicYearAdapter = new AcademicYearAdapter(academicYearList);
                            academicYearAdapter.notifyDataSetChanged();
                            rvAcademicYear.setAdapter(academicYearAdapter);
                        } else {
                            rvAcademicYear.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (academicYearListener != null) {
            academicYearListener.remove();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        loggedInUserId = sessionManager.getString("loggedInUserId");
        instituteId = sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_academic_year, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.academicYear));
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        spYear = view.findViewById(R.id.spYear);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        //addDialogFeedbackCategory();
        rvAcademicYear = view.findViewById(R.id.rvAcademicYear);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvAcademicYear.setLayoutManager(layoutManager);

        instituteCollectionRef.document(instituteId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Institute institute = task.getResult().toObject(Institute.class);
                            establishmentYear = institute.getYearOfEstablishment();
                            setYears();
                        }
                    }
                });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(selectedYear)) {
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Select year")
                            .setConfirmText("Ok");
                    dialog.setCancelable(false);
                    dialog.show();
                    return;
                } else {
                    String Name = selectedYear.replaceAll("\\s+", "");
                    System.out.println("Name " + Name);
                    for (int i = 0; i < alreadyAcademicYearList.size(); i++) {
                        String year = alreadyAcademicYearList.get(i).replaceAll("\\s+", "");
                        if (Name.equalsIgnoreCase(year)) {
                            System.out.println("equal");
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Already this year is saved")
                                    .setConfirmText("Ok");
                            dialog.setCancelable(false);
                            dialog.show();
                            return;
                        }
                    }
                }

                if (pDialog == null && !pDialog.isShowing()) {
                    pDialog.show();
                }
                academicYear = new AcademicYear();
                academicYear.setInstituteId(instituteId);
                academicYear.setStatus("I");
                academicYear.setYear(selectedYear);
                academicYear.setCreatorId(loggedInUserId);
                academicYear.setModifierId(loggedInUserId);
                academicYear.setCreatorType("A");
                academicYear.setModifierType("A");
                addAcademicYear();
            }
        });
        return view;
    }

    void setYears() {
        Calendar calendar = Calendar.getInstance();
        int lastYear = calendar.get(Calendar.YEAR);
        System.out.println("lastYear " + lastYear);
        lastYear++;
        System.out.println("lastYear " + lastYear);
        for (int i = establishmentYear; i <= lastYear; i++) {
            int k = i + 1;
            year.add(i + "-" + k);
        }
        ArrayAdapter<String> sectionAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, year);
        sectionAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(sectionAdaptor);
        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = spYear.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    class AcademicYearAdapter extends RecyclerView.Adapter<AcademicYearAdapter.MyViewHolder> {
        private List<AcademicYear> academicYearList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvYear;
            public Switch swStatus;

            public MyViewHolder(View view) {
                super(view);
                tvYear = view.findViewById(R.id.tvYear);
                swStatus = view.findViewById(R.id.swStatus);
            }
        }


        public AcademicYearAdapter(List<AcademicYear> academicYearList) {
            this.academicYearList = academicYearList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_academic_year, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final AcademicYear academicYear = academicYearList.get(position);
            holder.tvYear.setText("" + academicYear.getYear());
            if (academicYear.getStatus().equalsIgnoreCase("A")) {
                holder.swStatus.setTextOn(getString(R.string.active));
                holder.swStatus.setChecked(true);
            } else {
                holder.swStatus.setTextOff(getString(R.string.inactive));
                holder.swStatus.setChecked(false);
            }
            holder.swStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (academicYear.getStatus().equals("I")) {
                            for (AcademicYear academicYear1 : academicYearList) {
                                if (!academicYear1.getId().equalsIgnoreCase(academicYear.getId())) {
                                    if (academicYear1.getStatus().equalsIgnoreCase("A")) {
                                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText("Already one of academic Year is active")
                                                .setContentText("Please inactive " + academicYear1.getYear())
                                                .setConfirmText("Ok")
                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sDialog) {
                                                        holder.swStatus.setTextOff(getString(R.string.inactive));
                                                        holder.swStatus.setChecked(false);
                                                        sDialog.dismissWithAnimation();
                                                    }
                                                });
                                        dialog.setCancelable(false);
                                        dialog.show();
                                        return;
                                    }
                                }
                            }
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Activate")
                                    .setContentText("Are you sure to active this academic year?")
                                    .setConfirmText("Confirm")
                                    .setCancelText("Cancel")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            academicYear.setStatus("A");
                                            updateAcademicYear(academicYear);
                                            sweetAlertDialog.dismissWithAnimation();
                                        }
                                    })
                                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismissWithAnimation();
                                        }
                                    });
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                    } else {
                        if (academicYear.getStatus().equals("A")) {
                            academicYear.setStatus("I");
                            updateAcademicYear(academicYear);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return academicYearList.size();
        }
    }

    void updateAcademicYear(AcademicYear academicYear) {
        academicYearCollectionRef.document(academicYear.getId()).set(academicYear).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (pDialog != null) {
                    pDialog.dismiss();
                }
                if (task.isSuccessful()) {
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Updated")
                            .setContentText("Academic year has been updated.")
                            .setConfirmText("Ok");
                    dialog.setCancelable(false);
                    dialog.show();
                } else {
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Unable to update academic year")
                            .setContentText("Network issue, please check it.")
                            .setConfirmText("Ok");
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
        });
    }

    private void addAcademicYear() {
        academicYearCollectionRef
                .add(academicYear)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Added")
                                .setContentText("Academic year has been successfully added.")
                                .setConfirmText("Ok");
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
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Unable to add academic year")
                                .setContentText("Network issue, please check it.")
                                .setConfirmText("Ok");
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                });
    }

}
