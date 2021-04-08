package com.padmajeet.techforedu.wowcollege.admin;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Event;
import com.padmajeet.techforedu.wowcollege.admin.model.EventType;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;


public class FragmentAddEvent extends Fragment {
    private View view = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference eventCollectionRef = db.collection("Event");
    private CollectionReference eventTypeCollectionRef = db.collection("EventType");
    private List<Event> eventList = new ArrayList<>();
    private Event event;
    private List<EventType> eventTypeList = new ArrayList<>();
    private EventType selectEventType, eventType;
    private List<Batch> batchList = new ArrayList<>();
    private Batch batch;
    private List<Batch> selectedBatchList = new ArrayList<>();
    private String academicYearId, instituteId, loggedInUserId;
    private Date from_date, to_date;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SessionManager sessionManager;
    private SweetAlertDialog pDialog;
    private DatePickerDialog fromDatePicker,toDatePicker;
    private TextView tvError;
    private CheckBox cbParent, cbFaculty, cbBatch;
    private RadioGroup radioGroupResponseType, radioGroupBatch;
    private RadioButton radioNotResponse, radioResponse, radioAllBatch, radioSelectBatch;
    private LinearLayout llBatch, llSelectBatch;
    private Spinner spEventType;
    private EditText etEvent, etDressCode, etFromDate, etToDate, etDescription;
    private ImageView ivFromDate, ivToDate;
    private String name, dressCode, description, fromDate, toDate;
    private ImageButton ibMic;
    private Button btnSave;
    private Boolean isParentClicked = true, isFacultyClicked = false, schoolScope = true;
    private String category_add = "N";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int PICK_FILE_RESULT_CODE = 1;
    private Uri attachUri;
    private StorageTask mUploadTask;
    private StorageReference storageReference;
    private String attachmentUrl;
    private ImageButton ibChooseFile, ibRemoveFile;
    private TextView tvAttachmentFile;
    private StringBuffer comment = new StringBuffer();
    private Map<String, String> map = new HashMap<String, String>();
    private int c;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        loggedInUserId = sessionManager.getString("loggedInUserId");
        academicYearId = sessionManager.getString("academicYearId");
        instituteId = sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
        storageReference = FirebaseStorage.getInstance().getReference("EventAttachment");
    }

    public FragmentAddEvent() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_event, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.event));
        tvError = view.findViewById(R.id.tvError);
        cbParent = view.findViewById(R.id.cbParent);
        cbFaculty = view.findViewById(R.id.cbFaculty);
        radioGroupResponseType = view.findViewById(R.id.radioGroupResponseType);
        radioNotResponse = view.findViewById(R.id.radioNotResponse);
        radioResponse = view.findViewById(R.id.radioResponse);
        radioGroupBatch = view.findViewById(R.id.radioGroupBatch);
        radioAllBatch = view.findViewById(R.id.radioAllBatch);
        radioSelectBatch = view.findViewById(R.id.radioSelectBatch);
        llBatch = view.findViewById(R.id.llBatch);
        llSelectBatch = view.findViewById(R.id.llSelectBatch);
        spEventType = view.findViewById(R.id.spEventType);
        etEvent = view.findViewById(R.id.etEvent);
        etDescription = view.findViewById(R.id.etDescription);
        ibMic = view.findViewById(R.id.ibMic);
        etDressCode = view.findViewById(R.id.etDressCode);
        etFromDate = view.findViewById(R.id.etFromDate);
        ivFromDate = view.findViewById(R.id.ivFromDate);
        etToDate = view.findViewById(R.id.etToDate);
        ivToDate = view.findViewById(R.id.ivToDate);
        btnSave = view.findViewById(R.id.btnSave);
        tvAttachmentFile = view.findViewById(R.id.tvAttachmentFile);
        ibChooseFile = view.findViewById(R.id.ibChooseFile);
        ibRemoveFile = view.findViewById(R.id.ibRemoveFile);

        etFromDate.setInputType(InputType.TYPE_NULL);
        etToDate.setInputType(InputType.TYPE_NULL);
        Calendar instance = Calendar.getInstance();
        final int day = instance.get(Calendar.DAY_OF_MONTH);
        final int month = instance.get(Calendar.MONTH);
        final int year = instance.get(Calendar.YEAR);

        getEventTypes();//Get Event Type's
        getBatches();//Get Batch's

        ibChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        ibRemoveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFileChooser();
            }
        });
        ivFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //etFromDate.setEnabled(true);
                // date picker dialog
                fromDatePicker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etFromDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                            }
                        }, year, month, day);
                fromDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                fromDatePicker.setTitle("SELECT FROM DATE");
                fromDatePicker.show();
            }
        });
        ivToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //etToDate.setEnabled(true);
                // date picker dialog
                toDatePicker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etToDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                            }
                        }, year, month, day);
                toDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                toDatePicker.setTitle("SELECT TO DATE");
                toDatePicker.show();
            }
        });
        cbParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                // Check which checkbox was clicked
                if (checked) {
                    isParentClicked = true;
                    radioGroupBatch.setVisibility(View.VISIBLE);
                    onChangeBatch();
                } else {
                    isParentClicked = false;
                    radioGroupBatch.setVisibility(View.GONE);
                    llBatch.setVisibility(View.GONE);
                }
            }
        });
        cbFaculty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked) {
                    if (!isParentClicked) {
                        radioGroupBatch.setVisibility(View.GONE);
                        llBatch.setVisibility(View.GONE);
                    }
                    isFacultyClicked = true;
                } else {
                    isFacultyClicked = false;
                }
            }
        });
        radioGroupResponseType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (radioNotResponse.isChecked()) {
                    category_add = "N";
                }
                if (radioResponse.isChecked()) {
                    category_add = "R";
                }
            }
        });
        radioGroupBatch.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (radioAllBatch.isChecked()) {
                    schoolScope = true;
                    llBatch.setVisibility(View.GONE);
                }
                if (radioSelectBatch.isChecked()) {
                    schoolScope = false;
                    llBatch.setVisibility(View.VISIBLE);
                }
            }
        });
        spEventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectEventType = eventTypeList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // System.out.println("button is clicked");
                if (!isParentClicked && !isFacultyClicked) {
                    tvError.setText("Please select parent or teacher");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                } else {
                    tvError.setVisibility(View.GONE);
                }
                if (isParentClicked) {
                    if (!schoolScope) {
                        if (selectedBatchList.size() == 0) {
                            tvError.setText("Select at least one class");
                            tvError.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            if (selectedBatchList.size() == batchList.size()) {
                                schoolScope = true;
                            } else {
                                schoolScope = false;
                            }
                        }
                    }
                }
                if (selectEventType == null) {
                    tvError.setText("Please select event type");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }
                name = etEvent.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etEvent.setError("Enter event");
                    etEvent.requestFocus();
                    return;
                }else {
                    if(Utility.isNumericWithSpace(name)){
                        etEvent.setError("Enter valid event");
                        etEvent.requestFocus();
                        return;
                    }
                }
                description = etDescription.getText().toString().trim();
                if (!TextUtils.isEmpty(description)) {
                    if(Utility.isNumericWithSpace(description)){
                        etDescription.setError("Enter valid description");
                        etDescription.requestFocus();
                        return;
                    }
                }
                dressCode = etDressCode.getText().toString().trim();
                if (!TextUtils.isEmpty(dressCode)) {
                    if(!Utility.isAlphabetic(dressCode)){
                        etDressCode.setError("Enter valid dress code");
                        etDressCode.requestFocus();
                        return;
                    }
                }
                fromDate = etFromDate.getText().toString().trim();
                if (TextUtils.isEmpty(fromDate)) {
                    etFromDate.setError("Enter from date");
                    etFromDate.requestFocus();
                    return;
                } else {
                    try {
                        from_date = dateFormat.parse(fromDate);
                        if (from_date.before(new Date()) || from_date.equals(new Date())) {
                            etFromDate.setError("From date is already over");
                            etFromDate.requestFocus();
                            return;
                        }
                    } catch (ParseException e) {
                        etFromDate.setError("DD/MM/YYYY");
                        etFromDate.requestFocus();
                        e.printStackTrace();
                        return;
                    }

                }
                //System.out.println("From Date - " + fromDate);
                toDate = etToDate.getText().toString().trim();

                //System.out.println("To Date - " + toDate);
                if (!TextUtils.isEmpty(toDate)) {

                    try {
                        to_date = dateFormat.parse(toDate);

                    } catch (ParseException e) {
                        etToDate.setError("DD/MM/YYYY");
                        etToDate.requestFocus();
                        e.printStackTrace();
                        return;
                    }
                    if (from_date.after(to_date)) {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("To Date must be greater than From Date");
                        return;
                    }
                    if (from_date.equals(to_date)) {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Both Date's are equal");
                        etToDate.setText("");
                        return;
                    }
                }
                if (eventList.size() != 0) {
                    eventList.clear();
                }
                map.clear();
                for (int k = 0; k < selectedBatchList.size(); k++) {
                    System.out.print("selectedBatchList " + selectedBatchList.get(k).getName());
                }
                if (attachUri != null) {
                    uploadFile();
                } else {
                    attachmentUrl="";
                    addEvents();
                }
            }
        });

        ibMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        return view;
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
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    comment.append(result.get(0)).append("\n");
                    etDescription.setText(comment.toString());
                }
                break;
            }
            case PICK_FILE_RESULT_CODE: {
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    System.out.println("data.getData() " + data.getData());
                    attachUri = data.getData();
                    try {
                        if (mUploadTask != null && mUploadTask.isInProgress()) {
                            Toast.makeText(getContext(), "Upload in process...", Toast.LENGTH_SHORT).show();
                        } else {
                            tvAttachmentFile.setText(DocumentFile.fromSingleUri(getContext(), attachUri).getName());
                            //uploadFile();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    private void onChangeBatch() {
        if (schoolScope) {
            llBatch.setVisibility(View.GONE);
        } else {
            llBatch.setVisibility(View.VISIBLE);
        }
    }

    private void getEventTypes() {
        if(pDialog==null && !pDialog.isShowing()){
            pDialog.show();
        }
        if(eventTypeList.size()>0){
            eventTypeList.clear();
        }
        eventTypeCollectionRef
                .whereEqualTo("instituteId", instituteId)
                .orderBy("createdDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            eventType = documentSnapshot.toObject(EventType.class);
                            eventType.setId(documentSnapshot.getId());
                            eventTypeList.add(eventType);
                        }
                        if (eventTypeList.size() != 0) {
                            List<String> typeNameList = new ArrayList<>();
                            for (EventType eventType : eventTypeList) {
                                typeNameList.add(eventType.getName());
                            }
                            ArrayAdapter<String> eventTypeAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, typeNameList);
                            eventTypeAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spEventType.setAdapter(eventTypeAdaptor);
                        } else {
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText(R.string.noEventType);
                            spEventType.setVisibility(View.GONE);
                            btnSave.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        // [END get_all_users]

    }

    View.OnClickListener getOnClickBatch(final Button button) {
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

    private void getBatches() {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        if(batchList.size()>0){
            batchList.clear();
        }
        batchCollectionRef
                .whereEqualTo("instituteId", instituteId)
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
                            batch = documentSnapshot.toObject(Batch.class);
                            batch.setId(documentSnapshot.getId());
                            batchList.add(batch);
                        }
                        if (batchList.size() != 0) {
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
                                cbBatch.setOnClickListener(getOnClickBatch(cbBatch));
                                llSelectBatch.addView(cbBatch);
                            }
                        }else{
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText(R.string.noBatch);
                            btnSave.setVisibility(View.GONE);
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


    }

    private void addEvents() {
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        System.out.println("isFacultyClicked "+isFacultyClicked);
        if (isFacultyClicked) {
            System.out.println("isFacultyClicked "+isFacultyClicked);
            event = new Event();
            event.setName(name);
            event.setDescription(description);
            event.setDressCode(dressCode);
            event.setFromDate(from_date);
            event.setToDate(to_date);
            event.setCreatorType("F");
            event.setCreatorId(loggedInUserId);
            event.setModifierType("F");
            event.setModifierId(loggedInUserId);
            event.setBatchId("");
            event.setInstituteId(instituteId);
            event.setAcademicYearId(academicYearId);
            event.setTypeId(selectEventType.getId());
            event.setRecipientType("F");
            event.setCategory(category_add);
            event.setParentResponses(map);
            event.setSchoolScope(false);
            event.setAttachmentUrl(attachmentUrl);
            eventList.add(event);
        }
        System.out.println("isParentClicked "+isParentClicked);
        if (isFacultyClicked && !isParentClicked) {
            System.out.println("isParentClicked2 "+isParentClicked);
            addEventsToDatabase();
        }
        if (isParentClicked) {
            System.out.println("isParentClicked3 "+isParentClicked);
            if (schoolScope) {
                System.out.println("schoolScope "+schoolScope);
                event = new Event();
                event.setName(name);
                event.setDescription(description);
                event.setDressCode(dressCode);
                event.setFromDate(from_date);
                event.setToDate(to_date);
                event.setCreatorType("F");
                event.setCreatorId(loggedInUserId);
                event.setModifierType("F");
                event.setModifierId(loggedInUserId);
                event.setBatchId("");
                event.setInstituteId(instituteId);
                event.setAcademicYearId(academicYearId);
                event.setTypeId(selectEventType.getId());
                event.setRecipientType("P");
                event.setCategory(category_add);
                event.setParentResponses(map);
                event.setSchoolScope(true);
                event.setAttachmentUrl(attachmentUrl);
                eventList.add(event);
                addEventsToDatabase();
            } else {
                System.out.println("schoolScope "+schoolScope);
                if (eventList.size() == 1) {
                    c = selectedBatchList.size() + 1;
                } else {
                    c = selectedBatchList.size();
                }
                for (int j = 0; j < selectedBatchList.size(); j++) {
                    event = new Event();
                    event.setName(name);
                    event.setDescription(description);
                    event.setDressCode(dressCode);
                    event.setFromDate(from_date);
                    event.setToDate(to_date);
                    event.setCreatorType("F");
                    event.setCreatorId(loggedInUserId);
                    event.setModifierType("F");
                    event.setModifierId(loggedInUserId);
                    event.setBatchId(selectedBatchList.get(j).getId());
                    event.setInstituteId(instituteId);
                    event.setAcademicYearId(academicYearId);
                    event.setTypeId(selectEventType.getId());
                    event.setRecipientType("P");
                    event.setCategory(category_add);
                    event.setParentResponses(map);
                    event.setSchoolScope(false);
                    event.setAttachmentUrl(attachmentUrl);
                    eventList.add(event);
                    if (eventList.size() == c) {
                        addEventsToDatabase();
                    }
                }
            }
        }
    }
    private void addEventsToDatabase(){
        for (Event event : eventList) {
            eventCollectionRef
                    .add(event)
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
                .setContentText("Event Is Successfully Added.")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        FragmentEvent fragmentEvent = new FragmentEvent();
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        fragmentTransaction.replace(R.id.contentLayout, fragmentEvent).addToBackStack(null).commit();

                    }
                });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose a file"), PICK_FILE_RESULT_CODE);
    }

    private void removeFileChooser() {
        attachUri = null;
        tvAttachmentFile.setText("");
    }

    private void uploadFile() {
        if (attachUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final StorageReference fileRef = storageReference.child("Event" + System.currentTimeMillis() + "." + getFileExtension(attachUri));

            mUploadTask = fileRef.putFile(attachUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setProgress(0);
                                }
                            }, 5000);
                            Toast.makeText(getContext(), "Uploaded..", Toast.LENGTH_LONG).show();
                            pDialog.show();
                            //imageUrl=taskSnapshot.getStorage().getDownloadUrl().toString();
                            // System.out.println("Image Url of profile Stored "+taskSnapshot.getStorage().getDownloadUrl().getResult().toString());
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // getting image uri and converting into string
                                    Uri downloadUrl = uri;
                                    attachmentUrl = downloadUrl.toString();
                                    if (attachmentUrl != null) {
                                        tvAttachmentFile.setText(DocumentFile.fromSingleUri(getContext(), attachUri).getName());
                                        addEvents();
                                    }
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage(((int) progress) + "% Uploaded...");
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
}
