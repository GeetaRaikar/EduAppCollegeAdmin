package com.padmajeet.techforedu.wowcollege.admin;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.pedant.SweetAlert.SweetAlertDialog;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.model.StaffType;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAddEditViewStaff extends Fragment {
    private LinearLayout llSelectStaff;
    private String firstName, middleName, lastName, designation, mobileNumber,address,emergencyContact,emailId,qualification;
    private float salaryPerMonth;
    private EditText etFirstName, etMiddleName, etLastName, etDesignation, etMobileNumber,etAddress,etEmergencyContact,etEmailId,etSalary,etQualification;
    private Button btnSave,btnUpdate;
    private ImageButton ibChoosePhoto;
    private SweetAlertDialog pDialog;
    private View view = null;
    private RecyclerView rvStaffType;
    private Spinner spTitle;
    private String selectedTitle;
    private RadioGroup radioGroupGender;
    private RadioButton radioBtnMale, radioBtnFemale;
    private String gender;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference= FirebaseStorage.getInstance().getReference("Profile");
    private CollectionReference staffCollectionRef = db.collection("Staff");
    private CollectionReference staffTypeCollectionRef = db.collection("StaffType");
    private ImageView ivProfilePic;
    private final int PICK_IMAGE_REQUEST=1;
    private Uri imageUri;
    private String imageUrl;
    private StorageTask mUploadTask;
    private String loggedInUserId,instituteId;
    private Gson gson;
    private ImageView ivDob,ivDateOfJoining;
    private EditText etDob,etDateOfJoining;
    private DatePickerDialog picker;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private Date dob,dateOfJoining;
    private List<StaffType> staffTypeList = new ArrayList<>();
    private String selectedStaffTypeId;
    private int selectedStaffTypePos = 0;
    private Staff selectedStaff = null;
    private Fragment currentFragment;
    private boolean isAddMode;
    private double progress;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        loggedInUserId = sessionManager.getString("loggedInUserId");
        instituteId=sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    public FragmentAddEditViewStaff() {
        // Required empty public constructor
        currentFragment = this;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_edit_view_staff, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.staff));
        llSelectStaff = view.findViewById(R.id.llSelectStaff);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etMiddleName = view.findViewById(R.id.etMiddleName);
        etDesignation = view.findViewById(R.id.etDesignation);
        etMobileNumber = view.findViewById(R.id.etMobileNumber);
        radioBtnFemale = view.findViewById(R.id.radioBtnFemale);
        radioBtnMale = view.findViewById(R.id.radioBtnMale);
        radioGroupGender = view.findViewById(R.id.radioGroupGender);
        ivProfilePic=view.findViewById(R.id.ivProfilePic);
        ibChoosePhoto =view.findViewById(R.id.ibChoosePhoto);
        ivDob =view.findViewById(R.id.ivDob);
        ivDateOfJoining =view.findViewById(R.id.ivDateOfJoining);
        etDob =view.findViewById(R.id.etDob);
        etDateOfJoining =view.findViewById(R.id.etDateOfJoining);
        etAddress =view.findViewById(R.id.etAddress);
        etEmailId =view.findViewById(R.id.etEmailId);
        etQualification =view.findViewById(R.id.etQualification);
        etEmergencyContact =view.findViewById(R.id.etEmergencyContact);
        etSalary =view.findViewById(R.id.etSalary);
        rvStaffType = view.findViewById(R.id.rvStaffType);
        rvStaffType.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnSave = view.findViewById(R.id.btnSave);

        if(getArguments()==null){//Add Mode
            getStaffType();
            llSelectStaff.setVisibility(View.VISIBLE);

            radioGroupGender.clearCheck();
            radioBtnFemale.setChecked(true);
            if (radioBtnFemale.isChecked()) {
                gender = "Female";
            }
            isAddMode = true;
            selectedStaff = new Staff();
            selectedStaff.setStatus("F");
            selectedStaff.setCreatorType("A");
            selectedStaff.setCreatorId(loggedInUserId);
            btnSave.setVisibility(View.VISIBLE);
            btnUpdate.setVisibility(View.GONE);
        }
        else{//Edit Mode
            String selectedStaffJson = getArguments().getString("selectedStaff");
            llSelectStaff.setVisibility(View.GONE);
            selectedStaff = gson.fromJson(selectedStaffJson,Staff.class);
            selectedStaffTypeId = selectedStaff.getStaffTypeId();
            isAddMode = false;
            setStaffData();
            btnSave.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
        }

        radioGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (radioBtnFemale.isChecked()) {
                    gender = "Female";
                } else {
                    if (radioBtnMale.isChecked()) {
                        gender = "Male";
                    }
                }
            }
        });

        ibChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        spTitle = view.findViewById(R.id.spTitle);
        getAllTitle();

        Date today=new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(today);

        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int month = calendar.get(Calendar.MONTH);
        final int year = calendar.get(Calendar.YEAR);
        ivDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //etDob.setEnabled(true);
                // date picker dialog
                picker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etDob.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());
                picker.setTitle("Select Date of Birth");
                picker.show();
            }
        });
        ivDateOfJoining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //etDateOfJoining.setEnabled(true);
                // date picker dialog
                picker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etDateOfJoining.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());
                picker.setTitle("Select Date of Joining");
                picker.show();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateForm();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });
        return view;
    }

    private void setStaffData(){
        imageUrl =selectedStaff.getImageUrl();
        System.out.println("setStaffData imageUrl "+imageUrl);
        if (imageUrl != null) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .fitCenter()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_faculty)
                    .into(ivProfilePic);
        }
        etFirstName.setText(""+selectedStaff.getFirstName());
        etMiddleName.setText(""+selectedStaff.getMiddleName());
        etLastName.setText(""+selectedStaff.getLastName());
        if(selectedStaff.getDateOfJoining()!=null) {
            String doj = dateFormat.format(selectedStaff.getDateOfJoining());
            etDateOfJoining.setText(""+doj);
        }
        etAddress.setText(""+selectedStaff.getAddress());
        etSalary.setText(""+selectedStaff.getSalPerMonth());
        etEmailId.setText(""+selectedStaff.getEmailId());
        etMobileNumber.setText(""+selectedStaff.getMobileNumber());
        String dob = dateFormat.format(selectedStaff.getDob());
        etDob.setText(""+dob);
        etEmergencyContact.setText(""+selectedStaff.getMobileNumber());
        if(!TextUtils.isEmpty(selectedStaff.getDesignation())){
            etDesignation.setText(""+selectedStaff.getDesignation());
        }
        if(!TextUtils.isEmpty(selectedStaff.getQualification())){
            etQualification.setText(""+selectedStaff.getQualification());
        }
        gender = selectedStaff.getGender();
        if(gender.equalsIgnoreCase("Male")){
            radioBtnMale.setChecked(true);
        }
        else{
            radioBtnFemale.setChecked(true);
        }
    }

    private void getStaffType(){
        if(pDialog!=null && !pDialog.isShowing()){
            pDialog.show();
        }
        staffTypeCollectionRef.whereEqualTo("instituteId",instituteId)
                .orderBy("type", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            StaffType staffType=documentSnapshot.toObject(StaffType.class);
                            staffType.setId(documentSnapshot.getId());
                            staffTypeList.add(staffType);
                        }
                        if(staffTypeList.size()>0){
                            StaffTypeAdaptor staffTypeAdaptor = new StaffTypeAdaptor();
                            rvStaffType.setAdapter(staffTypeAdaptor);
                            selectedStaffTypeId = staffTypeList.get(0).getId();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                    }
                });
    }

    public class StaffTypeAdaptor extends RecyclerView.Adapter<StaffTypeAdaptor.MyViewHolder>{
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvEmployeeTypeName;
            private ImageView ivEmployeeTypePic;
            private LinearLayout llImage;
            private View row;

            public MyViewHolder(View view) {
                super(view);
                row = view;
                tvEmployeeTypeName = view.findViewById(R.id.tvEmployeeTypeName);
                ivEmployeeTypePic = view.findViewById(R.id.ivEmployeeTypePic);
                llImage = view.findViewById(R.id.llImage);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_employee_type, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final StaffType staffType = staffTypeList.get(position);
            holder.tvEmployeeTypeName.setText(""+staffType.getType());
            Glide.with(getContext())
                    .load(R.drawable.ic_profile_large)
                    .fitCenter()
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.ivEmployeeTypePic);
            if(selectedStaffTypePos == position){
                holder.tvEmployeeTypeName.setTextColor(getResources().getColor(R.color.colorGreenDark));
                holder.llImage.setBackground(getResources().getDrawable(R.drawable.circle_green));
            }
            else{
                holder.tvEmployeeTypeName.setTextColor(getResources().getColor(R.color.colorBlack));
                holder.llImage.setBackground(null);
            }
            holder.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedStaffTypePos = position;
                    selectedStaffTypeId = staffType.getId();
                    System.out.println("selectedStaffTypeId - "+selectedStaffTypeId);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return staffTypeList.size();
        }
    }


    private void showFileChooser(){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"select an image"),PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                Glide.with(getContext())
                        .load(imageUri)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_faculty)
                        .into(ivProfilePic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void  uploadFile(){
        if(imageUri!=null) {
            final ProgressDialog progressDialog=new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final StorageReference fileRef = storageReference.child("Staff"+System.currentTimeMillis()+"."+getFileExtension(imageUri));

            mUploadTask=fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            /*
                            Handler handler=new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setProgress((int)progress);
                                }
                            },1000);

                             */
                            Toast.makeText(getContext(),"Uploaded..",Toast.LENGTH_LONG).show();
                            //imageUrl=taskSnapshot.getStorage().getDownloadUrl().toString();
                            // System.out.println("Image Url of profile Stored "+taskSnapshot.getStorage().getDownloadUrl().getResult().toString());
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // getting image uri and converting into string
                                    Uri downloadUrl = uri;
                                    imageUrl = downloadUrl.toString();
                                    if(pDialog == null && !pDialog.isShowing()){
                                        pDialog.show();
                                    }
                                    if(!TextUtils.isEmpty(selectedStaff.getImageUrl())){
                                        // Create a storage reference from our app
                                        StorageReference childRef = FirebaseStorage.getInstance().getReferenceFromUrl(selectedStaff.getImageUrl());
                                        // Delete the file
                                        childRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // File deleted successfully
                                                selectedStaff.setImageUrl(imageUrl);
                                                if(isAddMode){
                                                    addStaff();
                                                }
                                                else {
                                                    updateStaff();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // Uh-oh, an error occurred!
                                            }
                                        });

                                    }else{
                                        selectedStaff.setImageUrl(imageUrl);
                                        if(isAddMode){
                                            addStaff();
                                        }
                                        else {
                                            updateStaff();
                                        }
                                    }
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(),exception.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage(((int)progress)+"% Uploaded...");
                        }
                    });
        }else {
            Toast.makeText(getContext(),"No file selected",Toast.LENGTH_SHORT).show();
        }

    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContext().getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void validateForm(){

        firstName = etFirstName.getText().toString().trim();
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("Enter first name");
            etFirstName.requestFocus();
            return;
        }else{
            if(!Utility.isAlphabetic(firstName)){
                etFirstName.setError("First name must be alphabetic");
                etFirstName.requestFocus();
                return;
            }
        }
        lastName = etLastName.getText().toString().trim();
        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Enter last name");
            etLastName.requestFocus();
            return;
        }else{
            if(!Utility.isAlphabetic(lastName)){
                etLastName.setError("Last name must be alphabetic");
                etLastName.requestFocus();
                return;
            }
        }
        middleName = etMiddleName.getText().toString().trim();
        if (!TextUtils.isEmpty(middleName)) {
            if(!Utility.isAlphabetic(middleName)){
                etMiddleName.setError("Middle name must be alphabetic");
                etMiddleName.requestFocus();
                return;
            }
        }
        designation = etDesignation.getText().toString().trim();
        if (TextUtils.isEmpty(designation)) {
            etDesignation.setError("Enter designation");
            etDesignation.requestFocus();
            return;
        }

        mobileNumber = etMobileNumber.getText().toString().trim();
        if (TextUtils.isEmpty(mobileNumber)) {
            etMobileNumber.setError("Enter mobile number");
            etMobileNumber.requestFocus();
            return;
        }else{
            if(mobileNumber.length()>10 ||mobileNumber.length()<10) {
                etMobileNumber.setError("Enter 10 digit's mobile number");
                etMobileNumber.requestFocus();
                return;
            }
            if (!Utility.isValidPhone(mobileNumber)) {
                etMobileNumber.setError("Enter valid mobile number");
                etMobileNumber.requestFocus();
                return;
            }
        }
        String DOB = etDob.getText().toString().trim();
        if (TextUtils.isEmpty(DOB)) {
            etDob.setError("Enter Date Of Birth");
            etDob.requestFocus();
            return;
        }else{
            try {
                dob = dateFormat.parse(DOB);
            } catch (ParseException e) {
                etDob.setError("DD/MM/YYYY");
                etDob.requestFocus();
                e.printStackTrace();
                return;
            }
        }
        String dateOfJoin = etDateOfJoining.getText().toString().trim();
        if(!TextUtils.isEmpty(dateOfJoin)) {
            try {
                dateOfJoining = dateFormat.parse(dateOfJoin);
            } catch (ParseException e) {
                etDateOfJoining.setError("DD/MM/YYYY");
                etDateOfJoining.requestFocus();
                e.printStackTrace();
                return;
            }
        }
        emailId = etEmailId.getText().toString().trim();
        if(!TextUtils.isEmpty(emailId)){
            if(!Utility.isEmailValid(emailId)){
                etEmailId.setError("Enter valid Email Address");
                etEmailId.requestFocus();
                return;
            }
        }
        address = etAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Enter address");
            etAddress.requestFocus();
            return;
        }else{
            if(Utility.isNumericWithSpace(address)){
                etAddress.setError("Enter valid address");
                etAddress.requestFocus();
                return;
            }
        }
        emergencyContact = etEmergencyContact.getText().toString().trim();
        if (TextUtils.isEmpty(emergencyContact) || emergencyContact.length()>10 ||emergencyContact.length()<10) {
            etEmergencyContact.setError("Enter 10 digit's mobile number");
            etEmergencyContact.requestFocus();
            return;
        }else{
            if (!Utility.isValidPhone(emergencyContact)) {
                etEmergencyContact.setError("Enter valid mobile number");
                etEmergencyContact.requestFocus();
                return;
            }
        }
        String salary = etSalary.getText().toString().trim();
        if(!TextUtils.isEmpty(salary)) {
            try {
                salaryPerMonth = Float.parseFloat(salary);
            } catch (Exception e) {
                salaryPerMonth = 0f;
                e.printStackTrace();
            }
        }

        qualification = etQualification.getText().toString().trim();
        if(!TextUtils.isEmpty(qualification)){
            if(!Utility.isNumericWithSpace(qualification)){
                etQualification.setError("Enter valid qualification");
                etQualification.requestFocus();
                return;
            }
        }
        System.out.println("selectedStaffTypeId - "+selectedStaffTypeId);
        selectedStaff.setInstituteId(instituteId);
        selectedStaff.setFirstName(firstName);
        selectedStaff.setMiddleName(middleName);
        selectedStaff.setLastName(lastName);
        selectedStaff.setMobileNumber(mobileNumber);
        selectedStaff.setGender(gender);
        selectedStaff.setStaffTypeId(selectedStaffTypeId);
        selectedStaff.setEmailId(emailId);
        selectedStaff.setDob(dob);
        selectedStaff.setDateOfJoining(dateOfJoining);
        selectedStaff.setJoiningYear(dateOfJoining.getYear());
        selectedStaff.setAddress(address);
        selectedStaff.setEmergencyContact(emergencyContact);
        selectedStaff.setSalPerMonth(salaryPerMonth);
        selectedStaff.setTitle(selectedTitle);
        selectedStaff.setImageUrl(imageUrl);
        selectedStaff.setDob(dob);
        selectedStaff.setQualification(qualification);
        selectedStaff.setDesignation(designation);
        selectedStaff.setModifierId(loggedInUserId);
        selectedStaff.setModifierType("A");
        selectedStaff.setModifiedDate(new Date());
        if(imageUri == null){
            if(isAddMode){
                addStaff();
            }
            else {
                updateStaff();
            }
        }else{
            uploadFile();
        }
    }

    private void updateStaff(){
        if(pDialog!=null) {
            pDialog.show();
        }
        staffCollectionRef
                .document(selectedStaff.getId())
                .set(selectedStaff)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(pDialog!=null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Staff successfully updated")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        /*Toast.makeText(getContext(), "Student is successfully added", Toast.LENGTH_SHORT).show();
                                        Bundle bundle = new Bundle();
                                        String selectedStaffJson = gson.toJson(selectedStaff);
                                        bundle.putString("selectedStaff",selectedStaffJson);
                                        FragmentAddEditViewStaff fragmentEditStaff = new FragmentAddEditViewStaff();
                                        fragmentEditStaff.setArguments(bundle);
                                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                        fragmentTransaction.replace(R.id.contentLayout, fragmentEditStaff).commit();

                                         */
                                        //String selectedStaffJson = getArguments().getString("selectedStaff");
                                        //getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null) {
                            pDialog.dismiss();
                        }
                    }
                });
    }

    private void addStaff() {
        if(pDialog == null && !pDialog.isShowing()){
            pDialog.show();
        }
        staffCollectionRef
                .add(selectedStaff)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Staff successfully added")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        //Toast.makeText(getContext(), "Student is successfully added", Toast.LENGTH_SHORT).show();
                                        FragmentStaff fragmentStaff = new FragmentStaff();
                                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                        fragmentTransaction.replace(R.id.contentLayout, fragmentStaff).addToBackStack(null).commit();

                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();

                        //Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        //Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });
        // [END add_document]

    }

    private void getAllTitle() {
        // Spinner Drop down elements
        final List<String> titles = new ArrayList<String>();
        titles.add("Mrs.");
        titles.add("Miss.");
        titles.add("Mr.");
        titles.add("Ms.");
        titles.add("Dr.");

        ArrayAdapter<String> titlesAdaptor = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, titles);
        titlesAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTitle.setAdapter(titlesAdaptor);
        if(!isAddMode){
            String tittle = selectedStaff.getTitle();
            int pos = titles.lastIndexOf(tittle);
            spTitle.setSelection(pos);
        }
        spTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTitle = titles.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
