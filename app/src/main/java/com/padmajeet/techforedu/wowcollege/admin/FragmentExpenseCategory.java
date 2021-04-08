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
import com.padmajeet.techforedu.wowcollege.admin.model.ExpenseCategory;
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
public class FragmentExpenseCategory extends Fragment {

    private View view;
    private LinearLayout llNoList;
    private String academicYearId, loggedInUserId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference expenseCategoryCollectionRef = db.collection("ExpenseCategory");
    private CollectionReference expenseCollectionRef = db.collection("Expense");
    private EditText etExpenseCategoryName;
    private ListenerRegistration expenseCategoryListener;
    private ExpenseCategory expenseCategory;
    private List<ExpenseCategory> expenseCategoryList = new ArrayList<>();
    private List<String> alreadyExpenseCategoryList = new ArrayList<>();
    private RecyclerView rvExpenseCategory;
    private RecyclerView.Adapter expenseCategoryAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ImageButton btnSubmit;
    private String name;
    private Staff loggedInUser;
    private String instituteId;
    private Gson gson;
    private SweetAlertDialog pDialog;


    @Override
    public void onStart() {
        super.onStart();
        if (pDialog.isShowing() && pDialog == null) {
            pDialog.show();
        }
        expenseCategoryListener = expenseCategoryCollectionRef
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
                        if (expenseCategoryList.size() != 0) {
                            expenseCategoryList.clear();
                        }
                        if (alreadyExpenseCategoryList.size() != 0) {
                            alreadyExpenseCategoryList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            expenseCategory = document.toObject(ExpenseCategory.class);
                            expenseCategory.setId(document.getId());
                            alreadyExpenseCategoryList.add(expenseCategory.getName());
                            expenseCategoryList.add(expenseCategory);
                        }
                        if (expenseCategoryList.size() != 0) {
                            rvExpenseCategory.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            expenseCategoryAdapter = new ExpenseCategoryAdapter(expenseCategoryList);
                            expenseCategoryAdapter.notifyDataSetChanged();
                            rvExpenseCategory.setAdapter(expenseCategoryAdapter);

                        } else {
                            rvExpenseCategory.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (expenseCategoryListener != null) {
            expenseCategoryListener.remove();
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
        instituteId =  sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }


    public FragmentExpenseCategory() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_expense_category, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.expenseCategory));
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        rvExpenseCategory = view.findViewById(R.id.rvExpenseCategory);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvExpenseCategory.setLayoutManager(layoutManager);
        etExpenseCategoryName = view.findViewById(R.id.etExpenseCategoryName);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etExpenseCategoryName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etExpenseCategoryName.setError("Enter Expense Category Name");
                    etExpenseCategoryName.requestFocus();
                    return;
                } else {
                    if (Utility.isNumericWithSpace(name)) {
                        etExpenseCategoryName.setError("Invalid Expense Category Name");
                        etExpenseCategoryName.requestFocus();
                        return;
                    }else {
                        String Name = name.replaceAll("\\s+", "");
                        System.out.println("Name " + Name);
                        for (int i = 0; i < alreadyExpenseCategoryList.size(); i++) {
                            String expenseCategoryName = alreadyExpenseCategoryList.get(i).replaceAll("\\s+", "");
                            if (Name.equalsIgnoreCase(expenseCategoryName)) {
                                System.out.println("equal");
                                etExpenseCategoryName.setError("Already this expense category is saved");
                                etExpenseCategoryName.requestFocus();
                                return;
                            }
                        }
                    }
                }
                if (!pDialog.isShowing() && pDialog == null) {
                    pDialog.show();
                }
                expenseCategory = new ExpenseCategory();
                expenseCategory.setInstituteId(instituteId);
                expenseCategory.setName(name);
                expenseCategory.setCreatorId(loggedInUserId);
                expenseCategory.setModifierId(loggedInUserId);
                expenseCategory.setCreatorType("A");
                expenseCategory.setModifierType("A");
                addExpenseCategory();
            }
        });
        return view;
    }

    class ExpenseCategoryAdapter extends RecyclerView.Adapter<ExpenseCategoryAdapter.MyViewHolder> {
        private List<ExpenseCategory> expenseCategoryList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView ExpenseCategoryName;
            public ImageView ivEditExpenseCategory, ivDeleteExpenseCategory;

            public MyViewHolder(View view) {
                super(view);
                ExpenseCategoryName = view.findViewById(R.id.tvExpenseCategory);
                ivEditExpenseCategory = view.findViewById(R.id.ivEditExpenseCategory);
                ivDeleteExpenseCategory = view.findViewById(R.id.ivDeleteExpenseCategory);
            }
        }


        public ExpenseCategoryAdapter(List<ExpenseCategory> expenseCategoryList) {
            this.expenseCategoryList = expenseCategoryList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_expense_category, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final ExpenseCategory expenseCategory = expenseCategoryList.get(position);
            holder.ExpenseCategoryName.setText("" + expenseCategory.getName());
            holder.ivEditExpenseCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText editText = new EditText(getContext());
                    editText.setText(expenseCategory.getName());
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit Expense Category")
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
                                    String expenseCategoryName = editText.getText().toString().trim();
                                    if (TextUtils.isEmpty(expenseCategoryName)) {
                                        editText.setError("Enter Expense Category");
                                        editText.requestFocus();
                                        return;
                                    }else{
                                        if(Utility.isNumericWithSpace(expenseCategoryName)){
                                            editText.setError("Invalid Expense Category");
                                            editText.requestFocus();
                                            return;
                                        }else{
                                            String Name = expenseCategory.getName();
                                            Name = Name.replaceAll("\\s+", "");
                                            System.out.println("Name " + Name);
                                            String EditName = expenseCategoryName.replaceAll("\\s+", "");
                                            System.out.println("EditName " + EditName);
                                            if (!Name.equalsIgnoreCase(EditName)) {
                                                for (int i = 0; i < alreadyExpenseCategoryList.size(); i++) {
                                                    String expense_category_name = alreadyExpenseCategoryList.get(i).replaceAll("\\s+", "");
                                                    System.out.println("expense_category_name " + expense_category_name);
                                                    if (EditName.equalsIgnoreCase(expense_category_name)) {
                                                        System.out.print("flag ");
                                                        editText.setError("Already this expense category is saved");
                                                        editText.requestFocus();
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    expenseCategory.setName(expenseCategoryName);
                                    expenseCategory.setModifiedDate(new Date());
                                    expenseCategory.setModifierId(loggedInUserId);
                                    if (pDialog != null && !pDialog.isShowing()) {
                                        pDialog.show();
                                    }
                                    expenseCategoryCollectionRef.document(expenseCategory.getId()).set(expenseCategory).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                if (pDialog != null) {
                                                    pDialog.dismiss();
                                                }
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Updated")
                                                        .setContentText("Expense category has been updated.")
                                                        .setConfirmText("Ok");
                                                dialog.setCancelable(false);
                                                dialog.show();

                                            } else {
                                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Unable to update expense category")
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
            holder.ivDeleteExpenseCategory.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    deleteExpenseCategory(expenseCategory);
                }
            });


        }

        @Override
        public int getItemCount() {
            return expenseCategoryList.size();
        }
    }

    private void deleteExpenseCategory(ExpenseCategory expenseCategory) {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        expenseCollectionRef
                .whereEqualTo("expenseCategoryId", expenseCategory.getId())
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
                                    .setContentText("Do you want to delete " + expenseCategory.getName() + "?")
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
                                            expenseCategoryCollectionRef.document(expenseCategory.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            if (pDialog != null) {
                                                                pDialog.dismiss();
                                                            }
                                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                                    .setTitleText("Deleted")
                                                                    .setContentText("Expense category has been deleted.")
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
                                                                    .setTitleText("Unable to delete expense category")
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
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Unable to delete expense category")
                                    .setContentText("In this expense category some expenses are there.")
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

    private void addExpenseCategory() {
        expenseCategoryCollectionRef
                .add(expenseCategory)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Successfully")
                                .setContentText("Expense category has been successfully added.")
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
                                .setTitleText("Unable to add expense category")
                                .setContentText("Network issue, please check it.")
                                .setConfirmText("Ok");
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                });
        etExpenseCategoryName.setText("");
    }
}
