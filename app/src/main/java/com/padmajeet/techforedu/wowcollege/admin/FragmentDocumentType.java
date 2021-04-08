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
import com.padmajeet.techforedu.wowcollege.admin.model.DocumentType;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentDocumentType extends Fragment {

    private View view;
    private LinearLayout llNoList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference documentTypeCollectionRef = db.collection("DocumentType");
    private CollectionReference documentCollectionRef=db.collection("Document");
    private String name;
    private EditText etDocumentType;
    private ImageButton btnSubmit;
    private ListenerRegistration documentTypeListener;
    private DocumentType documentType;
    private List<DocumentType> documentTypeList = new ArrayList<>();
    private List<String> alreadyDocumentTypeList=new ArrayList<>();
    private RecyclerView rvDocumentType;
    private RecyclerView.Adapter documentTypeAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Staff loggedInUser;
    private String loggedInUserId, instituteId,academicYearId;
    private Gson gson;
    private SweetAlertDialog pDialog;

    public FragmentDocumentType() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        if(pDialog==null && !pDialog.isShowing()) {
            pDialog.show();
        }
        documentTypeListener = documentTypeCollectionRef
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
                        if (documentTypeList.size() != 0) {
                            documentTypeList.clear();
                        }
                        if (alreadyDocumentTypeList.size() != 0) {
                            alreadyDocumentTypeList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            documentType = document.toObject(DocumentType.class);
                            documentType.setId(document.getId());
                            alreadyDocumentTypeList.add(documentType.getType());
                            documentTypeList.add(documentType);
                        }
                        if (documentTypeList.size() != 0) {
                            rvDocumentType.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            documentTypeAdapter = new DocumentTypeAdapter(documentTypeList);
                            documentTypeAdapter.notifyDataSetChanged();
                            rvDocumentType.setAdapter(documentTypeAdapter);
                        } else {
                            rvDocumentType.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (documentTypeListener != null) {
            documentTypeListener.remove();
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
        pDialog=Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_document_type, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.documentType));
        etDocumentType = (EditText) view.findViewById(R.id.etDocumentType);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        // addDialogEventType();
        rvDocumentType = view.findViewById(R.id.rvDocumentType);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvDocumentType.setLayoutManager(layoutManager);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etDocumentType.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etDocumentType.setError("Enter document type name");
                    etDocumentType.requestFocus();
                    return;
                }else{
                    if(Utility.isNumericWithSpace(name)){
                        etDocumentType.setError("Invalid document type name");
                        etDocumentType.requestFocus();
                        return;
                    }else {
                        //TODO
                        String Name = name.replaceAll("\\s+", "");
                        System.out.println("Name " + Name);
                        for (int i = 0; i < alreadyDocumentTypeList.size(); i++) {
                            String eventTypeName = alreadyDocumentTypeList.get(i).replaceAll("\\s+", "");
                            if (Name.equalsIgnoreCase(eventTypeName)) {
                                etDocumentType.setError("Already this event type is saved ");
                                etDocumentType.requestFocus();
                                return;
                            }
                        }
                    }
                }
                if(!pDialog.isShowing() && pDialog==null){
                    pDialog.show();
                }
                documentType = new DocumentType();
                documentType.setInstituteId(instituteId);
                documentType.setType(name);
                documentType.setStatus("A");
                documentType.setCreatorId(loggedInUserId);
                documentType.setModifierId(loggedInUserId);
                documentType.setCreatorType("A");
                documentType.setModifierType("A");
                addDocumentType();
            }
        });
        return view;
    }

    class DocumentTypeAdapter extends RecyclerView.Adapter<DocumentTypeAdapter.MyViewHolder> {
        private List<DocumentType> documentTypeList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView DocumentTypeName;
            public ImageView ivEditDocumentType,ivDeleteDocumentType;

            public MyViewHolder(View view) {
                super(view);
                DocumentTypeName = view.findViewById(R.id.tvDocumentTypeName);
                ivEditDocumentType = view.findViewById(R.id.ivEditDocumentType);
                ivDeleteDocumentType = view.findViewById(R.id.ivDeleteDocumentType);
            }
        }


        public DocumentTypeAdapter(List<DocumentType> documentTypeList) {
            this.documentTypeList = documentTypeList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_document_type, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final DocumentType documentType = documentTypeList.get(position);
            holder.DocumentTypeName.setText("" + documentType.getType());
            holder.ivEditDocumentType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText editText = new EditText(getContext());
                    editText.setText(documentType.getType());
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText(getResources().getString(R.string.editDocumentType))
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
                                    String documentTypeName = editText.getText().toString().trim();
                                    if (TextUtils.isEmpty(documentTypeName)) {
                                        editText.setError("Enter document type name");
                                        editText.requestFocus();
                                        return;
                                    }else {
                                        if(Utility.isNumericWithSpace(documentTypeName)){
                                            editText.setError("Invalid document type name");
                                            editText.requestFocus();
                                            return;
                                        }else {
                                            String Name = documentType.getType();
                                            Name = Name.replaceAll("\\s+", "");
                                            System.out.println("Name " + Name);
                                            String EditName = documentTypeName.replaceAll("\\s+", "");
                                            System.out.println("EditName " + EditName);
                                            if (!Name.equalsIgnoreCase(EditName)) {
                                                for (int i = 0; i < alreadyDocumentTypeList.size(); i++) {
                                                    String document_type_name = alreadyDocumentTypeList.get(i).replaceAll("\\s+", "");
                                                    System.out.println("document_type_name " + document_type_name);
                                                    if (EditName.equalsIgnoreCase(document_type_name)) {
                                                        System.out.print("flag ");
                                                        editText.setError("Already this document type is saved");
                                                        editText.requestFocus();
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    documentType.setType(documentTypeName);
                                    documentType.setModifiedDate(new Date());
                                    documentType.setModifierId(loggedInUserId);

                                    if(!pDialog.isShowing() && pDialog==null){
                                        pDialog.show();
                                    }
                                    documentTypeCollectionRef.document(documentType.getId()).set(documentType).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (pDialog != null) {
                                                pDialog.dismiss();
                                            }
                                            if (task.isSuccessful()) {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Updated")
                                                        .setContentText("Document type has been updated.")
                                                        .setConfirmText("Ok");
                                                dialog.setCancelable(false);
                                                dialog.show();
                                            } else {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Unable to update document type")
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
            holder.ivDeleteDocumentType.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    deleteDocumentType(documentType);
                }
            });
        }

        @Override
        public int getItemCount() {
            return documentTypeList.size();
        }
    }

    private void deleteDocumentType(DocumentType documentType){
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        documentCollectionRef
                .whereEqualTo("typeId",documentType.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        if(documentSnapshots.size() == 0){
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Delete")
                                    .setContentText("Do you want to delete "+documentType.getType()+" ?")
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
                                            documentTypeCollectionRef.document(documentType.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            if(pDialog != null){
                                                                pDialog.dismiss();
                                                            }
                                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                                    .setTitleText("Deleted")
                                                                    .setContentText("Document type has been deleted.")
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
                                                                    .setTitleText("Unable to delete document type")
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
                        }else{
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Unable to delete document type")
                                    .setContentText("In this document type some events are there.")
                                    .setConfirmText("Ok");
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        System.out.println("Error getting documents:"+e);
                    }
                });
    }
    private void addDocumentType() {

        documentTypeCollectionRef
                .add(documentType)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Successfully")
                                .setContentText("Document type has been successfully added.")
                                .setConfirmText("Ok");
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Unable to add document type")
                                .setContentText("Network issue, please check it.")
                                .setConfirmText("Ok");
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                });
        etDocumentType.setText("");
    }
}
