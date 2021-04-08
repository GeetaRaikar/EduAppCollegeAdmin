package com.padmajeet.techforedu.wowcollege.admin;


import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.eduapp.wowcollege.admin.model.Staff;
import com.padmajeet.eduapp.wowcollege.admin.model.StaffType;
import com.padmajeet.eduapp.wowcollege.admin.util.SessionManager;
import com.padmajeet.eduapp.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentStaff extends Fragment {

    private Gson gson;
    private String loggedInUserId,academicYearId,instituteId;
    private Staff loggedInUser;
    private SweetAlertDialog pDialog;
    private RecyclerView rvStaffType, rvStaff;
    private LinearLayout llNoList;
    private FloatingActionButton fabAddStaff;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference staffTypeCollectionRef = db.collection("StaffType");
    private CollectionReference staffCollectionRef = db.collection("Staff");

    private List<StaffType> staffTypeList = new ArrayList<>();
    private List<Staff> staffList = new ArrayList<>();
    private String selectedStaffTypeId;
    private int selectedStaffTypePos = 0;

    public FragmentStaff() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson=sessionManager.getString("loggedInUser");
        loggedInUser=gson.fromJson(userJson, Staff.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
        academicYearId= sessionManager.getString("AcademicYearId");
        instituteId=sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_staff, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvStaff = view.findViewById(R.id.rvStaff);
        rvStaffType = view.findViewById(R.id.rvStaffType);

        rvStaffType.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //rvStaff.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        llNoList = view.findViewById(R.id.llNoList);
        fabAddStaff = view.findViewById(R.id.fabAddStaff);

        fabAddStaff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentAddEditViewStaff fragmentAddEditViewStaff = new FragmentAddEditViewStaff();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.contentLayout, fragmentAddEditViewStaff).addToBackStack(null).commit();
            }
        });

        getStaffType();
    }

    private void getStaffType(){
        staffTypeCollectionRef.whereEqualTo("instituteId",instituteId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            StaffType staffType=documentSnapshot.toObject(StaffType.class);
                            staffType.setId(documentSnapshot.getId());
                            // System.out.println("Event EventName-"+event.getName());
                            staffTypeList.add(staffType);
                        }
                        if(staffTypeList.size()>0){
                            StaffTypeAdaptor staffTypeAdaptor = new StaffTypeAdaptor();
                            rvStaffType.setAdapter(staffTypeAdaptor);
                            selectedStaffTypeId = staffTypeList.get(0).getId();
                            getStaff();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void getStaff(){
        if(pDialog!=null){
            pDialog.show();
        }
        System.out.println("selectedStaffTypeId- "+selectedStaffTypeId);
        staffList.clear();
        staffCollectionRef
                .whereEqualTo("staffTypeId",selectedStaffTypeId)
                .whereEqualTo("instituteId",instituteId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            Staff staff=documentSnapshot.toObject(Staff.class);
                            staff.setId(documentSnapshot.getId());
                            // System.out.println("Event EventName-"+event.getName());
                            staffList.add(staff);
                        }
                        System.out.println("onSuccess- "+staffList.size());
                        if(staffList.size()!=0){
                            StaffAdaptor staffAdaptor = new StaffAdaptor();
                            rvStaff.setAdapter(staffAdaptor);
                            llNoList.setVisibility(View.GONE);
                            rvStaff.setVisibility(View.VISIBLE);
                        }
                        else{
                            rvStaff.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

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
                    getStaff();
                    notifyDataSetChanged();

                }
            });
        }

        @Override
        public int getItemCount() {
            return staffTypeList.size();
        }
    }

    public class StaffAdaptor extends RecyclerView.Adapter<StaffAdaptor.MyViewHolder>{
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName,tvMobileNumber;
            public ImageView ivEditStaff,ivProfilePic;
            private View row;

            public MyViewHolder(View view) {
                super(view);
                row = view;
                tvName = (TextView) view.findViewById(R.id.tvName);
                tvMobileNumber = (TextView) view.findViewById(R.id.tvMobileNumber);
                ivEditStaff = view.findViewById(R.id.ivEditStaff);
                ivProfilePic = view.findViewById(R.id.ivProfilePic);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_staff, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Staff staff = staffList.get(position);
            StringBuffer name = new StringBuffer();
            if(!staff.getTitle().isEmpty()){
                name.append(staff.getTitle()+" ");
            }
            if(!staff.getFirstName().isEmpty()){
                name.append(staff.getFirstName()+" ");
            }
            if (!(staff.getMiddleName().isEmpty())) {
                name.append(staff.getMiddleName()+" ");
            }
            if (!(staff.getLastName().isEmpty())) {
                name.append(staff.getLastName());
            }

            holder.tvName.setText("" + name.toString());

            holder.tvMobileNumber.setText("" + staff.getMobileNumber());
            String imageUrl =staff.getImageUrl();
            //System.out.println("imageUrl "+imageUrl);
            if (imageUrl != null) {
                Glide.with(getContext())
                        .load(imageUrl)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_faculty)
                        .into(holder.ivProfilePic);
            }

            holder.tvMobileNumber.setTextColor(getResources().getColor(R.color.colorLightBlue));
            holder.tvMobileNumber.setPaintFlags(holder.tvMobileNumber.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.tvMobileNumber.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    String mobileNumber = "tel:" + staff.getMobileNumber();
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(mobileNumber));
                    startActivity(intent);
                }
            });
            holder.ivEditStaff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    String selectedStaffJson = gson.toJson(staff);
                    bundle.putString("selectedStaff",selectedStaffJson);
                    FragmentAddEditViewStaff fragmentEditStaff = new FragmentAddEditViewStaff();
                    fragmentEditStaff.setArguments(bundle);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.contentLayout, fragmentEditStaff).addToBackStack(null).commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return staffList.size();
        }


    }
}
