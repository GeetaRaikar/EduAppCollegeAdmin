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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.EventType;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

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
public class FragmentEventType extends Fragment {
    private View view;
    private LinearLayout llNoList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventTypeCollectionRef = db.collection("EventType");
    private CollectionReference eventCollectionRef = db.collection("Event");
    private String name;
    private EditText etEventType;
    private ImageButton btnSubmit;
    private ListenerRegistration eventTypeListener;
    private EventType eventType;
    private List<EventType> eventTypeList = new ArrayList<>();
    private List<String> alreadyEventTypeList = new ArrayList<>();
    private RecyclerView rvEventType;
    private RecyclerView.Adapter eventTypeAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Staff loggedInUser;
    private String instituteId, academicYearId, loggedInUserId, imageUrl = "";
    private Gson gson;
    private SweetAlertDialog pDialog;


    @Override
    public void onStart() {
        super.onStart();
        if(pDialog==null && !pDialog.isShowing()) {
            pDialog.show();
        }
        eventTypeListener = eventTypeCollectionRef
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
                        if (eventTypeList.size() != 0) {
                            eventTypeList.clear();
                        }
                        if (alreadyEventTypeList.size() != 0) {
                            alreadyEventTypeList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            eventType = document.toObject(EventType.class);
                            eventType.setId(document.getId());
                            alreadyEventTypeList.add(eventType.getName());//This is for checking already event type is added or no
                            eventTypeList.add(eventType);
                        }
                        // System.out.println("eventTypeList -"+eventTypeList.size());
                        if (eventTypeList.size() != 0) {
                            rvEventType.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            eventTypeAdapter = new EventTypeAdapter(eventTypeList);
                            eventTypeAdapter.notifyDataSetChanged();
                            rvEventType.setAdapter(eventTypeAdapter);
                        } else {
                            rvEventType.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (eventTypeListener != null) {
            eventTypeListener.remove();
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


    public FragmentEventType() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_event_type, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.eventType));
        etEventType = (EditText) view.findViewById(R.id.etEventType);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        // addDialogEventType();
        rvEventType = view.findViewById(R.id.rvEventType);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvEventType.setLayoutManager(layoutManager);
        // getBatchList();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etEventType.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etEventType.setError("Enter event type name");
                    etEventType.requestFocus();
                    return;
                } else {
                    if (Utility.isNumericWithSpace(name)) {
                        etEventType.setError("Invalid event type name");
                        etEventType.requestFocus();
                        return;
                    }else {
                        //TODO
                        String Name = name.replaceAll("\\s+", "");
                        System.out.println("Name " + Name);
                        for (int i = 0; i < alreadyEventTypeList.size(); i++) {
                            String eventTypeName = alreadyEventTypeList.get(i).replaceAll("\\s+", "");
                            if (Name.equalsIgnoreCase(eventTypeName)) {
                                etEventType.setError("Already this event type is saved ");
                                etEventType.requestFocus();
                                return;
                            }
                        }
                    }
                }
                if(pDialog==null && !pDialog.isShowing()) {
                    pDialog.show();
                }
                eventType = new EventType();
                eventType.setImageUrl(imageUrl);
                eventType.setInstituteId(instituteId);
                eventType.setName(name);
                eventType.setStatus("A");
                eventType.setCreatorId(loggedInUserId);
                eventType.setModifierId(loggedInUserId);
                eventType.setCreatorType("A");
                eventType.setModifierType("A");
                addEventType();
            }
        });
        return view;
    }

    class EventTypeAdapter extends RecyclerView.Adapter<EventTypeAdapter.MyViewHolder> {
        private List<EventType> eventTypeList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView EventTypeName;
            public ImageView ivEditEventType;
            public ImageView ivDeleteEventType;

            public MyViewHolder(View view) {
                super(view);
                EventTypeName = view.findViewById(R.id.tvEventTypeName);
                ivEditEventType = view.findViewById(R.id.ivEditEventType);
                ivDeleteEventType = view.findViewById(R.id.ivDeleteEventType);
            }
        }


        public EventTypeAdapter(List<EventType> eventTypeList) {
            this.eventTypeList = eventTypeList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_event_type, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final EventType eventType = eventTypeList.get(position);
            holder.EventTypeName.setText("" + eventType.getName());
            holder.ivEditEventType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText editText = new EditText(getContext());
                    editText.setText(eventType.getName());
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText(getResources().getString(R.string.editEventType))
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
                                    String eventTypeName = editText.getText().toString().trim();
                                    if (TextUtils.isEmpty(eventTypeName)) {
                                        editText.setError("Enter event type name");
                                        editText.requestFocus();
                                        return;
                                    }else {
                                        if(Utility.isNumericWithSpace(eventTypeName)){
                                            editText.setError("Invalid event type name");
                                            editText.requestFocus();
                                            return;
                                        }else{
                                            String Name = eventType.getName();
                                            Name = Name.replaceAll("\\s+", "");
                                            System.out.println("Name " + Name);
                                            String EditName = eventTypeName.replaceAll("\\s+", "");
                                            System.out.println("EditName " + EditName);
                                            if (!Name.equalsIgnoreCase(EditName)) {
                                                for (int i = 0; i < alreadyEventTypeList.size(); i++) {
                                                    String event_type_name = alreadyEventTypeList.get(i).replaceAll("\\s+", "");
                                                    System.out.println("event_type_name " + event_type_name);
                                                    if (EditName.equalsIgnoreCase(event_type_name)) {
                                                        System.out.print("flag ");
                                                        editText.setError("Already this event type is saved");
                                                        editText.requestFocus();
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    eventType.setName(eventTypeName);
                                    eventType.setModifiedDate(new Date());
                                    eventType.setModifierId(loggedInUserId);
                                    if (!pDialog.isShowing() && pDialog == null) {
                                        pDialog.show();
                                    }
                                    eventTypeCollectionRef.document(eventType.getId()).set(eventType).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (pDialog != null) {
                                                pDialog.dismiss();
                                            }
                                            if (task.isSuccessful()) {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Update")
                                                        .setContentText("Event Type Is Successfully Updated.")
                                                        .setConfirmText("Ok");
                                                dialog.setCancelable(false);
                                                dialog.show();
                                            } else {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Unable to edit event type")
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
            holder.ivDeleteEventType.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    deleteEventType(eventType);
                }
            });
        }

        @Override
        public int getItemCount() {
            return eventTypeList.size();
        }
    }

    private void addEventType() {
        eventTypeCollectionRef
                .add(eventType)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Successfully")
                                .setContentText("Event type has been successfully added.")
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
        etEventType.setText("");
    }

    private void deleteEventType(EventType eventType) {
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        eventCollectionRef
                .whereEqualTo("typeId", eventType.getId())
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
                                    .setContentText("Do you want to delete " + eventType.getName() + "?")
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
                                            if (pDialog != null && !pDialog.isShowing()) {
                                                pDialog.show();
                                            }
                                            if (!TextUtils.isEmpty(eventType.getImageUrl())) {
                                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(eventType.getImageUrl());
                                                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // File deleted successfully
                                                        System.out.println("firebase storage onSuccess: deleted file");
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        // Uh-oh, an error occurred!
                                                        System.out.println("firebase storage onFailure: did not delete file");
                                                    }
                                                });
                                            }
                                            eventTypeCollectionRef.document(eventType.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            if (pDialog != null) {
                                                                pDialog.dismiss();
                                                            }
                                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                                    .setTitleText("Deleted")
                                                                    .setContentText("Event type has been deleted.")
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
                                                                    .setTitleText("Unable to delete event type")
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
                                    .setTitleText("Unable to delete event type")
                                    .setContentText("In this event type some events are there.")
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

}
