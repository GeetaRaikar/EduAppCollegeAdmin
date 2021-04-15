package com.padmajeet.techforedu.wowcollege.admin;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import cn.pedant.SweetAlert.SweetAlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Parent;
import com.padmajeet.techforedu.wowcollege.admin.model.Section;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.model.Student;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentUpdateStudent extends Fragment {
    private Bundle bundle = new Bundle();
    private Gson gson;
    private SweetAlertDialog pDialog;
    private View view = null;
    private String loggedInUserId,academicYearId;
    private EditText etFirstName,etLastName,etMobileNumber,etFatherFirstName,etFatherMiddleName,etFatherMobileNumber;
    private EditText etEmailId,etEmergencyContact,etAddress,etUSN;
    private Button btUpdate;
    private boolean isFirstNameEdited, isLastNameEdited, isMobileNumberEdited,isFatherMobileNumberEdited,isStatusSwitched,isUSNEdited;
    private boolean isEmailIdEdited, isFatherFirstNameEdited, isFatherMiddleNameEdited,isAddressEdited,isSectionEdited;
    private boolean isStudentEdited,isParentEdited;
    private List<Section> sectionList=new ArrayList<>();
    private Section section,selectedSection;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference studentCollectionRef=db.collection("Student");
    private CollectionReference parentCollectionRef=db.collection("Parent");
    private CollectionReference sectionCollectionRef=db.collection("Section");
    private Fragment currentFragment;
    private StudentParent selectedStudentParent;
    private Student selectedStudent;
    private Staff loggedInUser;
    private String instituteId;
    private Switch swStatus;
    private String status;
    private String updatedMiddleName;
    private Parent selectedParent;
    private Spinner spSection;

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
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    public FragmentUpdateStudent() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_update_student, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.students));
        bundle = getArguments();
        String selectedStudentParentJson = bundle.getString("selectedStudentParent");
       // System.out.println("selectedStudentJson - " + selectedStudentJson);
        gson = Utility.getGson();
        selectedStudentParent = gson.fromJson(selectedStudentParentJson, StudentParent.class);
        selectedStudent = selectedStudentParent.student;
        //selectedExamSeriesByExam=gson.toJson(currentExamSeries);
        selectedParent = selectedStudentParent.parent;
        currentFragment = this;
        spSection = view.findViewById(R.id.spSection);
        btUpdate = (Button) view.findViewById(R.id.btUpdate);
        btUpdate.setVisibility(View.INVISIBLE);

        //getSection();

        etFirstName = (EditText) view.findViewById(R.id.etUSN);
        String firstName = selectedStudent.getFirstName();

        etUSN = (EditText) view.findViewById(R.id.etUSN);
        String usn = selectedStudent.getUsn();
        String unAvailable = getString(R.string.unavailable);
        if (TextUtils.isEmpty(usn)) {
            etUSN.setHint(unAvailable);
        } else {
            etUSN.setText(usn);
        }

        etFirstName.setText(firstName);
        etLastName = (EditText) view.findViewById(R.id.etLastName);
        String lastName = selectedStudent.getLastName();
        if (TextUtils.isEmpty(lastName)) {
            etLastName.setHint(unAvailable);
        } else {
            etLastName.setText(lastName);
        }

        etMobileNumber = (EditText) view.findViewById(R.id.etMobileNumber);
        String mobileNumber = selectedStudent.getMobileNumber();
        etMobileNumber.setText(mobileNumber);

        etEmailId = (EditText) view.findViewById(R.id.etEmailId);
        String emailId = selectedStudent.getEmailId();
        if (TextUtils.isEmpty(emailId)) {
            etEmailId.setHint(unAvailable);
        } else {
            etEmailId.setText(emailId);
        }

        etFatherFirstName = (EditText) view.findViewById(R.id.etFatherFirstName);
        String fatherFirstName = selectedParent.getFirstName();
        etFatherFirstName.setText(fatherFirstName);

        etFatherMiddleName = (EditText) view.findViewById(R.id.etFatherMiddleName);
        String fatherMiddleName = selectedParent.getMiddleName();
        if (TextUtils.isEmpty(fatherMiddleName)) {
            etFatherMiddleName.setHint(unAvailable);
        } else {
            etFatherMiddleName.setText(fatherMiddleName);
        }

        etFatherMobileNumber = (EditText) view.findViewById(R.id.etFatherMobileNumber);
        String fatherMobileNumber = selectedParent.getMobileNumber();
        if (TextUtils.isEmpty(fatherMobileNumber)) {
            etFatherMobileNumber.setHint(unAvailable);
        } else {
            etFatherMobileNumber.setText(fatherMobileNumber);
        }

        etAddress = view.findViewById(R.id.etAddress);
        String address = selectedParent.getAddress();
        if (TextUtils.isEmpty(address)) {
            etAddress.setHint(unAvailable);
        } else {
            etAddress.setText(address);
        }


        /*ImageView ivEditSection = (ImageView) view.findViewById(R.id.ivEditSection);
        ivEditSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStudentEdited = true;
                isSectionEdited = true;
                btUpdate.setVisibility(View.VISIBLE);
                spSection.setEnabled(true);
            }
        });*/
        ImageView ivEditUSN = (ImageView) view.findViewById(R.id.ivEditUSN);
        ivEditUSN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStudentEdited = true;
                isUSNEdited = true;
                btUpdate.setVisibility(View.VISIBLE);
                etUSN.setEnabled(true);
                etUSN.requestFocus();
            }
        });
        ImageView ivEditFirstName = (ImageView) view.findViewById(R.id.ivEditFirstName);
        ivEditFirstName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStudentEdited = true;
                isFirstNameEdited = true;
                btUpdate.setVisibility(View.VISIBLE);
                etFirstName.setEnabled(true);
                etFirstName.requestFocus();
            }
        });

        ImageView ivEditLastName = (ImageView) view.findViewById(R.id.ivEditLastName);
        ivEditLastName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStudentEdited = true;
                isLastNameEdited = true;
                btUpdate.setVisibility(View.VISIBLE);
                etLastName.setEnabled(true);
                etLastName.requestFocus();
            }
        });

        ImageView ivEditMobileNumber = (ImageView) view.findViewById(R.id.ivEditMobileNumber);
        ivEditMobileNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStudentEdited = true;
                isMobileNumberEdited = true;
                btUpdate.setVisibility(View.VISIBLE);
                etMobileNumber.setEnabled(true);
                etMobileNumber.requestFocus();
            }
        });

        ImageView ivEditEmail = (ImageView) view.findViewById(R.id.ivEditEmail);
        ivEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStudentEdited = true;
                isEmailIdEdited = true;
                btUpdate.setVisibility(View.VISIBLE);
                etEmailId.setEnabled(true);
                etEmailId.requestFocus();
            }
        });

        ImageView ivEditFatherFirstName = (ImageView) view.findViewById(R.id.ivEditFatherFirstName);
        ivEditFatherFirstName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isParentEdited = true;
                isFatherFirstNameEdited = true;
                btUpdate.setVisibility(View.VISIBLE);
                etFatherFirstName.setEnabled(true);
                etFatherFirstName.requestFocus();
            }
        });

        ImageView ivEditFatherMiddleName = (ImageView) view.findViewById(R.id.ivEditFatherMiddleName);
        ivEditFatherMiddleName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isParentEdited = true;
                isFatherMiddleNameEdited = true;
                btUpdate.setVisibility(View.VISIBLE);
                etFatherMiddleName.setEnabled(true);
                etFatherMiddleName.requestFocus();
            }
        });

        ImageView ivEditFatherMobileNumber = (ImageView) view.findViewById(R.id.ivEditFatherMobileNumber);
        ivEditFatherMobileNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isParentEdited = true;
                isFatherMobileNumberEdited = true;
                btUpdate.setVisibility(View.VISIBLE);
                etFatherMobileNumber.setEnabled(true);
                etFatherMobileNumber.requestFocus();
            }
        });

        ImageView ivEditAddress = (ImageView) view.findViewById(R.id.ivEditAddress);
        ivEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isParentEdited = true;
                isStudentEdited = true;
                isAddressEdited = true;
                btUpdate.setVisibility(View.VISIBLE);
                etAddress.setEnabled(true);
                etAddress.requestFocus();
            }
        });

        swStatus = view.findViewById(R.id.swStatus);
        swStatus.setTextOn(getString(R.string.active));
        swStatus.setTextOff(getString(R.string.inactive));
        status = selectedStudent.getStatus();
        System.out.println("status - "+status);

        if(status.equals("I")){
            swStatus.setChecked(false);
        }
        else{
            swStatus.setChecked(true);
        }

        swStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    if(selectedStudent.getStatus().equals("I")){
                        status = "A";
                        isParentEdited = true;
                        isStudentEdited = true;
                        btUpdate.setVisibility(View.VISIBLE);
                    }
                    else if(isParentEdited || isStudentEdited){

                    }
                    else{
                        btUpdate.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    if(!selectedStudent.getStatus().equals("I")){
                        status = "I";
                        isParentEdited = true;
                        isStudentEdited = true;
                        btUpdate.setVisibility(View.VISIBLE);
                    }
                    else if(isParentEdited || isStudentEdited){

                    }
                    else{
                        btUpdate.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStudent();
            }
        });
        return view;
    }

    private void getSection(){
        if(sectionList.size()!=0){
            sectionList.clear();
        }

        final SweetAlertDialog pDialog;
        pDialog = Utility.createSweetAlertDialog(getContext());
        pDialog.show();
        sectionCollectionRef
                .whereEqualTo("batchId",selectedStudent.getCurrentBatchId())
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
                                section = document.toObject(Section.class);
                                section.setId(document.getId());
                                //System.out.println("Section Name-" + section.getName());
                                sectionList.add(section);
                            }
                            if(sectionList.size()!=0) {
                                spSection.setEnabled(true);
                                List<String> sectionNameList = new ArrayList<String>();
                                for (Section section : sectionList) {
                                    sectionNameList.add(section.getName());
                                }
                                ArrayAdapter<String> sectionAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sectionNameList);
                                sectionAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spSection.setAdapter(sectionAdaptor);
                                for (int i=0;i<sectionList.size();i++){
                                    /*if(sectionList.get(i).getId().equals(selectedStudent.getSectionId())){
                                        spSection.setSelection(sectionAdaptor.getPosition(sectionList.get(i).getName()));
                                        break;
                                    }*/
                                }
                                spSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        //System.out.println("Position " + position);
                                        selectedSection = sectionList.get(position);
                                        isStudentEdited = true;
                                        isSectionEdited = true;
                                        //selectedStudent.setSectionId(selectedSection.getId());
                                        btUpdate.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            }else{
                                spSection.setEnabled(false);
                            }
                        } else {

                            spSection.setEnabled(false);
                            //Log.w(TAG, "Error getting documents.", task.getException());
                            //System.out.println("Error getting documents: " + task.getException());
                        }
                    }
                });
        // [END get_all_users]

    }

    void updateStudent() {
        boolean canSave = true;


        if (isSectionEdited) {
            //selectedStudent.setSectionId(selectedSection.getId());
        }
        if (isUSNEdited) {
            String updatedUSN= etUSN.getText().toString().trim();
            if (TextUtils.isEmpty(updatedUSN)) {
                etUSN.setError("Enter usn");
                etUSN.requestFocus();
                canSave = false;
                return;
            } else {
                selectedStudent.setUsn(updatedUSN);
            }
        }
        if (isFirstNameEdited) {
            String updatedFirstName = etFirstName.getText().toString().trim();
            if (TextUtils.isEmpty(updatedFirstName)) {
                etFirstName.setError("Enter First Name");
                etFirstName.requestFocus();
                canSave = false;
                return;
            } else {
                if(!Utility.isAlphabetic(updatedFirstName)){
                    etFirstName.setError("First name must be alphabetic");
                    etFirstName.requestFocus();
                    canSave = false;
                    return;
                }else{
                    selectedStudent.setFirstName(updatedFirstName);
                }
            }
        }

        if (isLastNameEdited) {
            String updatedLastName = etLastName.getText().toString().trim();
            if (TextUtils.isEmpty(updatedLastName)) {
                etLastName.setError("Enter Last Name");
                etLastName.requestFocus();
                canSave = false;
                return;
            } else {
                if(!Utility.isAlphabetic(updatedLastName)){
                    etLastName.setError("Last name must be alphabetic");
                    etLastName.requestFocus();
                    canSave = false;
                    return;
                }else{
                    selectedStudent.setLastName(updatedLastName);
                    selectedParent.setLastName(updatedLastName);
                }
            }
        }

        if (isMobileNumberEdited) {
            String updatedMobileNumber = etMobileNumber.getText().toString().trim();
            if (TextUtils.isEmpty(updatedMobileNumber) || updatedMobileNumber.length()>10 ||updatedMobileNumber.length()<10) {
                etMobileNumber.setError("Enter 10 digit's mobile number");
                etMobileNumber.requestFocus();
                canSave = false;
                return;
            }else{
                if (!Utility.isValidPhone(updatedMobileNumber)) {
                    etMobileNumber.setError("Enter valid mobile number");
                    etMobileNumber.requestFocus();
                    canSave = false;
                    return;
                }else{
                    selectedParent.setMobileNumber(updatedMobileNumber);
                }
            }
        }

        if (isEmailIdEdited) {
            String updatedEmailId = etEmailId.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedEmailId)) {
                if (!Utility.isEmailValid(updatedEmailId)) {
                    etEmailId.setError("Enter valid email id");
                    etEmailId.requestFocus();
                    canSave = false;
                    return;
                }else{
                    selectedStudent.setEmailId(updatedEmailId);
                }
            }
        }

        if (isFatherFirstNameEdited) {
            String updatedFatherFirstName = etFatherFirstName.getText().toString().trim();
            if (TextUtils.isEmpty(updatedFatherFirstName)) {
                etFatherFirstName.setError("Enter father's first name");
                etFatherFirstName.requestFocus();
                canSave = false;
                return;
            } else {
                if(!Utility.isAlphabetic(updatedFatherFirstName)){
                    etFatherFirstName.setError("Father's first name must be alphabetic");
                    etFatherFirstName.requestFocus();
                    canSave = false;
                    return;
                }else{
                    selectedParent.setFirstName(updatedFatherFirstName);
                    selectedStudent.setMiddleName(updatedFatherFirstName);
                }
            }
        }

        if (isFatherMobileNumberEdited) {
            String updatedFatherMobileNumber= etFatherMobileNumber.getText().toString().trim();
            if (TextUtils.isEmpty(updatedFatherMobileNumber) || updatedFatherMobileNumber.length()>10 ||updatedFatherMobileNumber.length()<10) {
                etFatherMobileNumber.setError("Enter 10 digit's father's mobile number");
                etFatherMobileNumber.requestFocus();
                canSave = false;
                return;
            }else{
                if (!Utility.isValidPhone(updatedFatherMobileNumber)) {
                    etFatherMobileNumber.setError("Enter valid father's mobile number");
                    etFatherMobileNumber.requestFocus();
                    canSave = false;
                    return;
                }else {
                    selectedParent.setMobileNumber(updatedFatherMobileNumber);
                }
            }
        }

        if (isFatherMiddleNameEdited) {
            String updatedFatherMiddleName = etFatherMiddleName.getText().toString().trim();
            if(!TextUtils.isEmpty(updatedFatherMiddleName)) {
                if (!Utility.isAlphabetic(updatedFatherMiddleName)) {
                    etFatherMiddleName.setError("Father's middle name must be alphabetic");
                    etFatherMiddleName.requestFocus();
                    canSave = false;
                    return;
                } else {
                    selectedParent.setMiddleName(updatedFatherMiddleName);
                }
            }
        }


        if (isAddressEdited) {
            String updatedAddress = etAddress.getText().toString().trim();
            if (TextUtils.isEmpty(updatedAddress)) {
                etAddress.setError("Enter address");
                etAddress.requestFocus();
                canSave = false;
                return;
            }else{
                if(Utility.isNumericWithSpace(updatedAddress)){
                    etAddress.setError("Enter valid address");
                    etAddress.requestFocus();
                    canSave = false;
                    return;
                } else {
                    selectedParent.setAddress(updatedAddress);
                }
            }
        }

        if (canSave) {
            selectedStudent.setModifiedDate(new Date());
            selectedParent.setModifiedDate(new Date());
            if(selectedStudent.getStatus().equals(status)){

            }
            else {
                selectedStudent.setStatus(status);
                selectedParent.setStatus(status);
                isParentEdited = true;
            }
            etFirstName.setEnabled(false);
            etLastName.setEnabled(false);
           // etMiddleName.setEnabled(false);
            btUpdate.setVisibility(View.GONE);
            //Update

            if(pDialog!=null && !pDialog.isShowing()) {
                pDialog.show();
            }
            studentCollectionRef.document(selectedStudent.getId()).set(selectedStudent).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        if(isParentEdited){
                            updateParent();
                        }
                        else {
                            if(pDialog!=null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Success")
                                    .setContentText("Updated successfully")
                                    .setConfirmText("Ok")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                            fragmentTransaction.detach(currentFragment).attach(currentFragment).commit();

                                        }
                                    });
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                    } else {
                        if(pDialog!=null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }
                }
            });
        }
    }
    private class StudentParent{
        private Student student;
        private Parent parent;
    }

    private void updateParent(){

        if(isStatusSwitched){
            selectedParent.setStatus(status);
        }

        parentCollectionRef.document(selectedParent.getId()).set(selectedParent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(pDialog!=null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Success")
                        .setContentText("Updated successfully")
                        .setConfirmText("Ok")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                fragmentTransaction.detach(currentFragment).attach(currentFragment).commit();

                            }
                        });
                dialog.setCancelable(false);
                dialog.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(pDialog!=null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
            }
        });
    }
}
