package com.padmajeet.techforedu.wowcollege.admin;


import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Calendar;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCalendar extends Fragment {

    private View view = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference calendarCollectionRef = db.collection("Calendar");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private Spinner spBatch;
    private ArrayList<Batch> batchList = new ArrayList<>();
    private Batch selectedBatch, batch;
    private LinearLayout llNoList;
    private List<Calendar> calendarList = new ArrayList<>();
    private Calendar calendar;
    private String date;
    private boolean isFromDate, isToDate;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private RecyclerView rvCalendar;
    private RecyclerView.Adapter calendarAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton fab;
    private Staff loggedInUser;
    private String instituteId, loggedInUserId, academicYearId;
    private Gson gson;
    int[] circles = {R.drawable.circle_blue_filled, R.drawable.circle_brown_filled, R.drawable.circle_green_filled, R.drawable.circle_pink_filled, R.drawable.circle_orange_filled};
    SweetAlertDialog pDialog;
    Date toDayDate = new Date();
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private StringBuffer comment = new StringBuffer();
    private int day, month, year;
    private DatePickerDialog editFromDatePicker, editToDatePicker;
    private EditText etDescription;

    public FragmentCalendar() {
        // Required empty public constructor
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
        toDayDate.setTime(toDayDate.getTime() - 24 * 60 * 60 * 1000);
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_calendar, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.academicCalendar));
        spBatch = view.findViewById(R.id.spBatch);
        rvCalendar = (RecyclerView) view.findViewById(R.id.rvCalendar);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvCalendar.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        getBatches();
        fab = (FloatingActionButton) view.findViewById(R.id.addCalender);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentAddCalendar fragmentAddCalendar = new FragmentAddCalendar();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.contentLayout, fragmentAddCalendar).addToBackStack(null).commit();
            }
        });
        return view;
    }

    private void getBatches() {
        if (batchList.size() != 0) {
            batchList.clear();
        }
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        batchCollectionRef
                .whereEqualTo("instituteId", instituteId)
                //.orderBy("eligibleYears", Query.Direction.ASCENDING)
                //.orderBy("eligibleMonths", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            batch = documentSnapshot.toObject(Batch.class);
                            batch.setId(documentSnapshot.getId());
                            batchList.add(batch);
                        }
                        if (batchList.size() != 0) {
                            List<String> batchNameList = new ArrayList<>();
                            for (Batch batch : batchList) {
                                batchNameList.add(batch.getName());
                            }
                            ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                            batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spBatch.setAdapter(batchAdaptor);

                            spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedBatch = batchList.get(position);
                                    getCalendarOfBatch();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        } else {
                            spBatch.setEnabled(false);
                            llNoList.setVisibility(View.VISIBLE);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]
    }


    private void getCalendarOfBatch() {
        if (calendarList.size() != 0) {
            calendarList.clear();
        }
        if (pDialog == null && !pDialog.isShowing()) {
            pDialog.show();
        }
        calendarCollectionRef
                .whereEqualTo("academicYearId", academicYearId)
                .whereEqualTo("batchId", selectedBatch.getId())
                .orderBy("fromDate", Query.Direction.ASCENDING)
                .orderBy("toDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            calendar = documentSnapshot.toObject(Calendar.class);
                            calendar.setId(documentSnapshot.getId());
                            calendarList.add(calendar);
                        }
                        if (calendarList.size() != 0) {
                            calendarAdapter = new CalendarAdapter(calendarList);
                            rvCalendar.setAdapter(calendarAdapter);
                            rvCalendar.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        } else {
                            rvCalendar.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]

    }


    class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.MyViewHolder> {
        private List<Calendar> calendarList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvEvent, tvDate, tvDaysOfMonth, tvMonth;
            private ImageView ivEditCalendar, ivDeleteCalendar;
            private LinearLayout llImage;

            public MyViewHolder(View view) {
                super(view);
                tvEvent = view.findViewById(R.id.tvEvent);
                tvDate = view.findViewById(R.id.tvDate);
                ivEditCalendar = view.findViewById(R.id.ivEditCalendar);
                ivDeleteCalendar = view.findViewById(R.id.ivDeleteCalendar);
                tvDaysOfMonth = view.findViewById(R.id.tvDaysOfMonth);
                tvMonth = view.findViewById(R.id.tvMonth);
                llImage = view.findViewById(R.id.llImage);
            }
        }


        public CalendarAdapter(List<Calendar> calendarList) {
            this.calendarList = calendarList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_calendar, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Calendar calendar = calendarList.get(position);
            holder.tvEvent.setText("" + calendar.getEvent());
            int colorCode = position % 5;
            holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
            String eventDate = null;
            if (calendar.getFromDate() != null) {
                eventDate = Utility.formatDateToString(calendar.getFromDate().getTime());
                Format formatter = new SimpleDateFormat("MMM");
                holder.tvMonth.setText("" + formatter.format(calendar.getFromDate()).toUpperCase());
                formatter = new SimpleDateFormat("dd");
                holder.tvDaysOfMonth.setText("" + formatter.format(calendar.getFromDate()));
            }
            if (calendar.getToDate() != null) {
                eventDate = eventDate + " to " + Utility.formatDateToString(calendar.getToDate().getTime());
            }
            if (eventDate != null) {
                holder.tvDate.setText("" + eventDate);
            }
            if (calendar.getToDate() != null) {
                if (calendar.getToDate().getTime() < toDayDate.getTime()) {
                    holder.ivEditCalendar.setVisibility(View.GONE);
                } else {
                    holder.ivEditCalendar.setVisibility(View.VISIBLE);
                }
            } else {
                if (calendar.getFromDate().getTime() < toDayDate.getTime()) {
                    holder.ivEditCalendar.setVisibility(View.GONE);
                } else {
                    holder.ivEditCalendar.setVisibility(View.VISIBLE);
                }
            }
            holder.ivEditCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogLayout = inflater.inflate(R.layout.dialog_edit_calendar, null);
                    TextView tvError = dialogLayout.findViewById(R.id.tvError);
                    EditText etEditEvent = dialogLayout.findViewById(R.id.etEditEvent);
                    etEditEvent.setText("" + calendar.getEvent());
                    ImageButton ibEditMic = dialogLayout.findViewById(R.id.ibEditMic);
                    etDescription = dialogLayout.findViewById(R.id.etDescription);
                    if (!TextUtils.isEmpty(calendar.getDescription())) {
                        etDescription.setText("" + calendar.getDescription());
                    }
                    ibEditMic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            promptSpeechInput();
                        }
                    });
                    EditText etEditFromDate = dialogLayout.findViewById(R.id.etEditFromDate);
                    ImageView ivEditFromDate = dialogLayout.findViewById(R.id.ivEditFromDate);
                    final java.util.Calendar cldr = java.util.Calendar.getInstance();
                    cldr.setTime(calendar.getFromDate());
                    day = cldr.get(java.util.Calendar.DAY_OF_MONTH);
                    month = cldr.get(java.util.Calendar.MONTH);
                    year = cldr.get(java.util.Calendar.YEAR);
                    etEditFromDate.setText(String.format("%02d", day) + "/" + (String.format("%02d", (month + 1))) + "/" + year);
                    ivEditFromDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editFromDatePicker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                                    new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                            etEditFromDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                        }
                                    }, year, month, day);
                            editFromDatePicker.setTitle("Select From Date");
                            editFromDatePicker.show();
                        }
                    });
                    EditText etEditToDate = dialogLayout.findViewById(R.id.etEditToDate);
                    ImageView ivEditToDate = dialogLayout.findViewById(R.id.ivEditToDate);
                    if (calendar.getToDate() != null) {
                        cldr.setTime(calendar.getToDate());
                        day = cldr.get(java.util.Calendar.DAY_OF_MONTH);
                        month = cldr.get(java.util.Calendar.MONTH);
                        year = cldr.get(java.util.Calendar.YEAR);
                        etEditToDate.setText(String.format("%02d", day) + "/" + (String.format("%02d", (month + 1))) + "/" + year);
                    } else {
                        cldr.setTime(new Date());
                        day = cldr.get(java.util.Calendar.DAY_OF_MONTH);
                        month = cldr.get(java.util.Calendar.MONTH);
                        year = cldr.get(java.util.Calendar.YEAR);
                    }
                    ivEditToDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editToDatePicker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                                    new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                            etEditToDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                        }
                                    }, year, month, day);
                            editToDatePicker.setTitle("Select To Date");
                            editToDatePicker.show();
                        }
                    });
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit Calendar")
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
                                    String event = etEditEvent.getText().toString().trim();
                                    if (TextUtils.isEmpty(event)) {
                                        etEditEvent.setError("Enter the event");
                                        etEditEvent.requestFocus();
                                        return;
                                    } else {
                                        if (Utility.isNumeric(event)) {
                                            etEditEvent.setError("Invalid event");
                                            etEditEvent.requestFocus();
                                            return;
                                        } else {
                                            calendar.setEvent(event);
                                        }
                                    }
                                    String description = etDescription.getText().toString().trim();
                                    if (!TextUtils.isEmpty(description)) {
                                        if (Utility.isNumeric(description)) {
                                            etDescription.setError("Invalid description");
                                            etDescription.requestFocus();
                                            return;
                                        } else {
                                            calendar.setDescription(description);
                                        }
                                    }
                                    String fromDate = etEditFromDate.getText().toString().trim();
                                    if (TextUtils.isEmpty(fromDate)) {
                                        etEditFromDate.setError("Select the from date");
                                        etEditFromDate.requestFocus();
                                        return;
                                    }
                                    String toDate = etEditToDate.getText().toString().trim();

                                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                    Date fromDt = null;
                                    try {
                                        fromDt = dateFormat.parse(fromDate);
                                        if (fromDt.before(new Date())) {
                                            tvError.setVisibility(View.VISIBLE);
                                            tvError.setText("From date is already over");
                                            return;
                                        } else {
                                            calendar.setToDate(fromDt);
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        etEditFromDate.setError("Date format dd/MM/yyyy");
                                        etEditFromDate.requestFocus();
                                        return;
                                    }
                                    Date toDt = null;
                                    if (!TextUtils.isEmpty(toDate)) {
                                        try {
                                            toDt = dateFormat.parse(toDate);
                                            calendar.setToDate(toDt);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                            etEditToDate.setError("Date format dd/MM/yyyy");
                                            etEditToDate.requestFocus();
                                            return;
                                        }
                                        if (fromDt.after(toDt)) {
                                            tvError.setVisibility(View.VISIBLE);
                                            tvError.setText("To date must be less than from date");
                                            return;
                                        }
                                    } else {
                                        calendar.setToDate(toDt);
                                    }
                                    calendar.setModifiedDate(new Date());
                                    calendar.setModifierId(loggedInUserId);
                                    calendar.setModifierType("A");
                                    if (pDialog == null && !pDialog.isShowing()) {
                                        pDialog.show();
                                    }
                                    calendarCollectionRef.document(calendar.getId()).set(calendar).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                if (pDialog != null) {
                                                    pDialog.dismiss();
                                                }
                                                sDialog.dismissWithAnimation();
                                                SweetAlertDialog sweetAlertDialog=new SweetAlertDialog(getContext(),SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Calendar is updated")
                                                        .setConfirmText("Ok")
                                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog saDialog) {
                                                                saDialog.dismissWithAnimation();
                                                            }
                                                        });
                                                sweetAlertDialog.setCancelable(false);
                                                sweetAlertDialog.show();
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
            holder.ivDeleteCalendar.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Delete")
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
                                    sDialog.dismissWithAnimation();
                                    if (!pDialog.isShowing() && pDialog == null) {
                                        pDialog.show();
                                    }
                                    calendarCollectionRef.document(calendar.getId())
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if (pDialog != null) {
                                                        pDialog.dismiss();
                                                    }
                                                    SweetAlertDialog successDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                            .setTitleText("Deleted")
                                                            .setContentText("Calendar Has Been Deleted.")
                                                            .setConfirmText("Ok")
                                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                @Override
                                                                public void onClick(SweetAlertDialog sDialog) {
                                                                    sDialog.dismissWithAnimation();
                                                                    //getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
                                                                    getCalendarOfBatch();
                                                                }
                                                            });
                                                    successDialog.setCancelable(false);
                                                    successDialog.show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return calendarList.size();
        }
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


}
