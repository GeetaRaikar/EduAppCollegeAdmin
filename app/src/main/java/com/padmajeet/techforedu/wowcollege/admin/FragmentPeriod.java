package com.padmajeet.techforedu.wowcollege.admin;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Period;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentPeriod extends Fragment {

    private View view = null;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private CollectionReference periodCollectionRef=db.collection("Period");
    private CollectionReference batchCollectionRef=db.collection("Batch");
    private Period period;
    private List<Period> periodList=new ArrayList<>();
    private Batch batch;
    private List<Batch> batchList=new ArrayList<>();
    private Spinner spBatch;
    private RecyclerView rvPeriod;
    private RecyclerView.Adapter periodAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Gson gson;
    private Staff loggedInUser;
    private String schoolId,academicYearId;
    private Batch selectedBatch;
    private Bundle bundle = new Bundle();
    private Batch currentBatch;
    private LinearLayout llNoList;
    private String selectedBatchPeriod;
    private Fragment currentFragment;
    private SessionManager sessionManager;
    private SweetAlertDialog pDialog;
    private EditText etDuration,etTime,etNumber;
    private String number,time,min;
    private int duration;
    private Button btnSave;
    private List<String> alreadyPeriodList=new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        String userJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(userJson, Staff.class);
        schoolId = sessionManager.getString("schoolId");
        academicYearId=sessionManager.getString("academicYearId");
        pDialog=Utility.createSweetAlertDialog(getContext());
    }

    public FragmentPeriod() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_period, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.period));
        spBatch = view.findViewById(R.id.spBatch);
        rvPeriod = (RecyclerView) view.findViewById(R.id.rvPeriod);
        layoutManager = new LinearLayoutManager(getContext());
        rvPeriod.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        createBottomSheet();
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addPeriod);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.show();
            }});

        getBatches();
        return view;
    }
    BottomSheetDialog bottomSheetDialog;
    private void createBottomSheet() {
        if (bottomSheetDialog == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_period, null);
            bottomSheetDialog = new BottomSheetDialog(getContext());//new BottomSheetDialog(this,R.style.BottomSheetDialog)
            bottomSheetDialog.setContentView(view);
            etNumber = view.findViewById(R.id.etNumber);
            etTime = view.findViewById(R.id.etTime);
            etDuration = view.findViewById(R.id.etDuration);
            btnSave = view.findViewById(R.id.btnSave);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    number = etNumber.getText().toString().trim();
                    if (TextUtils.isEmpty(number)) {
                        etNumber.setError("Enter period name");
                        etNumber.requestFocus();
                        return;
                    } else {
                        String Name = number.replaceAll("\\s+", "");
                        System.out.println("Name " + Name);
                        for (int i = 0; i < alreadyPeriodList.size(); i++) {
                            String periodName = alreadyPeriodList.get(i).replaceAll("\\s+", "");
                            if (Name.toUpperCase() == periodName.toUpperCase()) {
                                System.out.println("equal");
                                etNumber.setError("Already this period name is saved ");
                                etNumber.requestFocus();
                                return;
                            }
                        }
                    }
                    time = etTime.getText().toString().trim();
                    if (TextUtils.isEmpty(time)) {
                        etTime.setError("Enter time");
                        etTime.requestFocus();
                        return;
                    }
                    min = etDuration.getText().toString().trim();
                    if (TextUtils.isEmpty(min)) {
                        etDuration.setError("Enter duration");
                        etDuration.requestFocus();
                        return;
                    }else{
                        duration=Integer.parseInt(min);
                    }
                    if (pDialog == null && !pDialog.isShowing()) {
                        pDialog.show();
                    }
                    period = new Period();
                    period.setBatchId(selectedBatch.getId());
                    period.setNumber(number);
                    period.setFromTime(time);
                    period.setDurationInMin(duration);
                    period.setCreatorId(loggedInUser.getId());
                    period.setModifierId(loggedInUser.getId());
                    addPeriod();
                }
            });
        }
    }
    private void addPeriod(){
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Period successfully added")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        periodCollectionRef
                                .add(period)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        FragmentPeriod fragmentPeriod = new FragmentPeriod();
                                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                        fragmentManager.beginTransaction().replace(R.id.contentLayout, fragmentPeriod).addToBackStack(null).commit();

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
                    }
                });
        dialog.setCancelable(false);
        dialog.show();

    }
    private void getBatches() {
        if(pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo("schoolId",schoolId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null && pDialog.isShowing()){
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
                                    if (periodList != null) {
                                        periodList.clear();
                                    }
                                    selectedBatch = batchList.get(position);
                                    selectedBatchPeriod = gson.toJson(selectedBatch);
                                    getPeriodsOfBatch();
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
    private void getPeriodsOfBatch() {
        if(pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        periodCollectionRef
                .whereEqualTo("batchId",selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            period = document.toObject(Period.class);
                            period.setId(document.getId());
                            alreadyPeriodList.add(period.getNumber());
                            periodList.add(period);
                        }
                        if (periodList.size()!=0) {
                            rvPeriod.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            periodAdapter = new PeriodAdapter(periodList);
                            rvPeriod.setAdapter(periodAdapter);
                        } else {
                            rvPeriod.setVisibility(View.GONE);
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
    class PeriodAdapter extends RecyclerView.Adapter<PeriodAdapter.MyViewHolder> {
        private List<Period> periodList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName, tvTime;
            public ImageView ivEditPeriodName;

            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
                tvTime = view.findViewById(R.id.tvTime);
                ivEditPeriodName = view.findViewById(R.id.ivEditPeriodName);
            }
        }


        public PeriodAdapter(List<Period> periodList) {
            this.periodList = periodList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_period, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final Period period = periodList.get(position);

            holder.tvName.setText("" + period.getNumber());
            holder.tvTime.setText("" + period.getFromTime());
            holder.ivEditPeriodName.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    final EditText editText = new EditText(getContext());
                    editText.setText(period.getFromTime());
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit Period Time")
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
                                    sDialog.dismissWithAnimation();
                                    time = editText.getText().toString().trim();
                                    // System.out.println("String batchName" + batchName);
                                    if (TextUtils.isEmpty(time)) {
                                        editText.setError("Enter time");
                                        editText.requestFocus();
                                        return;
                                    }
                                    period.setFromTime(time);
                                    period.setModifiedDate(new Date());
                                    period.setModifierId(loggedInUser.getId());

                                    updatePeriod(period);

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
    private void updatePeriod(final Period currentPeriod) {
        periodCollectionRef
                .document(currentPeriod.getId())
                .set(currentPeriod)
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
}
