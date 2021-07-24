package com.padmajeet.techforedu.wowcollege.admin;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import cn.pedant.SweetAlert.SweetAlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.techforedu.wowcollege.admin.model.AcademicYear;
import com.padmajeet.techforedu.wowcollege.admin.model.Batch;
import com.padmajeet.techforedu.wowcollege.admin.model.Institute;
import com.padmajeet.techforedu.wowcollege.admin.model.Period;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.model.Subject;
import com.padmajeet.techforedu.wowcollege.admin.model.SubjectFaculty;
import com.padmajeet.techforedu.wowcollege.admin.model.TimeTable;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAddTimeTable extends Fragment {

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference timeTableCollectionRef=db.collection("TimeTable");
    private CollectionReference periodCollectionRef=db.collection("Period");
    private CollectionReference batchCollectionRef=db.collection("Batch");
    private CollectionReference subjectCollectionRef=db.collection("Subject");
    private CollectionReference staffCollectionRef=db.collection("Staff");
    private Period period;
    private Batch batch;
    private Subject subject;
    private Staff staff;
    private TimeTable timeTable;
    private ArrayList<Batch> batchList=new ArrayList<>();
    private ArrayList<Period> periodList=new ArrayList<>();
    private ArrayList<Subject> subjectList=new ArrayList<>();
    private ArrayList<Staff> facultyList1=new ArrayList<>();
    private ArrayList<Staff> facultyList=new ArrayList<>();
    private Spinner spBatch,spDay,spSubject,spStaff,spPeriod;
    private Button btnSave;
    private SweetAlertDialog pDialog;
    private View view = null;
    private Staff loggedInUser;
    private Institute selectedInstitute;
    private Gson gson;
    private Batch selectedBatch;
    private SubjectFaculty selectedSubjectFaculty;
    private Period selectedPeriod;
    private String selectedDay;
    private AcademicYear selectedAcademicYear;
    private Subject selectedSubject;
    private Staff selectedStaff;
    private SessionManager sessionManager;

    public FragmentAddTimeTable() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager= new SessionManager(getContext());
        String adminJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(adminJson,Staff.class);
        String selectedInstituteJson = sessionManager.getString("selectedInstitute");
        selectedInstitute = gson.fromJson(selectedInstituteJson, Institute.class);
        super.onCreate(savedInstanceState);
        String selectedAcademicYearJson = sessionManager.getString("currentAcademicYear");
        selectedAcademicYear = gson.fromJson(selectedAcademicYearJson, AcademicYear.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_time_table, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.timetable));

        spDay =view.findViewById(R.id.spDay);
        spBatch = view.findViewById(R.id.spBatch);
        spSubject =view.findViewById(R.id.spSubject);
        spStaff=view.findViewById(R.id.spStaff);
        spPeriod =view.findViewById(R.id.spPeriod);
        getFaculties();
        getBatchesOfInstitute();
        // Spinner Drop down elements
        final List<String> days = new ArrayList<String>();
        //days.add("Sunday");
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
        days.add("Saturday");
        ArrayAdapter<String> dayAdaptor = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, days);
        dayAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDay.setAdapter(dayAdaptor);

        spDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDay = days.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeTable = new TimeTable();
                timeTable.setPeriodId(selectedPeriod.getId());
                timeTable.setSubjectId(selectedSubject.getId());
                timeTable.setStaffId(selectedStaff.getId());
                timeTable.setBatchId(selectedBatch.getId());
                timeTable.setDay(selectedDay);
                timeTable.setAcademicYearId(selectedAcademicYear.getId());
                timeTable.setCreatorId(loggedInUser.getId());
                timeTable.setModifierId(loggedInUser.getId());
                timeTable.setStatus("A");
                timeTable.setCreatorType("A");
                timeTable.setModifierType("A");
                addTimeTable();
            }
        });
        return view;
    }

    private void addTimeTable(){
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Batch successfully added")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        timeTableCollectionRef
                                .add(timeTable)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        FragmentTimeTable fragmentTimeTable = new FragmentTimeTable();
                                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                        fragmentManager.beginTransaction().replace(R.id.contentLayout,fragmentTimeTable).addToBackStack(null).commit();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Log.w(TAG, "Error adding document", e);
                                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                                    }
                                });
                        // [END add_document]
                    }
                });
        dialog.setCancelable(false);
        dialog.show();

    }
    private void  getBatchesOfInstitute() {
        pDialog = Utility.createSweetAlertDialog(getContext());
        pDialog.show();
        batchCollectionRef
                .whereEqualTo("instituteId",selectedInstitute.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            System.out.println("Data -key -" + document.getId() + " value -" + document.getData());
                            batch = document.toObject(Batch.class);
                            batch.setId(document.getId());
                            System.out.println("Data -key -" + document.getId() + " value -" + batch);
                            System.out.println("Batch -" + batch.getName());
                            batchList.add(batch);
                        }
                        if (batchList.size()!=0) {
                            List<String> batchNameList = new ArrayList<>();
                            for(Batch batch:batchList){
                                batchNameList.add(batch.getName());
                            }
                            ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                            batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spBatch.setAdapter(batchAdaptor);

                            spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedBatch = batchList.get(position);
                                    getPeriodsOfBatch();
                                    getSubjectOfBatch();
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        } else {
                            spBatch.setEnabled(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        spBatch.setEnabled(false);
                    }
                });
    }
    private void  getPeriodsOfBatch() {
        pDialog = Utility.createSweetAlertDialog(getContext());
        pDialog.show();
        periodCollectionRef
                .whereEqualTo("batchId",selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            System.out.println("Data -key -" + document.getId() + " value -" + document.getData());
                            period = document.toObject(Period.class);
                            period.setId(document.getId());
                            System.out.println("Data -key -" + document.getId() + " value -" + period);
                            System.out.println("Batch -" + period.getNumber());
                            periodList.add(period);
                        }
                        if (periodList.size()!=0) {
                            List<String> periodNameList = new ArrayList<>();
                            for(Period period:periodList){
                                periodNameList.add(period.getNumber()+" - "+period.getFromTime()+"-"+period.getToTime());
                            }
                            ArrayAdapter<String> periodAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, periodNameList);
                            periodAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spPeriod.setAdapter(periodAdaptor);
                            spPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedPeriod = periodList.get(position);
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        } else {
                            spPeriod.setEnabled(false);
                        }
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        spPeriod.setEnabled(false);
                    }
                });
    }
    private void  getSubjectOfBatch() {
        pDialog = Utility.createSweetAlertDialog(getContext());
        pDialog.show();
        subjectCollectionRef
                .whereEqualTo("batchId",selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            System.out.println("Data -key -" + document.getId() + " value -" + document.getData());
                            period = document.toObject(Period.class);
                            period.setId(document.getId());
                            System.out.println("Data -key -" + document.getId() + " value -" + period);
                            System.out.println("Batch -" + period.getNumber());
                            periodList.add(period);
                        }
                        if (periodList.size()!=0) {

                            List<String> subjectNameList = new ArrayList<>();
                            for(Subject subject:subjectList){
                                subjectNameList.add(subject.getName());
                            }
                            ArrayAdapter<String> subjectAdaptor = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, subjectNameList);
                            subjectAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spSubject.setAdapter(subjectAdaptor);
                            spSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedSubject = subjectList.get(position);
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        } else {
                            spSubject.setEnabled(false);
                        }
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        spSubject.setEnabled(false);
                    }
                });
    }
    private void getFaculties() {
        pDialog = Utility.createSweetAlertDialog(getContext());
        pDialog.show();
        staffCollectionRef
                .whereEqualTo("instituteId", selectedInstitute.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            System.out.println("Data -key -" + document.getId() + " value -" + document.getData());
                            staff = document.toObject(Staff.class);
                            staff.setId(document.getId());
                            System.out.println("Data -key -" + document.getId() + " value -" + staff);
                            System.out.println("Batch -" + staff.getFirstName());
                            periodList.add(period);
                        }
                        if (periodList.size() != 0) {
                            Staff facultyTemp = new Staff();
                            facultyTemp.setMobileNumber("10");
                            facultyTemp.setFirstName("");
                            facultyTemp.setCreatorId(loggedInUser.getId());
                            facultyTemp.setJoiningYear(0);
                            facultyTemp.setId(selectedStaff.getId());
                            facultyTemp.setModifierId(loggedInUser.getId());
                            facultyList.add(facultyTemp);
                            facultyList.addAll(1, facultyList1);

                            List<String> facultyNameList = new ArrayList<>();
                            facultyNameList.add("NONE");
                            for (Staff faculty : facultyList1) {
                                facultyNameList.add(faculty.getFirstName() + " " + faculty.getLastName());
                            }
                            ArrayAdapter<String> facultyAdaptor = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, facultyNameList);
                            facultyAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spStaff.setAdapter(facultyAdaptor);
                            spStaff.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedStaff = facultyList.get(position);
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        } else {
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

}
