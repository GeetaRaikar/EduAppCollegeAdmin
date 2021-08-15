package com.padmajeet.techforedu.wowcollege.admin;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.AcademicYear;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentProfile extends Fragment {
    private View view;
    private Staff loggedInUser;
    private TextView etMobileNumber;
    private EditText etFirstName,etLastName,etEmail,etAddress;
    private ImageView ivProfilePic;
    private Button btUpdateProfile;
    boolean isEmailEdited, isFirstNameEdited, isLastNameEdited, isAddressEdited;
    private Fragment currentFragment;
    private Gson gson;
    private String loggedInUserId;
    private TextView tvResetPassword;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference staffCollectionRef=db.collection("Staff");


    public FragmentProfile() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        String adminJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(adminJson, Staff.class);
        loggedInUserId= sessionManager.getString("loggedInUserId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.profile));

        currentFragment = this;
        isEmailEdited = false;
        isFirstNameEdited = false;
        isLastNameEdited = false;
        isAddressEdited = false;
        btUpdateProfile = (Button) view.findViewById(R.id.btUpdateProfile);
        btUpdateProfile.setVisibility(View.INVISIBLE);

        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        if (!TextUtils.isEmpty(loggedInUser.getImageUrl())) {
            Glide.with(this)
                    .load(loggedInUser.getImageUrl())
                    .fitCenter()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_profile_large)
                    .into(ivProfilePic);
        }

        etMobileNumber = (TextView) view.findViewById(R.id.etMobileNumber);
        etMobileNumber.setText(loggedInUser.getMobileNumber());

        etEmail = (EditText) view.findViewById(R.id.etEmail);
        if (TextUtils.isEmpty(loggedInUser.getEmailId())) {
            etEmail.setHint(getString(R.string.unavailable));
        } else {
            etEmail.setText(loggedInUser.getEmailId());
        }

        etFirstName = (EditText) view.findViewById(R.id.etFirstName);
        etFirstName.setText(loggedInUser.getFirstName());

        etLastName = (EditText) view.findViewById(R.id.etLastName);
        if (TextUtils.isEmpty(loggedInUser.getLastName())) {
            etLastName.setHint(getString(R.string.lastName_unavailable));
        } else {
            etLastName.setText(loggedInUser.getLastName());
        }

        etAddress = (EditText) view.findViewById(R.id.etAddress);
        if (TextUtils.isEmpty(loggedInUser.getAddress())) {
            etAddress.setHint(getString(R.string.lastName_unavailable));
        } else {
            etAddress.setText(loggedInUser.getAddress());
        }

        String name = "";
        if (!TextUtils.isEmpty(loggedInUser.getFirstName())) {
            name = loggedInUser.getFirstName();
        }
        if (!TextUtils.isEmpty(loggedInUser.getLastName())) {
            name = name + " " + loggedInUser.getLastName();
        }
        ((TextView) view.findViewById(R.id.tvName)).setText(name);



        ImageView ivEditEmail = (ImageView) view.findViewById(R.id.ivEditEmail);
        ivEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEmailEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etEmail.setEnabled(true);
                etEmail.requestFocus();
            }
        });

        ImageView ivEditFirstName = (ImageView) view.findViewById(R.id.ivEditFirstName);
        ivEditFirstName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFirstNameEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etFirstName.setEnabled(true);
                etFirstName.requestFocus();
            }
        });

        ImageView ivEditLastName = (ImageView) view.findViewById(R.id.ivEditLastName);
        ivEditLastName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLastNameEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etLastName.setEnabled(true);
                etLastName.requestFocus();
            }
        });

        ImageView ivEditAddress = (ImageView) view.findViewById(R.id.ivEditAddress);
        ivEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAddressEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etAddress.setEnabled(true);
                etAddress.requestFocus();
            }
        });

        tvResetPassword = view.findViewById(R.id.tvResetPassword);
        tvResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Reset Password")
                        .setContentText("After password reset you need to login again. Do you want to proceed?")
                        .setConfirmText("Proceed")
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
                                Intent intent = new Intent(getActivity(), ActivityForgotPassword.class);
                                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                startActivity(intent);
                                //getActivity().finish();
                            }
                        });
                dialog.setCancelable(false);
                dialog.show();
            }
        });
        btUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserProfile();
            }
        });

        return view;
    }

    void updateUserProfile() {
        boolean canSave = true;
        if (isEmailEdited) {
            String updatedEmail = etEmail.getText().toString().trim();
            if (!Utility.isEmailValid(updatedEmail)) {
                etEmail.setError(getString(R.string.errInvalidEmail));
                canSave = false;
            } else {
                loggedInUser.setEmailId(updatedEmail);
            }
        }
        if (isFirstNameEdited) {
            String updatedFirstName = etFirstName.getText().toString().trim();
            if (TextUtils.isEmpty(updatedFirstName)) {
                etFirstName.setError("Enter First Name");
                etFirstName.requestFocus();
                canSave = false;
            } else {
                loggedInUser.setFirstName(updatedFirstName);
            }
        }
        if (isLastNameEdited) {
            String updatedLastName = etLastName.getText().toString().trim();
            if (TextUtils.isEmpty(updatedLastName)) {
                etLastName.setError("Enter Last Name");
                etLastName.requestFocus();
                canSave = false;
            } else {
                loggedInUser.setLastName(updatedLastName);
            }
        }
        if (isAddressEdited) {
            String updatedAddress = etAddress.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedAddress)) {
                if(Utility.isNumericWithSpace(updatedAddress)){
                    etAddress.setError("Enter valid address");
                    etAddress.requestFocus();
                    canSave = false;
                } else {
                    loggedInUser.setAddress(updatedAddress);
                }
            }
        }
        if (canSave) {
            loggedInUser.setModifiedDate(new Date());
            etEmail.setEnabled(false);
            etFirstName.setEnabled(false);
            etLastName.setEnabled(false);
            etAddress.setEnabled(false);
            btUpdateProfile.setVisibility(View.GONE);
            //Update

            staffCollectionRef.document(loggedInUserId).set(loggedInUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SessionManager sessionManager= new SessionManager(getContext());
                        String userJson=gson.toJson(loggedInUser);
                        sessionManager.putString("loggedInUser",userJson);
                    } else {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
