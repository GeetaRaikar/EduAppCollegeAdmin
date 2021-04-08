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
import com.padmajeet.eduapp.wowcollege.admin.model.FeedbackCategory;
import com.padmajeet.eduapp.wowcollege.admin.model.Staff;
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
public class FragmentFeedbackCategory extends Fragment {
    private String loggedInUserId, academicYearId;
    private View view;
    private LinearLayout llNoList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference feedbackCategoryCollectionRef = db.collection("FeedbackCategory");
    private CollectionReference feedbackCollectionRef = db.collection("Feedback");
    private EditText etFeedbackCategoryName;
    private ListenerRegistration feedbackCategoryListener;
    private FeedbackCategory feedbackCategory;
    private List<FeedbackCategory> feedbackCategoryList = new ArrayList<>();
    private List<String> alreadyFeedbackCategoryList = new ArrayList<>();
    private RecyclerView rvFeedbackCategory;
    private RecyclerView.Adapter feedbackCategoryAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String name;
    private ImageButton btnSubmit;
    private Staff loggedInUser;
    private String instituteId;
    private Gson gson;
    private SweetAlertDialog pDialog;

    @Override
    public void onStart() {
        super.onStart();
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        feedbackCategoryListener = feedbackCategoryCollectionRef
                .whereEqualTo("instituteId", instituteId)
                .orderBy("category", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (feedbackCategoryList.size() != 0) {
                            feedbackCategoryList.clear();
                        }
                        if (alreadyFeedbackCategoryList.size() != 0) {
                            alreadyFeedbackCategoryList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            feedbackCategory = document.toObject(FeedbackCategory.class);
                            feedbackCategory.setId(document.getId());
                            alreadyFeedbackCategoryList.add(feedbackCategory.getCategory());
                            feedbackCategoryList.add(feedbackCategory);
                        }
                        if (feedbackCategoryList.size() != 0) {
                            rvFeedbackCategory.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            feedbackCategoryAdapter = new FeedbackCategoryAdapter(feedbackCategoryList);
                            feedbackCategoryAdapter.notifyDataSetChanged();
                            rvFeedbackCategory.setAdapter(feedbackCategoryAdapter);
                        } else {
                            rvFeedbackCategory.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (feedbackCategoryListener != null) {
            feedbackCategoryListener.remove();
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_feedback_category, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.feedbackCategory));
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        etFeedbackCategoryName = view.findViewById(R.id.etFeedbackCategoryName);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        //addDialogFeedbackCategory();
        rvFeedbackCategory = view.findViewById(R.id.rvFeedbackCategory);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvFeedbackCategory.setLayoutManager(layoutManager);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etFeedbackCategoryName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etFeedbackCategoryName.setError("Enter Feedback Category Name");
                    etFeedbackCategoryName.requestFocus();
                    return;
                } else {
                    if(Utility.isNumericWithSpace(name)){
                        etFeedbackCategoryName.setError("Invalid Feedback Category Name");
                        etFeedbackCategoryName.requestFocus();
                        return;
                    }else {
                        //TODO
                        String Name = name.replaceAll("\\s+", "");
                        System.out.println("Name " + Name);
                        for (int i = 0; i < alreadyFeedbackCategoryList.size(); i++) {
                            String feedbackCategoryName = alreadyFeedbackCategoryList.get(i).replaceAll("\\s+", "");
                            if (Name.equalsIgnoreCase(feedbackCategoryName)) {
                                System.out.println("equal");
                                etFeedbackCategoryName.setError("Already this feedback category is saved ");
                                etFeedbackCategoryName.requestFocus();
                                return;
                            }
                        }
                    }
                }

                if (pDialog == null && !pDialog.isShowing()) {
                    pDialog.show();
                }
                feedbackCategory = new FeedbackCategory();
                feedbackCategory.setInstituteId(instituteId);
                feedbackCategory.setCategory(name);
                feedbackCategory.setCreatorId(loggedInUserId);
                feedbackCategory.setModifierId(loggedInUserId);
                feedbackCategory.setCreatorType("A");
                feedbackCategory.setModifierType("A");
                addFeedbackCategory();
            }
        });
        return view;
    }

    class FeedbackCategoryAdapter extends RecyclerView.Adapter<FeedbackCategoryAdapter.MyViewHolder> {
        private List<FeedbackCategory> feedbackCategoryList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView FeedbackCategoryName;
            public ImageView ivEditFeedbackCategory, ivDeleteFeedbackCategory;

            public MyViewHolder(View view) {
                super(view);
                FeedbackCategoryName = view.findViewById(R.id.tvFeedbackCategory);
                ivEditFeedbackCategory = view.findViewById(R.id.ivEditFeedbackCategory);
                ivDeleteFeedbackCategory = view.findViewById(R.id.ivDeleteFeedbackCategory);
            }
        }


        public FeedbackCategoryAdapter(List<FeedbackCategory> feedbackCategoryList) {
            this.feedbackCategoryList = feedbackCategoryList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_feedback_category, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final FeedbackCategory feedbackCategory = feedbackCategoryList.get(position);
            holder.FeedbackCategoryName.setText("" + feedbackCategory.getCategory());
            holder.ivEditFeedbackCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText editText = new EditText(getContext());
                    editText.setText(feedbackCategory.getCategory());
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit FeedBack Category")
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
                                    String category = editText.getText().toString().trim();
                                    if (TextUtils.isEmpty(category)) {
                                        editText.setError("Enter Feedback Category");
                                        editText.requestFocus();
                                        return;
                                    }else {
                                        if(Utility.isNumericWithSpace(category)){
                                            editText.setError("Invalid Feedback Category");
                                            editText.requestFocus();
                                            return;
                                        }else {
                                            String Name = feedbackCategory.getCategory();
                                            Name = Name.replaceAll("\\s+", "");
                                            System.out.println("Name " + Name);
                                            String EditName = category.replaceAll("\\s+", "");
                                            System.out.println("EditName " + EditName);
                                            if (!Name.equalsIgnoreCase(EditName)) {
                                                for (int i = 0; i < alreadyFeedbackCategoryList.size(); i++) {
                                                    String feedback_category_name = alreadyFeedbackCategoryList.get(i).replaceAll("\\s+", "");
                                                    System.out.println("feedback_category_name " + feedback_category_name);
                                                    if (EditName.equalsIgnoreCase(feedback_category_name)) {
                                                        System.out.print("flag ");
                                                        editText.setError("Already this feedback category is saved");
                                                        editText.requestFocus();
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    feedbackCategory.setCategory(category);
                                    feedbackCategory.setModifiedDate(new Date());
                                    feedbackCategory.setModifierId(loggedInUserId);
                                    if (pDialog == null && !pDialog.isShowing()) {
                                        pDialog.show();
                                    }
                                    feedbackCategoryCollectionRef.document(feedbackCategory.getId()).set(feedbackCategory).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (pDialog != null) {
                                                pDialog.dismiss();
                                            }
                                            if (task.isSuccessful()) {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Updated")
                                                        .setContentText("Feedback category has been updated.")
                                                        .setConfirmText("Ok");
                                                dialog.setCancelable(false);
                                                dialog.show();
                                            } else {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Unable to update feedback category")
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
            holder.ivDeleteFeedbackCategory.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    deleteFeedbackCategory(feedbackCategory);
                }
            });
        }

        @Override
        public int getItemCount() {
            return feedbackCategoryList.size();
        }
    }

    private void deleteFeedbackCategory(FeedbackCategory feedbackCategory) {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        feedbackCategoryCollectionRef
                .whereEqualTo("feedbackCategoryId", feedbackCategory.getId())
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
                                    .setContentText("Do you want to delete " + feedbackCategory.getCategory() + "?")
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
                                            feedbackCategoryCollectionRef.document(feedbackCategory.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            if (pDialog != null) {
                                                                pDialog.dismiss();
                                                            }
                                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                                    .setTitleText("Deleted")
                                                                    .setContentText("Feedback category has been deleted.")
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
                                                                    .setTitleText("Unable to delete feedback category")
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
                                    .setTitleText("Unable to delete feedback category")
                                    .setContentText("In this feedback category some feedback are there.")
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

    private void addFeedbackCategory() {
        feedbackCategoryCollectionRef
                .add(feedbackCategory)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Successfully")
                                .setContentText("Feedback category has been successfully added.")
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
                                .setTitleText("Unable to add feedback category")
                                .setContentText("Network issue, please check it.")
                                .setConfirmText("Ok");
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                });
        etFeedbackCategoryName.setText("");
    }

}
