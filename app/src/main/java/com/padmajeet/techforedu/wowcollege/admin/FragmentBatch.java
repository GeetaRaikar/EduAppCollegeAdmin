package com.padmajeet.techforedu.wowcollege.admin;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;



/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentBatch extends Fragment {
    private String loggedInUserId;
    private View view = null;
    private LinearLayout llNoList;
    private ArrayList<Batch> batchList = new ArrayList<>();
    private List<String> alreadyBatchList = new ArrayList<>();
    private Batch batch;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    //private CollectionReference sectionCollectionRef = db.collection("Section");
    private String name, longName, sectionName;
    private EditText etBatchName, etBatchLongName;
    private ListenerRegistration batchListener;
    private RecyclerView rvBatch;
    private RecyclerView.Adapter batchAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Button btnSave;
    //private Section section;
    private Staff loggedInUser;
    private SweetAlertDialog pDialog;
    private String instituteId;
    private Gson gson;
    private ImageView ivProfilePic;
    private ImageButton ibChoosePhoto;
    private final int PICK_IMAGE_REQUEST=1;
    private Uri imageUri=null;
    private StorageReference storageReference;
    private String imageUrl;
    private StorageTask mUploadTask;

    @Override
    public void onStart() {
        super.onStart();
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        if (batchList.size() != 0) {
            batchList.clear();
        }
        if (alreadyBatchList.size() != 0) {
            alreadyBatchList.clear();
        }
        batchListener = batchCollectionRef
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
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            batch = document.toObject(Batch.class);
                            batch.setId(document.getId());
                            alreadyBatchList.add(batch.getName());
                            batchList.add(batch);
                        }
                        if (batchList.size() != 0) {
                            rvBatch.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            batchAdapter = new BatchAdapter(batchList);
                            rvBatch.setAdapter(batchAdapter);
                        } else {
                            rvBatch.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (batchListener != null) {
            batchListener.remove();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        loggedInUserId = sessionManager.getString("loggedInUserId");
        instituteId =sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
        storageReference= FirebaseStorage.getInstance().getReference("Images");
    }

    public FragmentBatch() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_batch, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.batch));
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        createBottomSheet();
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addBatch);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.show();
                //addDialogBatch();
            }
        });
        rvBatch = (RecyclerView) view.findViewById(R.id.rvBatch);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvBatch.setLayoutManager(layoutManager);
        return view;
    }

    BottomSheetDialog bottomSheetDialog;

    private void createBottomSheet() {
        if (bottomSheetDialog == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_batch, null);
            bottomSheetDialog = new BottomSheetDialog(getContext());//new BottomSheetDialog(this,R.style.BottomSheetDialog)
            bottomSheetDialog.setContentView(view);
            etBatchName = view.findViewById(R.id.etBatchName);
            etBatchLongName = view.findViewById(R.id.etBatchLongName);
            btnSave = view.findViewById(R.id.btnSave);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    name = etBatchName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        etBatchName.setError("Enter batch name");
                        etBatchName.requestFocus();
                        return;
                    } else {
                        if (Utility.isNumericWithSpace(name)) {
                            etBatchName.setError("Invalid batch name");
                            etBatchName.requestFocus();
                            return;
                        }else {
                            //TODO
                            String Name = name.replaceAll("\\s+", "");
                            System.out.println("Name " + Name);
                            for (int i = 0; i < alreadyBatchList.size(); i++) {
                                String batchName = alreadyBatchList.get(i).replaceAll("\\s+", "");
                                if (Name.toUpperCase() == batchName.toUpperCase()) {
                                    System.out.println("equal");
                                    etBatchName.setError("Already this batch name is saved ");
                                    etBatchName.requestFocus();
                                    return;
                                }
                            }
                        }
                    }
                    longName = etBatchLongName.getText().toString().trim();
                    if (TextUtils.isEmpty(longName)) {
                        etBatchLongName.setError("Enter long name");
                        etBatchLongName.requestFocus();
                        return;
                    }else{
                        if(Utility.isNumericWithSpace(longName)){
                            etBatchLongName.setError("Invalid long name");
                            etBatchLongName.requestFocus();
                            return;
                        }
                    }
                    if (pDialog == null && !pDialog.isShowing()) {
                        pDialog.show();
                    }
                    batch = new Batch();
                    batch.setInstituteId(instituteId);
                    batch.setName(name);
                    batch.setLongName(longName);
                    batch.setStatus("A");
                    batch.setCreatorId(loggedInUserId);
                    batch.setModifierId(loggedInUserId);
                    batch.setCreatorType("A");
                    batch.setModifierType("A");
                    batch.setImageUrl(null);
                    if(imageUri==null){
                        addBatch(batch);
                    }else{
                        uploadFile();
                    }
                }
            });
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
                if(mUploadTask !=null && mUploadTask.isInProgress()){
                    Toast.makeText(getContext(),"Upload in process...",Toast.LENGTH_SHORT).show();
                }
                Glide.with(getContext())
                        .load(imageUri.toString())
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_menu_batch)
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
            final StorageReference fileRef = storageReference.child("Class"+System.currentTimeMillis()+"."+getFileExtension(imageUri));

            mUploadTask=fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            Handler handler=new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setProgress(0);
                                }
                            },5000);
                            Toast.makeText(getContext(),"Uploaded..",Toast.LENGTH_LONG).show();
                            //imageUrl=taskSnapshot.getStorage().getDownloadUrl().toString();
                            // System.out.println("Image Url of profile Stored "+taskSnapshot.getStorage().getDownloadUrl().getResult().toString());
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // getting image uri and converting into string
                                    Uri downloadUrl = uri;
                                    imageUrl = downloadUrl.toString();
                                    //System.out.println("ImageURL -"+imageUrl);
                                    //batch.setImageUrl(imageUrl);
                                    addBatch(batch);
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
                            double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
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
    class BatchAdapter extends RecyclerView.Adapter<BatchAdapter.MyViewHolder> {
        private List<Batch> batchList;
        String batchName;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView BatchName, tvLongName;
            public ImageView ivEditBatchName, ivProfilePic;

            public MyViewHolder(View view) {
                super(view);
                BatchName = view.findViewById(R.id.tvBatchName);
                ivEditBatchName = (ImageView) view.findViewById(R.id.ivEditBatch);
                tvLongName = view.findViewById(R.id.tvLongName);
            }
        }


        public BatchAdapter(List<Batch> batchList) {
            this.batchList = batchList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_batch, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final Batch batch = batchList.get(position);
            holder.BatchName.setText("" + batch.getName());
            if(TextUtils.isEmpty(batch.getLongName()) || batch.getLongName()==null){
                holder.tvLongName.setVisibility(View.GONE);
            }
            holder.tvLongName.setText("" + batch.getLongName());
            /*
            holder.llImage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        holder.llImage.setVisibility(View.GONE);
                        holder.tvTypeLong.setVisibility(View.VISIBLE);
                    }
                    else{
                        holder.llImage.setVisibility(View.VISIBLE);
                        holder.tvTypeLong.setVisibility(View.GONE);
                    }
                }
            });
            holder.llImage.setOnHoverListener(new View.OnHoverListener() {
                @Override
                public boolean onHover(View v, MotionEvent event) {
                    System.out.println("onHover");
                    int what = event.getAction();
                    switch(what) {
                        case MotionEvent.ACTION_HOVER_ENTER:

                            holder.llImage.setVisibility(View.GONE);
                            holder.tvTypeLong.setVisibility(View.VISIBLE);
                            break;
                        case MotionEvent.ACTION_HOVER_EXIT:
                            holder.llImage.setVisibility(View.VISIBLE);
                            holder.tvTypeLong.setVisibility(View.GONE);

                    }
                    return false;
                }
            });

             */
            //String url = "" + batch.getImageUrl();
            //System.out.println("Image path" + url);
            /*
            if (url != null) {
                Glide.with(getContext())
                        .load(url)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.standard)
                        .into(holder.ivProfilePic);
            }
            */
            holder.ivEditBatchName.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialogLayout = inflater.inflate(R.layout.dialog_edit_batch, null);
                    final EditText etName = dialogLayout.findViewById(R.id.etBatchName);
                    etName.setText("" + batch.getName());
                    final EditText etLongName = dialogLayout.findViewById(R.id.etBatchLongName);
                    if(!TextUtils.isEmpty(batch.getLongName()) || batch.getLongName()!=null){
                        etLongName.setText("" + batch.getLongName());
                    }
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit Batch")
                            .setConfirmText("Update")
                            .setCustomView(dialogLayout)
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    batchName = etName.getText().toString().trim();
                                    if (TextUtils.isEmpty(batchName)) {
                                        etName.setError("Enter batch name");
                                        etName.requestFocus();
                                        return;
                                    }else {
                                        if(Utility.isNumericWithSpace(batchName)){
                                            etName.setError("Invalid batch name");
                                            etName.requestFocus();
                                            return;
                                        }else {
                                            String Name = batch.getName();
                                            Name = Name.replaceAll("\\s+", "");
                                            System.out.println("Name " + Name);
                                            String EditName = Name.replaceAll("\\s+", "");
                                            System.out.println("EditName " + EditName);
                                            if (!Name.equalsIgnoreCase(EditName)) {
                                                for (int i = 0; i < alreadyBatchList.size(); i++) {
                                                    String batch_name = alreadyBatchList.get(i).replaceAll("\\s+", "");
                                                    System.out.println("batch_name " + batch_name);
                                                    if (EditName.equalsIgnoreCase(batch_name)) {
                                                        System.out.print("flag ");
                                                        etName.setError("Already this batch name is saved");
                                                        etName.requestFocus();
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    String batchLongName = etLongName.getText().toString().trim();
                                    if (!TextUtils.isEmpty(batchLongName)) {
                                        if(Utility.isNumericWithSpace(batchLongName)){
                                            etLongName.setError("Invalid long name");
                                            etLongName.requestFocus();
                                            return;
                                        }
                                    }
                                    batch.setName(batchName);
                                    batch.setLongName(batchLongName);
                                    batch.setModifiedDate(new Date());
                                    batch.setModifierId(loggedInUserId);
                                    batch.setModifierType("A");
                                    sDialog.dismissWithAnimation();
                                    if(pDialog==null && !pDialog.isShowing()) {
                                        pDialog.show();
                                    }
                                    batchCollectionRef.document(batch.getId()).set(batch).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                if (pDialog != null) {
                                                    pDialog.dismiss();
                                                }
                                            } else {
                                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                }
                            });
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return batchList.size();
        }
    }

    private void addBatch(Batch batch) {
        batchCollectionRef
                .add(batch)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        /*
                        section = new Section();
                        section.setName(sectionName);
                        section.setBatchId(documentReference.getId());
                        section.setStatus("A");
                        section.setCreatorId(loggedInUserId);
                        section.setModifierId(loggedInUserId);
                        section.setCreatorType("A");
                        section.setModifierType("A");
                        addSection();
                        */
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Batch successfully added")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        //getBatchAge();
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void addSection() {
        /*
        sectionCollectionRef
                .add(section)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        bottomSheetDialog.dismiss();
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Class successfully added")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        //getBatchAge();
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
            }
        });
        */

    }
}

