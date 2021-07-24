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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Holiday;
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

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHoliday extends Fragment {

    private LinearLayout llNoList;
    private List<Holiday> holidayList = new ArrayList<>();
    private Bundle bundle = new Bundle();
    private String academicYearId;
    private Fragment currentFragment = this;
    private String date;
    private boolean isFromDate, isToDate;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference holidayCollectionRef = db.collection("Holiday");
    private Batch batch;
    private ArrayList<Batch> batchList = new ArrayList<>();
    private RecyclerView rvHoliday;
    private RecyclerView.Adapter holidayAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String loggedInUserId;
    private FloatingActionButton fab;
    private Staff loggedInUser;
    private String instituteId;
    private Gson gson;
    private EditText etEvent;
    private EditText etDescription, etFromDate, etToDate;
    private ImageView ivToDate, ivFromDate;
    private Button btnSave;
    private TextView tvError;
    private DatePickerDialog picker, fromDatePicker, toDatePicker, editFromDatePicker, editToDatePicker;
    private int[] circles = {R.drawable.circle_blue_filled, R.drawable.circle_brown_filled, R.drawable.circle_green_filled, R.drawable.circle_pink_filled, R.drawable.circle_orange_filled};
    private ListenerRegistration holidayListener;
    private int day, month, year;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private StringBuffer description = new StringBuffer();
    private StringBuffer editDescription = new StringBuffer();
    private EditText etEditDescription;
    private ImageButton ibMic;


    public FragmentHoliday() {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_holiday, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.holiday));
        rvHoliday = (RecyclerView) view.findViewById(R.id.rvHoliday);
        createBottomSheet();
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvHoliday.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        fab = (FloatingActionButton) view.findViewById(R.id.addHoliday);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        final SweetAlertDialog pDialog;
        pDialog = Utility.createSweetAlertDialog(getContext());
        pDialog.show();
        holidayListener = holidayCollectionRef
                .whereEqualTo("instituteId", instituteId)
                .whereEqualTo("academicYearId", academicYearId)
                .orderBy("fromDate", Query.Direction.DESCENDING)
                .orderBy("toDate", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (holidayList.size() != 0) {
                            holidayList.clear();
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            Holiday holiday = documentSnapshot.toObject(Holiday.class);
                            holiday.setId(documentSnapshot.getId());
                            holidayList.add(holiday);
                        }
                        //System.out.println("Calendar  -" + holidayList.size());
                        if (holidayList.size() != 0) {
                            holidayAdapter = new HolidayAdapter(holidayList);
                            rvHoliday.setAdapter(holidayAdapter);
                            rvHoliday.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        } else {
                            rvHoliday.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });
        // [END get_all_users]

    }

    @Override
    public void onStop() {
        super.onStop();
        if (holidayListener != null) {
            holidayListener.remove();
        }
    }

    BottomSheetDialog bottomSheetDialog;

    private void createBottomSheet() {
        if (bottomSheetDialog == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_holiday, null);
            bottomSheetDialog = new BottomSheetDialog(getContext());//new BottomSheetDialog(this,R.style.BottomSheetDialog)
            bottomSheetDialog.setContentView(view);
            etEvent = view.findViewById(R.id.etEvent);
            //etDescription = view.findViewById(R.id.etDescription);
            etFromDate = view.findViewById(R.id.etFromDate);
            etToDate = view.findViewById(R.id.etToDate);
            ivFromDate = view.findViewById(R.id.ivFromDate);
            ivToDate = view.findViewById(R.id.ivToDate);
            btnSave = view.findViewById(R.id.btnSave);
            tvError = view.findViewById(R.id.tvError);
            etDescription = view.findViewById(R.id.etDescription);
            ibMic = view.findViewById(R.id.ibMic);
            ibMic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    promptSpeechInput();
                }
            });
            final java.util.Calendar cldr = java.util.Calendar.getInstance();
            final int day = cldr.get(java.util.Calendar.DAY_OF_MONTH);
            final int month = cldr.get(java.util.Calendar.MONTH);
            final int year = cldr.get(java.util.Calendar.YEAR);

            ivFromDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fromDatePicker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    etFromDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                }
                            }, year, month, day);
                    fromDatePicker.setTitle("Select From Date");
                    fromDatePicker.show();
                }
            });
            etFromDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fromDatePicker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    etFromDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                }
                            }, year, month, day);
                    fromDatePicker.setTitle("Select From Date");
                    fromDatePicker.show();
                }
            });

            ivToDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toDatePicker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    etToDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                }
                            }, year, month, day);
                    toDatePicker.setTitle("Select To Date");
                    toDatePicker.show();
                }
            });
            etToDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toDatePicker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    etToDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                }
                            }, year, month, day);
                    toDatePicker.setTitle("Select To Date");
                    toDatePicker.show();
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvError.setVisibility(View.GONE);

                    String event = etEvent.getText().toString().trim();
                    if (TextUtils.isEmpty(event)) {
                        etEvent.setError("Enter the event");
                        etEvent.requestFocus();
                        return;
                    }

                    String fromDate = etFromDate.getText().toString().trim();
                    if (TextUtils.isEmpty(fromDate)) {
                        etFromDate.setError("Select the from date");
                        etFromDate.requestFocus();
                        return;
                    }
                    //String desc = etDescription.getText().toString().trim();
                    String toDate = etToDate.getText().toString().trim();

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date fromDt = null;
                    try {
                        fromDt = dateFormat.parse(fromDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        etFromDate.setError("Date format dd/MM/yyyy");
                        etFromDate.requestFocus();
                        return;
                    }
                    Date toDt = null;
                    if (!TextUtils.isEmpty(toDate)) {
                        try {
                            toDt = dateFormat.parse(toDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            etToDate.setError("Date format dd/MM/yyyy");
                            etToDate.requestFocus();
                            return;
                        }
                        if (fromDt.after(toDt)) {
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText("To Date must be less than From Date");
                            return;
                        }
                    }

                    Holiday holiday = new Holiday();
                    holiday.setAcademicYearId(academicYearId);
                    holiday.setCreatedDate(new Date());
                    holiday.setCreatorId(loggedInUserId);
                    holiday.setCreatorType("A");
                    holiday.setModifierId(loggedInUserId);
                    holiday.setModifierType("A");
                    //holiday.setDescription(desc);
                    holiday.setEvent(event);
                    holiday.setFromDate(fromDt);
                    holiday.setToDate(toDt);
                    holiday.setInstituteId(instituteId);
                    holiday.setStatus("A");
                    holidayCollectionRef
                            .add(holiday)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    bottomSheetDialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    bottomSheetDialog.dismiss();
                                    Toast.makeText(getContext(), "Something failed please try later", Toast.LENGTH_LONG).show();
                                }
                            });
                }
            });
        }
    }

    class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.MyViewHolder> {
        private List<Holiday> holidayList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvEvent, tvDate, tvDaysOfMonth, tvMonth;
            private ImageView ivDeleteCalendar, ivEditCalendar;
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


        public HolidayAdapter(List<Holiday> holidayList) {
            this.holidayList = holidayList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_calendar, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Holiday holiday = holidayList.get(position);
            holder.tvEvent.setText("" + holiday.getEvent());
            int colorCode = position % 5;
            holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
            String eventDate = null;
            if (holiday.getFromDate() != null) {
                eventDate = Utility.formatDateToString(holiday.getFromDate().getTime());
                Format formatter = new SimpleDateFormat("MMM");
                holder.tvMonth.setText("" + formatter.format(holiday.getFromDate()).toUpperCase());
                formatter = new SimpleDateFormat("dd");
                holder.tvDaysOfMonth.setText("" + formatter.format(holiday.getFromDate()));

            }
            if (holiday.getToDate() != null) {
                eventDate = eventDate + " to " + Utility.formatDateToString(holiday.getToDate().getTime());
            }
            if (eventDate != null) {
                holder.tvDate.setText("" + eventDate);
            }
            if (holiday.getFromDate().getTime() < new Date().getTime()) {
                holder.ivEditCalendar.setVisibility(View.GONE);
            } else {
                holder.ivEditCalendar.setVisibility(View.VISIBLE);
            }
            holder.ivEditCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogLayout = inflater.inflate(R.layout.dialog_edit_holiday, null);
                    TextView tvError = dialogLayout.findViewById(R.id.tvError);
                    EditText etEditEvent = dialogLayout.findViewById(R.id.etEditEvent);
                    etEditEvent.setText("" + holiday.getEvent());
                    ImageButton ibEditMic = dialogLayout.findViewById(R.id.ibEditMic);
                    etDescription = dialogLayout.findViewById(R.id.etDescription);
                    if (!TextUtils.isEmpty(holiday.getDescription())) {
                        etDescription.setText("" + holiday.getDescription());
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
                    cldr.setTime(holiday.getFromDate());
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
                    etEditFromDate.setOnClickListener(new View.OnClickListener() {
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
                    if (holiday.getToDate() != null) {
                        cldr.setTime(holiday.getToDate());
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
                    etEditToDate.setOnClickListener(new View.OnClickListener() {
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
                            .setTitleText("Edit Holiday")
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
                                            holiday.setEvent(event);
                                        }
                                    }
                                    String description = etDescription.getText().toString().trim();
                                    if (!TextUtils.isEmpty(description)) {
                                        if (Utility.isNumeric(description)) {
                                            etDescription.setError("Invalid description");
                                            etDescription.requestFocus();
                                            return;
                                        } else {
                                            holiday.setDescription(description);
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
                                            holiday.setToDate(fromDt);
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
                                            holiday.setToDate(toDt);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                            etEditToDate.setError("Date format dd/MM/yyyy");
                                            etEditToDate.requestFocus();
                                            return;
                                        }
                                        if (fromDt.after(toDt)) {
                                            tvError.setVisibility(View.VISIBLE);
                                            tvError.setText("To Date must be less than From Date");
                                            return;
                                        }
                                    } else {
                                        holiday.setToDate(toDt);
                                    }
                                    holiday.setModifiedDate(new Date());
                                    holiday.setModifierId(loggedInUserId);
                                    holiday.setModifierType("A");
                                    final SweetAlertDialog pDialog;
                                    pDialog = Utility.createSweetAlertDialog(getContext());
                                    pDialog.show();
                                    holidayCollectionRef.document(holiday.getId()).set(holiday).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                if (pDialog != null) {
                                                    pDialog.dismiss();
                                                }
                                                sDialog.dismissWithAnimation();
                                                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Holiday is updated")
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
                                    final SweetAlertDialog sweetAlertDialog;
                                    sweetAlertDialog = Utility.createSweetAlertDialog(getContext());
                                    sweetAlertDialog.show();
                                    holidayCollectionRef.document(holiday.getId())
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if (sweetAlertDialog != null) {
                                                        sweetAlertDialog.dismiss();
                                                    }
                                                    sDialog.dismissWithAnimation();
                                                    //getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
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
            return holidayList.size();
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

        description.delete(0, description.length());
        description.append(etDescription.getText().toString());
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    description.append(result.get(0)).append(" ");
                    etDescription.setText(description.toString());
                }
                break;
            }
        }
    }

}
