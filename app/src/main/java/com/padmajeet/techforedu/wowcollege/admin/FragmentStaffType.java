package com.padmajeet.techforedu.wowcollege.admin;


import android.os.Bundle;
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
import com.padmajeet.eduapp.wowcollege.admin.model.Staff;
import com.padmajeet.eduapp.wowcollege.admin.model.StaffType;
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
public class FragmentStaffType extends Fragment {
    private View view;
    private LinearLayout llNoList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference staffTypeCollectionRef = db.collection("StaffType");
    private CollectionReference staffCollectionRef = db.collection("Staff");
    private String name;
    private EditText etStaffType;
    private ImageButton btnSubmit;
    private ListenerRegistration staffTypeListener;
    private StaffType staffType;
    private List<StaffType> staffTypeList = new ArrayList<>();
    private List<String> alreadyStaffTypeList = new ArrayList<>();
    private RecyclerView rvStaffType;
    private RecyclerView.Adapter staffTypeAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Staff loggedInUser;
    private String instituteId, loggedInUserId, imageUrl = "", academicYearId;
    private Gson gson;
    private SweetAlertDialog pDialog;

    @Override
    public void onStart() {
        super.onStart();
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        staffTypeListener = staffTypeCollectionRef
                .whereEqualTo("instituteId", instituteId)
                .orderBy("type", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (staffTypeList.size() != 0) {
                            staffTypeList.clear();
                        }
                        if (alreadyStaffTypeList.size() != 0) {
                            alreadyStaffTypeList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            staffType = document.toObject(StaffType.class);
                            staffType.setId(document.getId());
                            alreadyStaffTypeList.add(staffType.getType());
                            staffTypeList.add(staffType);
                        }
                        if (staffTypeList.size() != 0) {
                            rvStaffType.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            staffTypeAdapter = new StaffTypeAdapter(staffTypeList);
                            staffTypeAdapter.notifyDataSetChanged();
                            rvStaffType.setAdapter(staffTypeAdapter);
                        } else {
                            rvStaffType.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (staffTypeListener != null) {
            staffTypeListener.remove();
        }
    }

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

    public FragmentStaffType() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_staff_type, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.staffType));
        etStaffType = (EditText) view.findViewById(R.id.etStaffType);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        // addDialogStaffType();
        rvStaffType = view.findViewById(R.id.rvStaffType);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvStaffType.setLayoutManager(layoutManager);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etStaffType.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etStaffType.setError("Enter staff type name");
                    etStaffType.requestFocus();
                    return;
                } else {
                    if(Utility.isNumericWithSpace(name)){
                        etStaffType.setError("Invalid staff type name");
                        etStaffType.requestFocus();
                        return;
                    }else {
                        //TODO
                        String Name = name.replaceAll("\\s+", "");
                        System.out.println("Name " + Name);
                        for (int i = 0; i < alreadyStaffTypeList.size(); i++) {
                            String staffTypeName = alreadyStaffTypeList.get(i).replaceAll("\\s+", "");
                            if (Name.equalsIgnoreCase(staffTypeName)) {
                                etStaffType.setError("Already this staff type is saved ");
                                etStaffType.requestFocus();
                                return;
                            }
                        }
                    }
                }
                if (pDialog == null && !pDialog.isShowing()) {
                    pDialog.show();
                }
                staffType = new StaffType();
                staffType.setImageUrl(imageUrl);
                staffType.setInstituteId(instituteId);
                staffType.setType(name);
                staffType.setStatus("A");
                staffType.setCreatorId(loggedInUserId);
                staffType.setModifierId(loggedInUserId);
                staffType.setCreatorType("A");
                staffType.setModifierType("A");
                addStaffType();
            }
        });
        return view;
    }

    class StaffTypeAdapter extends RecyclerView.Adapter<StaffTypeAdapter.MyViewHolder> {
        private List<StaffType> staffTypeList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView StaffTypeName;
            public ImageView ivEditStaffType;
            public ImageView ivDeleteStaffType;

            public MyViewHolder(View view) {
                super(view);
                StaffTypeName = view.findViewById(R.id.tvStaffTypeName);
                ivEditStaffType = view.findViewById(R.id.ivEditStaffType);
                ivDeleteStaffType = view.findViewById(R.id.ivDeleteStaffType);
            }
        }


        public StaffTypeAdapter(List<StaffType> staffTypeList) {
            this.staffTypeList = staffTypeList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_staff_type, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final StaffType staffType = staffTypeList.get(position);
            holder.StaffTypeName.setText("" + staffType.getType());
            if(staffType.getIsMandatory()){
                holder.ivEditStaffType.setVisibility(View.GONE);
                holder.ivDeleteStaffType.setVisibility(View.GONE);
            }else{
                holder.ivEditStaffType.setVisibility(View.VISIBLE);
                holder.ivDeleteStaffType.setVisibility(View.VISIBLE);
            }
            holder.ivEditStaffType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText editText = new EditText(getContext());
                    editText.setText(staffType.getType());
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText(getResources().getString(R.string.editStaffType))
                            .setConfirmText(getResources().getString(R.string.update))
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
                                    String staffTypeName = editText.getText().toString().trim();
                                    if (TextUtils.isEmpty(staffTypeName)) {
                                        editText.setError("Enter staff type name");
                                        editText.requestFocus();
                                        return;
                                    }else {
                                        if(Utility.isNumericWithSpace(staffTypeName)){
                                            editText.setError("Invalid staff type name");
                                            editText.requestFocus();
                                            return;
                                        }else {
                                            String Name = staffType.getType();
                                            Name = Name.replaceAll("\\s+", "");
                                            System.out.println("Name " + Name);
                                            String EditName = staffTypeName.replaceAll("\\s+", "");
                                            System.out.println("EditName " + EditName);
                                            if (!Name.equalsIgnoreCase(EditName)) {
                                                for (int i = 0; i < alreadyStaffTypeList.size(); i++) {
                                                    String staff_type_name = alreadyStaffTypeList.get(i).replaceAll("\\s+", "");
                                                    System.out.println("staff_type_name " + staff_type_name);
                                                    if (EditName.equalsIgnoreCase(staff_type_name)) {
                                                        System.out.print("flag ");
                                                        editText.setError("Already this staff type is saved");
                                                        editText.requestFocus();
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    staffType.setType(staffTypeName);
                                    staffType.setModifiedDate(new Date());
                                    staffType.setModifierId(loggedInUserId);

                                    if (pDialog == null && !pDialog.isShowing()) {
                                        pDialog.show();
                                    }
                                    staffTypeCollectionRef.document(staffType.getId()).set(staffType).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (pDialog != null) {
                                                pDialog.dismiss();
                                            }
                                            if (task.isSuccessful()) {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Updated")
                                                        .setContentText("Staff type has been updated.")
                                                        .setConfirmText("Ok");
                                                dialog.setCancelable(false);
                                                dialog.show();
                                            } else {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Unable to update staff type")
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
            holder.ivDeleteStaffType.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    deleteStaffType(staffType);
                }
            });
        }

        @Override
        public int getItemCount() {
            return staffTypeList.size();
        }
    }

    private void deleteStaffType(StaffType staffType) {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        staffCollectionRef
                .whereEqualTo("staffTypeId", staffType.getId())
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
                                    .setContentText("Do you want to delete " + staffType.getType() + "?")
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
                                            staffTypeCollectionRef.document(staffType.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            if (pDialog != null) {
                                                                pDialog.dismiss();
                                                            }
                                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                                    .setTitleText("Deleted")
                                                                    .setContentText("Staff type has been deleted.")
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
                                                                    .setTitleText("Unable to delete staff type")
                                                                    .setContentText("For some network issue please check it.")
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
                                    .setTitleText("Unable to delete staff type")
                                    .setContentText("In this staff type some staffs are there.")
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

    private void addStaffType() {

        staffTypeCollectionRef
                .add(staffType)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Successfully")
                                .setContentText("Staff type has been successfully added.")
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
                                .setTitleText("Unable to add staff type")
                                .setContentText("Network issue, please check it.")
                                .setConfirmText("Ok");
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                });
        etStaffType.setText("");
    }
}
