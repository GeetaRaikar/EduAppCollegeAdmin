package com.padmajeet.techforedu.wowcollege.admin;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.padmajeet.techforedu.wowcollege.admin.model.AcademicYear;
import com.padmajeet.techforedu.wowcollege.admin.model.Exam;
import com.padmajeet.techforedu.wowcollege.admin.model.ExamSeries;
import com.padmajeet.techforedu.wowcollege.admin.model.Institute;
import com.padmajeet.techforedu.wowcollege.admin.model.Staff;
import com.padmajeet.techforedu.wowcollege.admin.util.SessionManager;
import com.padmajeet.techforedu.wowcollege.admin.util.Utility;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentExam extends Fragment {
    private Staff loggedInUser;
    private Institute selectedInstitute;
    private ExamSeries selectedExamSeriesByExam;
    private String selectedExamSeries;
    private String currentExamSeries;
    private Gson gson;
    private View view;
    private Bundle bundle=new Bundle();
    private ListView lvExam;
    private LinearLayout llNoList;
    private ArrayList<Exam> examList;
    private AcademicYear selectedAcademicYear;
    private String date;
    private boolean isExamDate;
    private Date exam_date;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private String time;
    private Boolean isFromTime,isToTime;
    private Fragment currentFragment=this;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference examCollectionRef = db.collection("Exam");
    private ExamSeries examSeries;
    private Exam exam;
    private RecyclerView rvExam;
    private RecyclerView.Adapter examAdapter;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        String managementJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(managementJson,Staff.class);
        String selectedInstituteJson = sessionManager.getString("selectedInstitute");
        selectedInstitute = gson.fromJson(selectedInstituteJson, Institute.class);
        String selectedAcademicYearJson = sessionManager.getString("currentAcademicYear");
        selectedAcademicYear =gson.fromJson(selectedAcademicYearJson, AcademicYear.class);
        super.onCreate(savedInstanceState);
    }
    public FragmentExam() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_exam, container, false);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.exam));
        // Inflate the layout for this fragment
        bundle = getArguments();
        String selectedExamSeriesJson = bundle.getString("currentExamSeries");
        System.out.println("selectedExamSeriesJson - "+selectedExamSeriesJson);
        gson = Utility.getGson();
        selectedExamSeriesByExam = gson.fromJson(selectedExamSeriesJson,ExamSeries.class);
        currentExamSeries=gson.toJson(selectedExamSeriesByExam);
        lvExam = view.findViewById(R.id.lvExam);
        llNoList = view.findViewById(R.id.llNoList);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addExam);
        //fab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#mycolor")));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putString("currentExamSeries",currentExamSeries);
                FragmentAddExam fragmentAddExam = new FragmentAddExam();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentAddExam.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.contentLayout,fragmentAddExam).addToBackStack(null).commit();
            }
        });
        getExamList();
        return view;
    }
    private void  getExamList() {
        if(examList.size()!=0){
            examList.clear();
        }
        examCollectionRef
                .whereEqualTo("examSeriesId", selectedExamSeriesByExam.getId())
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            exam = documentSnapshot.toObject(Exam.class);
                            exam.setId(documentSnapshot.getId());
                            examList.add(exam);
                        }
                        if (examList.size() != 0) {
                            // System.out.println("event Size -"+eventList.size());
                            examAdapter = new ExamAdapter(examList);
                            rvExam.setAdapter(examAdapter);
                            rvExam.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        } else {
                            rvExam.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
    class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.MyViewHolder> {
        private List<Exam> examList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvDate, tvSubject, tvTime;
            public ImageView ivEditExamDate, ivEditExamTime;

            public MyViewHolder(View view) {
                super(view);
                tvDate = (TextView) view.findViewById(R.id.tvDate);
                tvSubject = (TextView) view.findViewById(R.id.tvSubject);
                tvTime = (TextView) view.findViewById(R.id.tvTime);
                ivEditExamDate = view.findViewById(R.id.ivEditExamDate);
                ivEditExamTime = view.findViewById(R.id.ivEditExamTime);
            }
        }


        public ExamAdapter(List<Exam> examList) {
            this.examList = examList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_event, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final Exam currentExam = examList.get(position);
            final String examDate =Utility.formatDateToString(currentExam.getDate().getTime());
            holder.tvDate.setText(""+examDate);
            holder.tvSubject.setText(""+currentExam.getSubjectId());
            final String examTime = currentExam.getFromHrs()+":"+currentExam.getFromMins()+" "+currentExam.getFromPeriod()+"-"+currentExam.getToHrs()+":"+currentExam.getToMins()+" "+currentExam.getToPeriod();
            holder.tvTime.setText(examTime);

            holder.ivEditExamDate.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    final EditText editText = new EditText(getContext());
                    editText.setText(examDate);
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit Date")
                            .setConfirmText("Update")
                            .setCustomView(editText)
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
                                    date = editText.getText().toString().trim();
                                    System.out.println("date" + date);
                                    if (TextUtils.isEmpty(date)) {
                                        editText.setError("Enter date");
                                        editText.requestFocus();
                                        return;
                                    }

                                    String[] examdate = date.split("/");
                                    int year = Integer.parseInt(examdate[2]);
                                    year = 2000 + year;
                                    date = examdate[0] + "/" + examdate[1] + "/" + year;


                                    if (Utility.isDateValid(date)) {
                                        isExamDate = true;
                                    } else {
                                        isExamDate = false;
                                        editText.setError("InCorrect Date");
                                        editText.requestFocus();
                                        return;
                                    }
                                    try {
                                        exam_date = dateFormat.parse(date);

                                    } catch (ParseException e) {
                                        editText.setError("DD/MM/YYYY");
                                        editText.requestFocus();
                                        e.printStackTrace();
                                    }
                                    if (isExamDate == true) {
                                        currentExam.setDate(exam_date);
                                        currentExam.setModifiedDate(new Date());
                                        currentExam.setModifierId(loggedInUser.getId());
                                        updateExam(currentExam);
                                    }


                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();


                }
            });
            holder.ivEditExamTime.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    //System.out.println("Edit clicked");
                    final EditText editText = new EditText(getContext());
                    editText.setText(examTime);
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Edit Date")
                            .setConfirmText("Update")
                            .setCustomView(editText)
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
                                    time = editText.getText().toString().trim();
                                    if (TextUtils.isEmpty(time)) {
                                        editText.setError("Enter Time");
                                        editText.requestFocus();
                                        return;
                                    }
                                    String[] Time = time.split("-");
                                    String fromTime = Time[0];
                                    String toTime = Time[1];

                                    if (Utility.isTimeValid(fromTime)) {
                                        isFromTime = true;
                                    } else {
                                        isFromTime = false;
                                        editText.setError("InCorrect Time");
                                        editText.requestFocus();
                                        return;

                                    }
                                    if (Utility.isTimeValid(toTime)) {
                                        isToTime = true;
                                    } else {
                                        isToTime = false;
                                        editText.setError("InCorrect Time");
                                        editText.requestFocus();
                                        return;
                                    }
                                    String[] hours = fromTime.split(":");
                                    int from_Hrs = Integer.parseInt(hours[0]);
                                    String[] minutes = hours[1].split(" ");
                                    int from_Mins = Integer.parseInt(minutes[0]);
                                    String from_period = minutes[1];

                                    String[] hrs = toTime.split(":");
                                    int to_Hrs = Integer.parseInt(hrs[0]);
                                    String[] mins = hours[1].split(" ");
                                    int to_Mins = Integer.parseInt(mins[0]);
                                    String to_period = mins[1];

                                    if(isFromTime==true && isToTime==true){
                                        currentExam.setFromHrs(from_Hrs);
                                        currentExam.setFromMins(from_Mins);
                                        currentExam.setFromPeriod(from_period);
                                        currentExam.setToPeriod(to_period);
                                        currentExam.setToMins(to_Mins);
                                        currentExam.setToHrs(to_Hrs);
                                        currentExam.setModifiedDate(new Date());
                                        currentExam.setModifierId(loggedInUser.getId());

                                        updateExam(currentExam);
                                    }


                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();


                }
            });

        }

        @Override
        public int getItemCount() {
            return examList.size();
        }
    }
    private void updateExam(final Exam currentExam) {
        Gson gson = Utility.getGson();
        final String examJson = gson.toJson(currentExam);
        examCollectionRef.document(currentExam.getId()).set(currentExam).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
                } else {
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Unable to edit exam")
                            .setConfirmText("Ok");
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
        });
    }
}
