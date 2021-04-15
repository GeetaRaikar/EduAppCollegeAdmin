package com.padmajeet.techforedu.wowcollege.admin;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Institute;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;


import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentInstitute extends Fragment {
    private Bundle bundle = new Bundle();
    private Gson gson;
    private SweetAlertDialog pDialog;
    private View view = null;
    private String loggedInUserId, academicYearId, InstituteId;
    private EditText etInstituteName, etShortName, etMission, etVision, etAbout, etAddress, etPrimaryContactNo, etSecondaryContactNo;
    private EditText etEmailId, etEstablishmentYear, etCurrency, etCurrencySymbol;
    private ImageView ivEditInstituteName, ivEditShortName, ivEditMission, ivEditVision, ivEditAbout, ivEditAddress, ivEditPrimaryContactNo, ivEditSecondaryContactNo;
    private ImageView ivEditEmailId, ivEditEstablishmentYear, ivEditCurrency, ivEditCurrencySymbol, ivInstituteLogo, ivEditInstituteLogo;
    private Boolean isInstituteName = false, isShortName = false, isMission = false, isVision = false;
    private Boolean isAbout = false, isAddress = false, isPrimaryContactNo = false, isSecondaryContactNo = false;
    private Boolean isEmailId = false, isEstablishmentYear = false, isCurrency = false, isCurrencySymbol = false;
    private ImageButton ibChoosePhoto;
    private Button btUpdate;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference instituteCollectionRef = db.collection("Institute");
    private Fragment currentFragment;
    private Institute institute;
    private final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri = null;
    private StorageReference storageReference;
    private String imageUrl;
    private StorageTask mUploadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        loggedInUserId = sessionManager.getString("loggedInUserId");
        academicYearId = sessionManager.getString("academicYearId");
        InstituteId = sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
        storageReference = FirebaseStorage.getInstance().getReference("Logo");
    }

    public FragmentInstitute() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_institute, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.institute));
        currentFragment = this;
        btUpdate = (Button) view.findViewById(R.id.btUpdate);
        ivInstituteLogo = (ImageView) view.findViewById(R.id.ivSchoolLogo);
        ivEditInstituteLogo = (ImageView) view.findViewById(R.id.ivEditSchoolLogo);
        ibChoosePhoto = (ImageButton) view.findViewById(R.id.ibChoosePhoto);
        etInstituteName = (EditText) view.findViewById(R.id.etSchoolName);
        etShortName = (EditText) view.findViewById(R.id.etShortName);
        etMission = (EditText) view.findViewById(R.id.etMission);
        etVision = (EditText) view.findViewById(R.id.etVision);
        etAbout = (EditText) view.findViewById(R.id.etAbout);
        etAddress = (EditText) view.findViewById(R.id.etAddress);
        etPrimaryContactNo = (EditText) view.findViewById(R.id.etPrimaryContactNo);
        etSecondaryContactNo = (EditText) view.findViewById(R.id.etSecondaryContactNo);
        etEmailId = (EditText) view.findViewById(R.id.etEmailId);
        etEstablishmentYear = (EditText) view.findViewById(R.id.etEstablishmentYear);
        etCurrency = (EditText) view.findViewById(R.id.etCurrency);
        etCurrencySymbol = (EditText) view.findViewById(R.id.etCurrencySymbol);
        ivEditInstituteName = (ImageView) view.findViewById(R.id.ivEditSchoolName);
        ivEditShortName = (ImageView) view.findViewById(R.id.ivEditShortName);
        ivEditMission = (ImageView) view.findViewById(R.id.ivEditMission);
        ivEditVision = (ImageView) view.findViewById(R.id.ivEditVision);
        ivEditAbout = (ImageView) view.findViewById(R.id.ivEditAbout);
        ivEditAddress = (ImageView) view.findViewById(R.id.ivEditAddress);
        ivEditPrimaryContactNo = (ImageView) view.findViewById(R.id.ivEditPrimaryContactNo);
        ivEditSecondaryContactNo = (ImageView) view.findViewById(R.id.ivEditSecondaryContactNo);
        ivEditEmailId = (ImageView) view.findViewById(R.id.ivEditEmailId);
        ivEditEstablishmentYear = (ImageView) view.findViewById(R.id.ivEditEstablishmentYear);
        ivEditCurrency = (ImageView) view.findViewById(R.id.ivEditCurrency);
        ivEditCurrencySymbol = (ImageView) view.findViewById(R.id.ivEditCurrencySymbol);


        btUpdate.setVisibility(View.INVISIBLE);
        if (!pDialog.isShowing() && pDialog == null) {
            pDialog.show();
        }
        instituteCollectionRef.document(InstituteId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (pDialog.isShowing() && pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (task.isSuccessful()) {
                            institute = task.getResult().toObject(Institute.class);
                            setData();
                        } else {

                        }
                    }
                });

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateInstitute();
            }
        });
        return view;
    }

    private void setData() {
        String unAvailable = getString(R.string.unavailable);

        etInstituteName.setText(institute.getName());

        if (TextUtils.isEmpty(institute.getShortName())) {
            etShortName.setHint(unAvailable);
        } else {
            etShortName.setText(institute.getShortName());
        }

        if (TextUtils.isEmpty(institute.getMission())) {
            etMission.setHint(unAvailable);
        } else {
            etMission.setText(institute.getMission());
        }

        if (TextUtils.isEmpty(institute.getVision())) {
            etVision.setHint(unAvailable);
        } else {
            etVision.setText(institute.getVision());
        }

        if (TextUtils.isEmpty(institute.getAboutInstitute())) {
            etAbout.setHint(unAvailable);
        } else {
            etAbout.setText(institute.getAboutInstitute());
        }

        if (TextUtils.isEmpty(institute.getAddress())) {
            etAddress.setHint(unAvailable);
        } else {
            etAddress.setText(institute.getAddress());
        }

        if (TextUtils.isEmpty(institute.getPrimaryContactNumber())) {
            etPrimaryContactNo.setHint(unAvailable);
        } else {
            etPrimaryContactNo.setText(institute.getPrimaryContactNumber());
        }

        if (TextUtils.isEmpty(institute.getSecondaryContactNumber())) {
            etSecondaryContactNo.setHint(unAvailable);
        } else {
            etSecondaryContactNo.setText(institute.getSecondaryContactNumber());
        }

        if (TextUtils.isEmpty(institute.getEmailId())) {
            etEmailId.setHint(unAvailable);
        } else {
            etEmailId.setText(institute.getEmailId());
        }

        if (TextUtils.isEmpty("" + institute.getYearOfEstablishment())) {
            etEstablishmentYear.setHint(unAvailable);
        } else {
            etEstablishmentYear.setText("" + institute.getYearOfEstablishment());
        }

        if (TextUtils.isEmpty(institute.getCurrency())) {
            etCurrency.setHint(unAvailable);
        } else {
            etCurrency.setText(institute.getCurrency());
        }

        if (TextUtils.isEmpty(institute.getCurrencySymbol())) {
            etCurrencySymbol.setHint(unAvailable);
        } else {
            etCurrencySymbol.setText(institute.getCurrencySymbol());
        }

        ivEditInstituteName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInstituteName = true;
                etInstituteName.setEnabled(true);
                btUpdate.setVisibility(View.VISIBLE);
            }
        });

        ivEditShortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isShortName = true;
                btUpdate.setVisibility(View.VISIBLE);
                etShortName.setEnabled(true);
                etShortName.requestFocus();
            }
        });

        ivEditMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMission = true;
                btUpdate.setVisibility(View.VISIBLE);
                etMission.setEnabled(true);
                etMission.requestFocus();
            }
        });

        ivEditVision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVision = true;
                btUpdate.setVisibility(View.VISIBLE);
                etVision.setEnabled(true);
                etVision.requestFocus();
            }
        });

        ivEditAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAbout = true;
                btUpdate.setVisibility(View.VISIBLE);
                etAbout.setEnabled(true);
                etAbout.requestFocus();
            }
        });

        ivEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAddress = true;
                btUpdate.setVisibility(View.VISIBLE);
                etAddress.setEnabled(true);
                etAddress.requestFocus();
            }
        });

        ivEditPrimaryContactNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPrimaryContactNo = true;
                btUpdate.setVisibility(View.VISIBLE);
                etPrimaryContactNo.setEnabled(true);
                etPrimaryContactNo.requestFocus();
            }
        });

        ivEditSecondaryContactNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSecondaryContactNo = true;
                btUpdate.setVisibility(View.VISIBLE);
                etSecondaryContactNo.setEnabled(true);
                etSecondaryContactNo.requestFocus();
            }
        });

        ivEditEmailId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEmailId = true;
                btUpdate.setVisibility(View.VISIBLE);
                etEmailId.setEnabled(true);
                etEmailId.requestFocus();
            }
        });

        ivEditEstablishmentYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEstablishmentYear = true;
                btUpdate.setVisibility(View.VISIBLE);
                etEstablishmentYear.setEnabled(true);
                etEstablishmentYear.requestFocus();
            }
        });

        ivEditCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCurrency = true;
                btUpdate.setVisibility(View.VISIBLE);
                etCurrency.setEnabled(true);
                etCurrency.requestFocus();
            }
        });

        ivEditCurrencySymbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCurrencySymbol = true;
                btUpdate.setVisibility(View.VISIBLE);
                etCurrencySymbol.setEnabled(true);
                etCurrencySymbol.requestFocus();
            }
        });
        ibChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
                btUpdate.setVisibility(View.VISIBLE);
            }
        });
        if (!TextUtils.isEmpty(institute.getLogoImagePath())) {
            Glide.with(getContext())
                    .load(institute.getLogoImagePath())
                    .fitCenter()
                    .placeholder(R.drawable.ic_school)
                    .into(ivInstituteLogo);
        }
    }

    void updateInstitute() {
        boolean canSave = true;

        if (isInstituteName) {
            String updatedName = etInstituteName.getText().toString().trim();
            if (TextUtils.isEmpty(updatedName)) {
                etInstituteName.setError("Enter Institute Name");
                etInstituteName.requestFocus();
                canSave = false;
                return;
            } else {
                if (Utility.isNumericWithSpace(updatedName)) {
                    etInstituteName.setError("Institute name must be alphabetic");
                    etInstituteName.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setName(updatedName);
                }
            }
        }

        if (isShortName) {
            String updatedShortName = etShortName.getText().toString().trim();
            if (TextUtils.isEmpty(updatedShortName)) {
                etShortName.setError("Enter Short Name");
                etShortName.requestFocus();
                canSave = false;
                return;
            } else {
                if (Utility.isNumericWithSpace(updatedShortName)) {
                    etShortName.setError("Short name must be alphabetic");
                    etShortName.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setShortName(updatedShortName);
                }
            }
        }

        if (isMission) {
            String updatedMission = etMission.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedMission)) {
                if (Utility.isNumericWithSpace(updatedMission)) {
                    etMission.setError("Invalid mission");
                    etMission.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setMission(updatedMission);
                }
            }
        }

        if (isVision) {
            String updatedVision = etVision.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedVision)) {
                if (Utility.isNumericWithSpace(updatedVision)) {
                    etVision.setError("Invalid vision");
                    etVision.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setVision(updatedVision);
                }
            }
        }

        if (isAbout) {
            String updatedAbout = etAbout.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedAbout)) {
                if (Utility.isNumericWithSpace(updatedAbout)) {
                    etAbout.setError("Invalid about institute");
                    etAbout.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setVision(updatedAbout);
                }
            }
        }

        if (isAddress) {
            String updatedAddress = etAddress.getText().toString().trim();
            if (TextUtils.isEmpty(updatedAddress)) {
                etAddress.setError("Enter address");
                etAddress.requestFocus();
                canSave = false;
                return;
            } else {
                if (Utility.isNumericWithSpace(updatedAddress)) {
                    etAddress.setError("Invalid address");
                    etAddress.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setAddress(updatedAddress);
                }
            }
        }
        if (isPrimaryContactNo) {
            String updatedPrimaryContactNo = etPrimaryContactNo.getText().toString().trim();
            if (TextUtils.isEmpty(updatedPrimaryContactNo) || updatedPrimaryContactNo.length() > 10 || updatedPrimaryContactNo.length() < 10) {
                etPrimaryContactNo.setError("Enter 10 digit's primary contact number");
                etPrimaryContactNo.requestFocus();
                canSave = false;
                return;
            } else {
                if (!Utility.isValidPhone(updatedPrimaryContactNo)) {
                    etPrimaryContactNo.setError("Enter valid primary contact number");
                    etPrimaryContactNo.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setPrimaryContactNumber(updatedPrimaryContactNo);
                }
            }
        }
        if (isSecondaryContactNo) {
            String updatedSecondaryContactNo = etSecondaryContactNo.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedSecondaryContactNo) || updatedSecondaryContactNo.length() > 10 || updatedSecondaryContactNo.length() < 10) {
                if (!Utility.isValidPhone(updatedSecondaryContactNo)) {
                    etSecondaryContactNo.setError("Enter valid secondary contact Number");
                    etSecondaryContactNo.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setSecondaryContactNumber(updatedSecondaryContactNo);
                }
            }
        }

        if (isEmailId) {
            String updatedEmailId = etEmailId.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedEmailId)) {
                if (!Utility.isEmailValid(updatedEmailId)) {
                    etEmailId.setError("Enter valid Email Address");
                    etEmailId.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setEmailId(updatedEmailId);
                }
            }
        }

        if (isEstablishmentYear) {
            String updatedEstablishmentYear = etEstablishmentYear.getText().toString().trim();
            if (TextUtils.isEmpty(updatedEstablishmentYear)) {
                etAddress.setError("Enter establishment year");
                etAddress.requestFocus();
                canSave = false;
                return;
            } else {
                if (Utility.isYear(updatedEstablishmentYear)) {
                    etAddress.setError("Enter valid establishment year");
                    etAddress.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setYearOfEstablishment(Integer.parseInt(updatedEstablishmentYear));
                }
            }
        }

        if (isCurrency) {
            String updatedCurrency = etCurrency.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedCurrency)) {
                if (!Utility.isAlphanumeric(updatedCurrency)) {
                    etCurrency.setError("Invalid currency");
                    etCurrency.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setCurrency(updatedCurrency);
                }
            }
        }

        if (isCurrencySymbol) {
            String updatedCurrencySymbol = etCurrencySymbol.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedCurrencySymbol)) {
                if (!Utility.isAlphanumeric(updatedCurrencySymbol)) {
                    etCurrencySymbol.setError("Invalid currency symbol");
                    etCurrencySymbol.requestFocus();
                    canSave = false;
                    return;
                } else {
                    institute.setCurrencySymbol(updatedCurrencySymbol);
                }
            }
        }
        if (canSave) {
            etAddress.setEnabled(false);
            etAbout.setEnabled(false);
            etCurrencySymbol.setEnabled(false);
            etCurrency.setEnabled(false);
            etEstablishmentYear.setEnabled(false);
            etEmailId.setEnabled(false);
            etSecondaryContactNo.setEnabled(false);
            etPrimaryContactNo.setEnabled(false);
            etVision.setEnabled(false);
            etMission.setEnabled(false);
            etShortName.setEnabled(false);
            etInstituteName.setEnabled(false);

            // etMiddleName.setEnabled(false);
            btUpdate.setVisibility(View.GONE);
            //Update

            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            if (imageUri != null) {
                uploadFile();
            } else {
                updateInstituteToDatabase();
            }
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select an image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(getContext(), "Upload in process...", Toast.LENGTH_SHORT).show();
                } else {
                    if (imageUri != null) {
                        Glide.with(getContext())
                                .load(imageUri)
                                .fitCenter()
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.ic_school)
                                .into(ivEditInstituteLogo);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadFile() {
        if (imageUri != null) {
            final StorageReference fileRef = storageReference.child("InstituteLogo" + System.currentTimeMillis() + "." + getFileExtension(imageUri));

            mUploadTask = fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                }
                            }, 5000);
                            Toast.makeText(getContext(), "Uploaded..", Toast.LENGTH_LONG).show();
                            //imageUrl=taskSnapshot.getStorage().getDownloadUrl().toString();
                            // System.out.println("Image Url of profile Stored "+taskSnapshot.getStorage().getDownloadUrl().getResult().toString());
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // getting image uri and converting into string
                                    Uri downloadUrl = uri;
                                    imageUrl = downloadUrl.toString();
                                    if (!TextUtils.isEmpty(institute.getLogoImagePath())) {
                                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(institute.getLogoImagePath());
                                        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                    institute.setLogoImagePath(imageUrl);
                                    updateInstituteToDatabase();
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            //progressDialog.setMessage(((int) progress) + "% Uploaded...");
                        }
                    });
        } else {
            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    void updateInstituteToDatabase() {
        instituteCollectionRef.document(InstituteId).set(institute).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (pDialog != null && pDialog.isShowing()) {
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
                } else {
                    if (pDialog != null && pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                }
            }
        });
    }
}
