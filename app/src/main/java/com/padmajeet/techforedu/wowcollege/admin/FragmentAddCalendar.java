package com.padmajeet.techforedu.wowcollege.admin;


import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.padmajeet.eduapp.wowcollege.admin.model.Batch;
import com.padmajeet.eduapp.wowcollege.admin.model.Calendar;
import com.padmajeet.eduapp.wowcollege.admin.util.SessionManager;
import com.padmajeet.eduapp.wowcollege.admin.util.Utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAddCalendar extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference calendarCollectionRef = db.collection("Calendar");
    private RadioButton radioBtnAll, radioBtnSelect;
    private RadioGroup radioGroupBatch;
    private LinearLayout llSelectBatch;
    private CheckBox cbBatch;
    private EditText etEvent, etDescription, etFromDate, etToDate;
    private ImageView ivFromDate, ivToDate;
    private Button btnSave;
    private TextView tvError;
    private DatePickerDialog fromDatePicker, toDatePicker;
    private SweetAlertDialog pDialog;
    private ImageButton ibMic;
    private SessionManager sessionManager;
    private String loggedInUserId, academicYearId, instituteId;
    private List<Batch> batchList = new ArrayList<>();
    private List<Batch> selectedBatchList = new ArrayList<>();
    private boolean isSelectBatchClickedFirstTime = true;
    private int i;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private StringBuffer comment = new StringBuffer();
    private Calendar calendar;

    public FragmentAddCalendar() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        loggedInUserId = sessionManager.getString("loggedInUserId");
        academicYearId = sessionManager.getString("academicYearId");
        instituteId = sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        radioBtnAll = view.findViewById(R.id.radioBtnAll);
        radioBtnSelect = view.findViewById(R.id.radioBtnSelect);
        radioGroupBatch = view.findViewById(R.id.radioGroupBatch);
        llSelectBatch = view.findViewById(R.id.llSelectBatch);
        etEvent = view.findViewById(R.id.etEvent);
        etDescription = view.findViewById(R.id.etDescription);
        etFromDate = view.findViewById(R.id.etFromDate);
        etToDate = view.findViewById(R.id.etToDate);
        ivFromDate = view.findViewById(R.id.ivFromDate);
        ivToDate = view.findViewById(R.id.ivToDate);
        btnSave = view.findViewById(R.id.btnSave);
        tvError = view.findViewById(R.id.tvError);
        etDescription = view.findViewById(R.id.etDescription);
        ibMic = view.findViewById(R.id.ibMic);

        getBatches();
        radioGroupBatch.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (selectedBatchList != null) {
                    selectedBatchList.clear();
                }
                if (radioBtnAll.isChecked()) {
                    for (Batch batch : batchList) {
                        selectedBatchList.add(batch);
                    }
                    llSelectBatch.setVisibility(View.GONE);
                } else {
                    if (radioBtnSelect.isChecked()) {
                        if (selectedBatchList != null) {
                            selectedBatchList.clear();
                        }
                        if (batchList.size() != 0) {
                            if (isSelectBatchClickedFirstTime) {
                                llSelectBatch.removeAllViews();
                                int i = 0;
                                LinkedHashMap<Integer, String> batch_list = new LinkedHashMap<Integer, String>();
                                for (Batch batch : batchList) {
                                    i++;
                                    batch_list.put(i, batch.getName());
                                }
                                Set<?> set = batch_list.entrySet();
                                // Get an iterator
                                Iterator<?> iterator = set.iterator();
                                // Display elements
                                while (iterator.hasNext()) {
                                    @SuppressWarnings("rawtypes")
                                    Map.Entry me = (Map.Entry) iterator.next();

                                    cbBatch = new CheckBox(getContext());
                                    cbBatch.setId(Integer.parseInt(me.getKey().toString()));
                                    cbBatch.setText(me.getValue().toString());
                                    cbBatch.setOnClickListener(getOnClickDoSomething(cbBatch));
                                    llSelectBatch.addView(cbBatch);
                                }
                                isSelectBatchClickedFirstTime = false;
                            }
                        }
                        llSelectBatch.setVisibility(View.VISIBLE);
                    }

                }
            }
        });
        final java.util.Calendar instance = java.util.Calendar.getInstance();
        final int day = instance.get(java.util.Calendar.DAY_OF_MONTH);
        final int month = instance.get(java.util.Calendar.MONTH);
        final int year = instance.get(java.util.Calendar.YEAR);

        ivFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //etFromDate.setEnabled(true);
                fromDatePicker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etFromDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                            }
                        }, year, month, day);
                fromDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                fromDatePicker.setTitle("Select from date");
                fromDatePicker.show();
            }
        });

        ivToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //etToDate.setEnabled(true);
                toDatePicker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etToDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                            }
                        }, year, month, day);
                toDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                toDatePicker.setTitle("Select to date");
                toDatePicker.show();
            }
        });

        ibMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvError.setVisibility(View.GONE);
                if (selectedBatchList == null || selectedBatchList.size() <= 0) {
                    tvError.setText("Select class");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }
                String event = etEvent.getText().toString().trim();
                if (TextUtils.isEmpty(event)) {
                    etEvent.setError("Enter the event");
                    etEvent.requestFocus();
                    return;
                } else {
                    if (Utility.isNumericWithSpace(event)) {
                        etEvent.setError("Invalid event");
                        etEvent.requestFocus();
                        return;
                    }
                }

                String desc = etDescription.getText().toString().trim();
                if (!TextUtils.isEmpty(desc)) {
                    if (Utility.isNumericWithSpace(event)) {
                        etDescription.setError("Invalid description");
                        etDescription.requestFocus();
                        return;
                    }
                }

                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date fromDt = null;
                Date toDayDate = new Date();
                toDayDate.setTime(toDayDate.getTime() - 24 * 60 * 60 * 1000);
                System.out.println("toDayDate " + toDayDate);
                String fromDate = etFromDate.getText().toString().trim();
                if (TextUtils.isEmpty(fromDate)) {
                    etFromDate.setError("Select the from date");
                    etFromDate.requestFocus();
                    return;
                } else {
                    try {
                        fromDt = dateFormat.parse(fromDate);
                        System.out.println("fromDt " + toDayDate.after(fromDt));
                        if (toDayDate.after(fromDt)) {
                            etFromDate.setError("From date is already over");
                            etFromDate.requestFocus();
                            return;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        etFromDate.setError("From date format should be in dd/MM/yyyy");
                        etFromDate.requestFocus();
                        return;
                    }

                }
                String toDate = etToDate.getText().toString().trim();
                Date toDt = null;
                if (!TextUtils.isEmpty(toDate)) {
                    try {
                        toDt = dateFormat.parse(toDate);
                        if (fromDt.after(toDt)) {
                            etToDate.setError("To date must be less than from date");
                            etToDate.requestFocus();
                            return;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        etToDate.setError("To date format should be in dd/MM/yyyy");
                        etToDate.requestFocus();
                        return;
                    }
                }
                if (fromDate.equalsIgnoreCase(toDate)) {
                    toDt = null;
                }
                if (pDialog != null) {
                    pDialog.show();
                }
                calendar = new Calendar();
                calendar.setAcademicYearId(academicYearId);
                calendar.setCreatedDate(new Date());
                calendar.setCreatorId(loggedInUserId);
                calendar.setCreatorType("A");
                calendar.setModifierId(loggedInUserId);
                calendar.setModifierType("A");
                calendar.setDescription(desc);
                calendar.setEvent(event);
                calendar.setFromDate(fromDt);
                calendar.setToDate(toDt);
                calendar.setInstituteId(instituteId);
                calendar.setStatus("A");
                for (i = 0; i < selectedBatchList.size(); i++) {
                    Batch batch = selectedBatchList.get(i);
                    calendar.setBatchId(batch.getId());
                    calendarCollectionRef
                            .add(calendar)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Add")
                        .setContentText("Calendar successfully added.")
                        .setConfirmText("Ok")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                FragmentCalendar fragmentCalendar = new FragmentCalendar();
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                fragmentTransaction.replace(R.id.contentLayout, fragmentCalendar).commit();
                            }
                        });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        comment.delete(0, comment.length());
        comment.append(etDescription.getText().toString());
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    comment.append(result.get(0)).append("\n");
                    etDescription.setText(comment.toString());
                }
                break;
            }
        }
    }

    private void getBatches() {
        if (!pDialog.isShowing() && pDialog == null) {
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo(instituteId, instituteId)
                //.orderBy("eligibleYears", Query.Direction.ASCENDING)
                //.orderBy("eligibleMonths", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            Batch batch = documentSnapshot.toObject(Batch.class);
                            batch.setId(documentSnapshot.getId());
                            batchList.add(batch);
                        }
                        if(batchList.size()==0){
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText(R.string.noBatch);
                            btnSave.setVisibility(View.GONE);
                        }else {
                            tvError.setVisibility(View.GONE);
                            btnSave.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]

    }

    View.OnClickListener getOnClickDoSomething(final Button button) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                // Check which checkbox was clicked
                if (checked) {
                    for (Batch batch : batchList) {
                        if (button.getText().toString().equals(batch.getName())) {
                            // System.out.println("Batch Name" + button.getText().toString());
                            selectedBatchList.add(batch);
                            break;
                        }
                    }
                } else {
                    for (Batch batch : batchList) {
                        if (button.getText().toString().equals(batch.getName())) {
                            selectedBatchList.remove(batch);
                            break;
                        }
                    }

                }

            }
        };
    }
}
