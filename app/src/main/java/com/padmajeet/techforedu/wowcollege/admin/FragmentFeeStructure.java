package com.padmajeet.techforedu.wowcollege.admin;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
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
import com.padmajeet.techforedu.wowcollege.admin.model.FeeComponent;
import com.padmajeet.techforedu.wowcollege.admin.model.FeeStructure;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentFeeStructure extends Fragment {

    private String loggedInUserId,academicYearId,instituteId;
    private View view = null;
    private Spinner spBatch;
    private LinearLayout llNoList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference feeStructureCollectionRef = db.collection("FeeStructure");
    private CollectionReference feeComponentCollectionRef = db.collection("FeeComponent");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private  DocumentReference feeComponentDocRef;
    private FeeComponent feeComponent;
    private FeeStructure feeStructure;
    private List<FeeStructure> feeStructureList = new ArrayList<>();
    private List<FeeComponent> feeComponentList = new ArrayList<>();
    private Spinner spFeeComponent,spClass;
    private EditText etAmount;
    private FeeComponent selectedFeeComponent;
    private Batch selectBatch;
    private List<Batch> batchList = new ArrayList<>();
    private Batch batch;
    private float totalFees;
    private TextView tvTotalFees;
    private RecyclerView rvFeeStructure;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private LinearLayout llTotalFees;
    private Button btnSave;
    private FloatingActionButton fab;
    private Staff loggedInUser;
    private Gson gson;
    private int batchSpinnerPosition;
    private int subSpinnerPos;
    private int mainSpinnerPos;
    private SweetAlertDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson=sessionManager.getString("loggedInUser");
        loggedInUser=gson.fromJson(userJson,Staff.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
        academicYearId= sessionManager.getString("academicYearId");
        instituteId=sessionManager.getString("instituteId");
        pDialog= Utility.createSweetAlertDialog(getContext());
    }


    public FragmentFeeStructure() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fee_structure, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.feeStructure));
        rvFeeStructure = (RecyclerView) view.findViewById(R.id.lvFeeStructure);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvFeeStructure.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        spBatch = view.findViewById(R.id.spBatch);
        tvTotalFees = view.findViewById(R.id.tvTotalFee);
        llTotalFees = view.findViewById(R.id.llTotalFee);
        //lvFeeStructure = view.findViewById(R.id.lvFeeStructure);
        llNoList = view.findViewById(R.id.llNoList);
        getBatches();

        fab = (FloatingActionButton) view.findViewById(R.id.addFeeStructure);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBottomSheet();
                bottomSheetDialog.show();
            }
        });
        return view;
    }

    BottomSheetDialog bottomSheetDialog;

    private void createBottomSheet() {
        if (bottomSheetDialog == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_fee_structure, null);
            bottomSheetDialog = new BottomSheetDialog(getContext());//new BottomSheetDialog(this,R.style.BottomSheetDialog)
            bottomSheetDialog.setContentView(view);
            etAmount = view.findViewById(R.id.etAmount);
            spFeeComponent = view.findViewById(R.id.spFeeComponent);
            btnSave = view.findViewById(R.id.btnSave);
            spClass =view.findViewById(R.id.spClass);

            selectBatch=new Batch();

            if(batchList.size()!=0) {
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
                        selectBatch = batchList.get(position);
                        subSpinnerPos = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
            getFeeComponentList();
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String amount = etAmount.getText().toString().trim();
                    if (TextUtils.isEmpty(amount)) {
                        etAmount.setError("Enter amount");
                        etAmount.requestFocus();
                        return;
                    }
                    float amountInFloat=0;
                    try {
                        amountInFloat = Float.parseFloat(amount);
                    }catch (Exception e){
                        etAmount.setError("Enter valid amount");
                        etAmount.requestFocus();
                        return;
                    }
                    if(selectBatch!=null && selectedFeeComponent!=null) {

                        if(pDialog==null && !pDialog.isShowing()){
                            pDialog.show();
                        }
                        feeStructure = new FeeStructure();
                        feeStructure.setAmount(amountInFloat);
                        feeStructure.setFeeComponentId(selectedFeeComponent.getId());
                        feeStructure.setBatchId(selectBatch.getId());
                        feeStructure.setCreatorId(loggedInUserId);
                        feeStructure.setModifierId(loggedInUserId);
                        feeStructure.setCreatorType("A");
                        feeStructure.setModifierType("A");
                        addFeeStructure();
                        etAmount.setText("");
                    }
                }
            });
        }
    }

    private  void getFeeComponentList(){
        if(feeComponentList.size()!=0){
            feeComponentList.clear();
        }
        if(pDialog==null && !pDialog.isShowing()){
            pDialog.show();
        }
        feeComponentCollectionRef
                .whereEqualTo("instituteId",instituteId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        for (QueryDocumentSnapshot document :queryDocumentSnapshots ) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            feeComponent=document.toObject(FeeComponent.class);
                            feeComponent.setId(document.getId());
                            feeComponentList.add(feeComponent);
                        }
                        //System.out.println("feeComponentList size"+feeComponentList.size());
                        if (feeComponentList.size() != 0) {
                            List<String> nameList = new ArrayList<>();
                            for (FeeComponent feeComponent : feeComponentList) {
                                nameList.add(feeComponent.getName());
                            }
                            ArrayAdapter<String> adaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nameList);
                            adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spFeeComponent.setAdapter(adaptor);

                            spFeeComponent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedFeeComponent = feeComponentList.get(position);

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
                        if(pDialog!=null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                    }
                });
    }
    private void addFeeStructure() {
        if(pDialog==null && !pDialog.isShowing()){
            pDialog.show();
        }
        bottomSheetDialog.dismiss();
        feeStructureCollectionRef
                .whereEqualTo("batchId",selectBatch.getId())
                .whereEqualTo("feeComponentId",selectedFeeComponent.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().size() > 0){
                                if (pDialog != null) {
                                    pDialog.dismiss();
                                }
                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("Already fee structure added.")
                                        .setContentText("Please edit it")
                                        .setConfirmText("Ok")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                sDialog.dismissWithAnimation();
                                                if(mainSpinnerPos==subSpinnerPos){
                                                    getFeeStructureList();
                                                }
                                                else {
                                                    spBatch.setSelection(subSpinnerPos, true);
                                                }
                                            }
                                        });
                                dialog.setCancelable(false);
                                dialog.show();
                            }else{
                                feeStructureCollectionRef
                                        .add(feeStructure)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Success")
                                                        .setContentText("Successfully added")
                                                        .setConfirmText("Ok")
                                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sDialog) {
                                                                sDialog.dismissWithAnimation();
                                                                if (pDialog != null) {
                                                                    pDialog.dismiss();
                                                                }
                                                                if(mainSpinnerPos==subSpinnerPos){
                                                                    getFeeStructureList();
                                                                }
                                                                else {
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
        if(batchList.size()!=0){
            batchList.clear();
        }
        if(pDialog==null && !pDialog.isShowing()){
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
                                batchList.add(batch);
                            }
                            if(batchList.size()!=0) {
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
                                        selectBatch = batchList.get(position);
                                        mainSpinnerPos = position;
                                        getFeeStructureList();

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
                        }
                    }
                });
        // [END get_all_users]
    }

    private void getFeeStructureList() {
        totalFees = 0;
        if (feeStructureList.size() != 0) {
            feeStructureList.clear();
        }
        if(pDialog==null && !pDialog.isShowing()){
            pDialog.show();
        }
        feeStructureCollectionRef
                .whereEqualTo("batchId", selectBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            feeStructure = documentSnapshot.toObject(FeeStructure.class);
                            feeStructure.setId(documentSnapshot.getId());
                            totalFees += feeStructure.getAmount();
                            feeStructureList.add(feeStructure);
                        }
                        if (feeStructureList.size() != 0) {
                            llTotalFees.setVisibility(View.VISIBLE);
                            rvFeeStructure.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            tvTotalFees.setText("" + totalFees);
                            mAdapter = new MyAdapter(feeStructureList);
                            rvFeeStructure.setAdapter(mAdapter);
                        } else {
                            llTotalFees.setVisibility(View.GONE);
                            rvFeeStructure.setVisibility(View.GONE);
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
                    }
                });
        // [END get_all_users]
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private List<FeeStructure> feeStructureList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView feeComponentName, amount;
            public ImageView ivEditFeeStructure,ivDeleteFeeStructure;

            public MyViewHolder(View contentView) {
                super(contentView);
                feeComponentName = (TextView) contentView.findViewById(R.id.tvFeeComponentName);
                amount = (TextView) contentView.findViewById(R.id.tvAmount);
                ivEditFeeStructure=contentView.findViewById(R.id.ivEditFeeStructure);
                ivDeleteFeeStructure=contentView.findViewById(R.id.ivDeleteFeeStructure);
            }
        }


        public MyAdapter(List<FeeStructure> feeStructureList) {
            this.feeStructureList = feeStructureList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_fee_stucture, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final FeeStructure feeStructure = feeStructureList.get(position);
            feeComponentDocRef = db.document("FeeComponent/" + feeStructure.getFeeComponentId());
            feeComponentDocRef
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            feeComponent=documentSnapshot.toObject(FeeComponent.class);
                            feeComponent.setId(documentSnapshot.getId());
                            System.out.println("ExpenseCategoryId - "+feeComponent.getName());
                            holder.feeComponentName.setText("" + feeComponent.getName());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

            holder.amount.setText("" + feeStructure.getAmount());
            holder.ivDeleteFeeStructure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Delete")
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
                                    sDialog.dismissWithAnimation();
                                    final SweetAlertDialog pDialog;
                                    pDialog = Utility.createSweetAlertDialog(getContext());
                                    pDialog.show();
                                    feeStructureCollectionRef.document(feeStructure.getId())
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    pDialog.dismiss();
                                                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                            .setTitleText("Deleted")
                                                            .setContentText("Fee structure has been deleted.")
                                                            .setConfirmText("Ok")
                                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                @Override
                                                                public void onClick(SweetAlertDialog sDialog) {
                                                                    sDialog.dismissWithAnimation();
                                                                    getFeeStructureList();
                                                                }
                                                            });
                                                    dialog.setCancelable(false);
                                                    dialog.show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                }
                                            });
                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });
            holder.ivEditFeeStructure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText editView=new EditText(getContext());
                    editView.setText(""+feeStructure.getAmount());
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit Amount")
                            .setCustomView(editView)
                            .setConfirmText("Update")
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    String price = editView.getText().toString().trim();
                                    if (TextUtils.isEmpty(price)) {
                                        editView.setError("Enter Amount");
                                        editView.requestFocus();
                                        return;
                                    }
                                    float amountInFloat =0;
                                    try {
                                        amountInFloat = Float.parseFloat(price);
                                    }catch (Exception e){
                                        editView.setError("Enter valid Amount");
                                        editView.requestFocus();
                                        return;
                                    }
                                    feeStructure.setAmount(amountInFloat);
                                    feeStructure.setCreatorId(loggedInUserId);
                                    feeStructure.setModifierId(loggedInUserId);
                                    feeStructure.setCreatorType("A");
                                    feeStructure.setModifierType("A");
                                    sDialog.dismissWithAnimation();
                                    feeStructureCollectionRef.document(feeStructure.getId()).set(feeStructure).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                getFeeStructureList();

                                            } else {
                                                // Log.d(TAG, "Error getting documents: ", task.getException());
                                                System.out.println("Error getting documents: -" + task.getException());
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
            return feeStructureList.size();
        }
    }


}
