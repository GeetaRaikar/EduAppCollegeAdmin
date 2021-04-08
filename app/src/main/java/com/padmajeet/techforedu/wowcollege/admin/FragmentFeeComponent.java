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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.padmajeet.techforedu.wowcollege.admin.model.FeeComponent;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentFeeComponent extends Fragment {
    private View view;
    private LinearLayout llNoList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference feeComponentCollectionRef = db.collection("FeeComponent");
    private CollectionReference feeStructureCollectionRef = db.collection("FeeStructure");
    private EditText etFeeComponentName;
    private ListenerRegistration feeComponentListener;
    private FeeComponent feeComponent;
    private List<FeeComponent> feeComponentList = new ArrayList<>();
    private List<String> alreadyFeeComponentList = new ArrayList<>();
    private RecyclerView rvFeeComponent;
    private RecyclerView.Adapter feeComponentAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String name;
    private ImageButton btnSubmit;
    private String loggedInUserId, academicYearId, instituteId;
    private Staff loggedInUser;
    private Gson gson;
    private SweetAlertDialog pDialog;

    @Override
    public void onStart() {
        super.onStart();
        if (!pDialog.isShowing() && pDialog == null) {
            pDialog.show();
        }
        feeComponentListener = feeComponentCollectionRef
                .whereEqualTo("instituteId", instituteId)
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
                        if (feeComponentList.size() != 0) {
                            feeComponentList.clear();
                        }
                        if (alreadyFeeComponentList.size() != 0) {
                            alreadyFeeComponentList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            feeComponent = document.toObject(FeeComponent.class);
                            feeComponent.setId(document.getId());
                            alreadyFeeComponentList.add(feeComponent.getName());
                            feeComponentList.add(feeComponent);
                        }
                        if (feeComponentList.size() != 0) {
                            rvFeeComponent.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            feeComponentAdapter = new FeeComponentAdapter(feeComponentList);
                            feeComponentAdapter.notifyDataSetChanged();
                            rvFeeComponent.setAdapter(feeComponentAdapter);
                        } else {
                            rvFeeComponent.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (feeComponentListener != null) {
            feeComponentListener.remove();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = Utility.getGson();
        SessionManager sessionManager = new SessionManager(getContext());
        String userJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(userJson, Staff.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
        academicYearId = sessionManager.getString("academicYearId");
        instituteId = sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }


    public FragmentFeeComponent() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fee_component, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.feeComponent));
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        etFeeComponentName = view.findViewById(R.id.etFeeComponentName);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        //addDialogFeeComponent();
        rvFeeComponent = view.findViewById(R.id.rvFeeComponent);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvFeeComponent.setLayoutManager(layoutManager);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etFeeComponentName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etFeeComponentName.setError("Enter Fee Component Name");
                    etFeeComponentName.requestFocus();
                    return;
                } else {
                    if(Utility.isNumericWithSpace(name)){
                        etFeeComponentName.setError("Invalid Fee Component Name");
                        etFeeComponentName.requestFocus();
                        return;
                    }else {
                        //TODO
                        String Name = name.replaceAll("\\s+", "");
                        System.out.println("Name " + Name);
                        for (int i = 0; i < alreadyFeeComponentList.size(); i++) {
                            String feeComponentName = alreadyFeeComponentList.get(i).replaceAll("\\s+", "");
                            if (Name.equalsIgnoreCase(feeComponentName)) {
                                etFeeComponentName.setError("Already this fee component is saved ");
                                etFeeComponentName.requestFocus();
                                return;
                            }
                        }
                    }
                }
                if (!pDialog.isShowing() && pDialog == null) {
                    pDialog.show();
                }
                feeComponent = new FeeComponent();
                feeComponent.setInstituteId(instituteId);
                feeComponent.setName(name);
                feeComponent.setCreatorId(loggedInUserId);
                feeComponent.setModifierId(loggedInUserId);
                feeComponent.setCreatorType("A");
                feeComponent.setModifierType("A");
                addFeeComponent();
            }
        });
        return view;
    }

    class FeeComponentAdapter extends RecyclerView.Adapter<FeeComponentAdapter.MyViewHolder> {
        private List<FeeComponent> feeComponentList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView FeeComponentName;
            public ImageView ivEditFeeComponent, ivDeleteFeeComponent;

            public MyViewHolder(View view) {
                super(view);
                FeeComponentName = view.findViewById(R.id.tvFeeComponentName);
                ivEditFeeComponent = view.findViewById(R.id.ivEditFeeComponent);
                ivDeleteFeeComponent = view.findViewById(R.id.ivDeleteFeeComponent);
            }
        }


        public FeeComponentAdapter(List<FeeComponent> feeComponentList) {
            this.feeComponentList = feeComponentList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_fee_component, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final FeeComponent feeComponent = feeComponentList.get(position);
            holder.FeeComponentName.setText("" + feeComponent.getName());
            holder.ivEditFeeComponent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText editText = new EditText(getContext());
                    editText.setText(feeComponent.getName());
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit Fee Component")
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
                                        editText.setError("Enter fee component");
                                        editText.requestFocus();
                                        return;
                                    }else {
                                        if(Utility.isNumericWithSpace(name)){
                                            editText.setError("Invalid fee component");
                                            editText.requestFocus();
                                            return;
                                        }else {
                                            String Name = feeComponent.getName();
                                            Name = Name.replaceAll("\\s+", "");
                                            System.out.println("Name " + Name);
                                            String EditName = name.replaceAll("\\s+", "");
                                            System.out.println("EditName " + EditName);
                                            if (!Name.equalsIgnoreCase(EditName)) {
                                                for (int i = 0; i < alreadyFeeComponentList.size(); i++) {
                                                    String fee_component_name = alreadyFeeComponentList.get(i).replaceAll("\\s+", "");
                                                    System.out.println("expense_category_name " + fee_component_name);
                                                    if (EditName.equalsIgnoreCase(fee_component_name)) {
                                                        editText.setError("Already this fee component is saved");
                                                        editText.requestFocus();
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    feeComponent.setName(name);
                                    feeComponent.setModifiedDate(new Date());
                                    feeComponent.setModifierId(loggedInUserId);

                                    if (pDialog != null && !pDialog.isShowing()) {
                                        pDialog.show();
                                    }
                                    feeComponentCollectionRef.document(feeComponent.getId()).set(feeComponent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (pDialog != null) {
                                                pDialog.dismiss();
                                            }
                                            if (task.isSuccessful()) {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Updated")
                                                        .setContentText("Fee component has been updated.")
                                                        .setConfirmText("Ok");
                                                dialog.setCancelable(false);
                                                dialog.show();
                                            } else {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Unable to update fee component")
                                                        .setContentText("Network issue, please check it.")
                                                        .setConfirmText("Ok");
                                                dialog.setCancelable(false);
                                                dialog.show();
                                            }
                                            sDialog.dismissWithAnimation();
                                        }
                                    });

                                }
                            });
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.setCancelable(false);
                    dialog.show();

                }
            });
            holder.ivDeleteFeeComponent.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    deleteFeeComponent(feeComponent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return feeComponentList.size();
        }
    }

    private void deleteFeeComponent(FeeComponent feeComponent) {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        feeStructureCollectionRef
                .whereEqualTo("feeComponentId", feeComponent.getId())
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
                                    .setContentText("Do you want to delete " + feeComponent.getName() + "?")
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
                                            if (pDialog == null && !pDialog.isShowing()) {
                                                pDialog.show();
                                            }
                                            feeComponentCollectionRef.document(feeComponent.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            if (pDialog != null) {
                                                                pDialog.dismiss();
                                                            }
                                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                                    .setTitleText("Deleted")
                                                                    .setContentText("Fee component has been deleted.")
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
                                                                    .setTitleText("Unable to delete fee component")
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
                                    .setTitleText("Unable to delete fee component")
                                    .setContentText("In this fee component some fee structures are there.")
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
    }

    private void addFeeComponent() {
        feeComponentCollectionRef
                .add(feeComponent)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Successfully")
                                .setContentText("Fee component has been successfully added.")
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
                                .setTitleText("Unable to add fee component")
                                .setContentText("Network issue, please check it.")
                                .setConfirmText("Ok");
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                });
        // [END add_document]
        etFeeComponentName.setText("");

    }
}