package biome.fresnotes.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import biome.fresnotes.Objects.MilestoneObject;
import biome.fresnotes.Objects.TaskObject;
import biome.fresnotes.ProjectDetailActivity;
import biome.fresnotes.R;
import biome.fresnotes.SourceSansRegularTextView;

import static biome.fresnotes.MainActivity.mAuth;
import static biome.fresnotes.MainActivity.mDatabase;
import static biome.fresnotes.ProjectDetailActivity.mProject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AgendaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AgendaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AgendaFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    String mId;
    List<MilestoneObject> mileStones;
    List<Entry> entries;
    private OnFragmentInteractionListener mListener;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");

    LineChart lineChart;

    long totalTasks;
    long totalMileStones;

    EditText newMilestoneTitle;
    EditText newMiletsoneDescription;
    TextView dueDate;
    ImageView addMilestone;
    ExpandableLayout milestoneDetails;
    SimpleDateFormat dueDateFormat = new SimpleDateFormat("yyyy-M-dd");

    Date newMilestoneDate;

    MilestoneListADapter mAdapter;
    RecyclerView mRecyclerview;

    public AgendaFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AgendaFragment newInstance() {
        AgendaFragment fragment = new AgendaFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = mAuth.getCurrentUser().getUid();
        mileStones = new ArrayList<>();
        entries = new ArrayList<>();


        mAdapter = new MilestoneListADapter(mileStones, getContext());

        mDatabase.child("agenda").child(((ProjectDetailActivity)getActivity()).projectId).child("milestones").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mileStones.clear();

                for (DataSnapshot milestoneSnap: dataSnapshot.getChildren()) {

                    final String milestoneKey = milestoneSnap.getKey();
                    boolean newMilestone = true;
                    for (MilestoneObject milestone : mileStones) {
                        if (milestone.getId() == milestoneKey) {
                            newMilestone = false;
                        }
                    }

                    final MilestoneObject milestone = new MilestoneObject();
                    milestone.setId(milestoneSnap.getKey());
                    milestone.setAuthor((String) milestoneSnap.child("author").getValue());
                    milestone.setDescription((String) milestoneSnap.child("desc").getValue());
                    milestone.setDueDate((String) milestoneSnap.child("dueDate").getValue());
                    milestone.setName((String) milestoneSnap.child("name").getValue());
                    milestone.setCreatedTimestamp((Long) milestoneSnap.child("created").getValue());
                    milestone.setLastEditedTimestamp((Long) milestoneSnap.child("editedTime").getValue());
                    final List<String> taskKeys = new ArrayList<String>();
                    for (DataSnapshot keySnap : milestoneSnap.child("tasks").getChildren()) {
                        taskKeys.add(keySnap.getKey());
                    }


                    for (String string : taskKeys) {
                        totalTasks++;

                        mDatabase.child("agenda").child(((ProjectDetailActivity) getActivity()).projectId).child("tasks").child(string).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot taskSnap) {
                                String deletedKey = taskSnap.getKey();

                                if (taskSnap.getValue() == null) {
                                    milestone.removeTask(deletedKey);
                                    mAdapter.notifyDataSetChanged();
                                    return;
                                }

                                TaskObject alreadyHasTask = milestone.getTask(deletedKey);

                                if (alreadyHasTask != null) {
                                    alreadyHasTask.setAuthor((String) taskSnap.child("author").getValue());
                                    alreadyHasTask.setId(taskSnap.getKey());
                                    alreadyHasTask.setName((String) taskSnap.child("name").getValue());
                                    alreadyHasTask.setCompleted((boolean) taskSnap.child("completed").getValue());
                                    try {
                                        alreadyHasTask.setCompletedTimestamp((long) taskSnap.child("completedChangedTimestamp").getValue());
                                    } catch (Exception e) {
                                        alreadyHasTask.setCreatedTimestamp(new Date().getTime());
                                    }
                                    alreadyHasTask.setCreatedTimestamp((long) taskSnap.child("created").getValue());

                                    //mAdapter.notifyDataSetChanged();
                                } else {

                                    TaskObject task = new TaskObject();
                                    task.setMilestoneId(milestoneKey);
                                    task.setAuthor((String) taskSnap.child("author").getValue());
                                    task.setId(taskSnap.getKey());
                                    task.setName((String) taskSnap.child("name").getValue());
                                    task.setCompleted((boolean) taskSnap.child("completed").getValue());
                                    try {
                                        task.setCompletedTimestamp((long) taskSnap.child("completedChangedTimestamp").getValue());
                                    } catch (Exception e) {
                                        task.setCreatedTimestamp(new Date().getTime());
                                    }
                                    task.setCreatedTimestamp((long) taskSnap.child("created").getValue());
                                    milestone.addTask(task);
                                    mAdapter.notifyDataSetChanged();
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }


                    mileStones.add(milestone);

                    //Collections.sort(mileStones);
                    mAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//        mDatabase.child("agenda").child(((ProjectDetailActivity)getActivity()).projectId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                totalTasks = 0;
//                totalMileStones = 0;
//                mileStones.clear();
//
//                for (final DataSnapshot milestoneSnap : dataSnapshot.child("milestones").getChildren()) {
//                    totalMileStones ++;
//                    MilestoneObject milestone = new MilestoneObject();
//                    milestone.setId(milestoneSnap.getKey());
//                    milestone.setAuthor((String)milestoneSnap.child("author").getValue());
//                    milestone.setDescription((String)milestoneSnap.child("desc").getValue());
//                    milestone.setDueDate((String)milestoneSnap.child("dueDate").getValue());
//                    milestone.setName((String)milestoneSnap.child("name").getValue());
//                    milestone.setCreatedTimestamp((Long)milestoneSnap.child("created").getValue());
//                    milestone.setLastEditedTimestamp((Long)milestoneSnap.child("editedTime").getValue());
//                    List<TaskObject> taskObjects = new ArrayList<TaskObject>();
//                    List<String> taskKeys = new ArrayList<String>();
//                    for (DataSnapshot keySnap : milestoneSnap.child("tasks").getChildren()){
//                        taskKeys.add(keySnap.getKey());
//                    }
//                    long childCount = milestoneSnap.child("tasks").getChildrenCount();
//
//                    for (String string: taskKeys ){
//                        totalTasks++;
//                        DataSnapshot taskSnap = dataSnapshot.child("tasks").child(string);
//
//                        TaskObject task = new TaskObject();
//                        task.setMilestoneId(milestoneSnap.getKey());
//                        task.setAuthor((String)taskSnap.child("author").getValue());
//                        task.setId(taskSnap.getKey());
//                        task.setName((String)taskSnap.child("name").getValue());
//                        task.setCompleted((boolean)taskSnap.child("completed").getValue());
//                        try {
//                            task.setCompletedTimestamp((long) taskSnap.child("completedChangedTimestamp").getValue());
//                        }catch (Exception e){
//                            task.setCreatedTimestamp(new Date().getTime());
//                        }
//                        task.setCreatedTimestamp((long)taskSnap.child("created").getValue());
//                        taskObjects.add(task);
//
////                        counter ++;
////                        if (counter == childCount){
////
////                        }
//
//                    }
//                    milestone.setTasks(taskObjects);
//                    mileStones.add(milestone);
//
//                    //Collections.sort(mileStones);
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_agenda, container, false);

        //lineChart = (LineChart) view.findViewById(R.id.line_chart);
        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date((long)value);
                String label =dateFormat.format(date);
                return label;
            }

        };

        milestoneDetails = (ExpandableLayout)view.findViewById(R.id.milestone_details);
        newMiletsoneDescription = (EditText)view.findViewById(R.id.description_input);
        addMilestone = (ImageView)view.findViewById(R.id.add_milestone);
        addMilestone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newMilestoneTitle.getText().toString().length() < 1){
                    Toast.makeText(getContext(), "Milestones must have a title", Toast.LENGTH_SHORT).show();
                    return;
                }

                String title = newMilestoneTitle.getText().toString();
                String description = newMiletsoneDescription.getText().toString();
                String date = "";
                if (newMilestoneDate!= null) {
                    date = dueDateFormat.format(newMilestoneDate);
                    newMilestoneDate = null;
                }
                newMilestoneTitle.setText("");
                newMiletsoneDescription.setText("");

                HashMap<String, Object> milestone = new HashMap<String, Object>();
                milestone.put("author", mAuth.getCurrentUser().getUid());
                milestone.put("created", System.currentTimeMillis());
                milestone.put("desc", description);
                milestone.put("name", title);
                milestone.put("editedTime", System.currentTimeMillis());
                if (!date.equals("")) {
                    milestone.put("dueDate", dueDate.getText().toString());
                }

                dueDate.setText("Tap to set a due date");

                mDatabase.child("agenda").child(mProject.getProjectId()).child("milestones").push().setValue(milestone);
                milestoneDetails.collapse();
            }
        });

        newMilestoneTitle = (EditText)view.findViewById(R.id.new_milestone_title);
        newMilestoneTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 0){
                    milestoneDetails.expand(true);
                }
                else {
                    milestoneDetails.collapse(true);
                }
            }
        });

        dueDate = (TextView)view.findViewById(R.id.due_date);
        dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                final int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        newMilestoneDate = new Date(year, month, dayOfMonth);
                        dueDate.setText(year + "-" + (month +1) + "-" + dayOfMonth);
                    }
                }, year, month, day);

                datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dueDate.setText("Tap to set a due date");
                    }
                });
                datePickerDialog.show();
            }
        });

//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
//        xAxis.setValueFormatter(formatter);

        mRecyclerview = (RecyclerView) view.findViewById(R.id.milestone_recycler);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerview.setAdapter(mAdapter);


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class MilestoneListADapter extends UltimateViewAdapter<MilestoneHolder> {

        public List<MilestoneObject> mMilestones;
        Context mContext;
        public MilestoneListADapter(List<MilestoneObject> stones, Context context){
            mMilestones = stones;
            mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
            return null;
        }

        @Override
        public MilestoneHolder newHeaderHolder(View view) {
            return null;
        }

        @Override
        public void onBindViewHolder(MilestoneHolder holder, int position, List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public long generateHeaderId(int position) {
            return 0;
        }

        @Override
        public MilestoneHolder newFooterHolder(View view) {
            return null;
        }

        @Override
        public int getAdapterItemCount() {
            return 0;
        }

        @Override
        public void onBindViewHolder(final MilestoneHolder holder, final int position) {

            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
//            holder.taskLayout.removeAllViews();
//            holder.setIsRecyclable(false);
//            for (final TaskObject issue: mMilestones.get(position).getTasks()){
//
//                View view = layoutInflater.inflate(R.layout.list_item_issue, holder.taskLayout, false);
//                ((TextView)view.findViewById(R.id.issue_title)).setText(issue.getName());
//                ((CheckBox)view.findViewById(R.id.issue_check_box)).setChecked(issue.isCompleted());
//                ((CheckBox)view.findViewById(R.id.issue_check_box)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        mDatabase.child("agenda").child(mProject.getProjectId()).child("tasks").child(issue.getId()).child("completed").setValue(isChecked);
//                        mDatabase.child("agenda").child(mProject.getProjectId()).child("tasks").child(issue.getId()).child("completedChangedTimestamp").setValue(System.currentTimeMillis());
//                    }
//                });
//                holder.taskLayout.addView(view);
//            }

            View newTaskView = layoutInflater.inflate(R.layout.add_new_issue, holder.taskLayout, true);
            final View newTask = newTaskView.findViewById(R.id.add_new_task);
            final View taskInput = newTaskView.findViewById(R.id.task_input_layout);

            final EditText taskName = (EditText) newTaskView.findViewById(R.id.task_input);
            View saveTask = newTaskView.findViewById(R.id.save_task);
            View cancelTask = newTaskView.findViewById(R.id.cancel_task);

            saveTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (taskName.getText().toString().length() > 0){
                        HashMap<String, Object> taskValue = new HashMap<String, Object>();
                        taskValue.put("author", mAuth.getCurrentUser().getUid());
                        taskValue.put("completed", false);
                        taskValue.put("completedChangedTimestamp", System.currentTimeMillis());
                        taskValue.put("created", System.currentTimeMillis());
                        taskValue.put("name", taskName.getText().toString());
                        taskValue.put("milestone", mMilestones.get(position).getId());

                        String taskKey = mDatabase.child("agendas").child(mProject.getProjectId()).child("tasks").push().getKey();
                        mDatabase.child("agenda").child(mProject.getProjectId()).child("tasks").child(taskKey).setValue(taskValue);
                        mDatabase.child("agenda").child(mProject.getProjectId()).child("milestones").child(mMilestones.get(position).getId()).child("tasks").child(taskKey).setValue(true);

                        taskName.setText("");
                        newTask.setVisibility(View.VISIBLE);
                        taskInput.setVisibility(View.GONE);

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                    }
                }
            });
            cancelTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    taskName.setText("");
                    newTask.setVisibility(View.VISIBLE);
                    taskInput.setVisibility(View.GONE);

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(newTask.getWindowToken(),0);
                }
            });

            newTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    taskName.requestFocus();
                    InputMethodManager imm = (InputMethodManager)   getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(taskName, InputMethodManager.SHOW_IMPLICIT);
                    newTask.setVisibility(View.GONE);
                    taskInput.setVisibility(View.VISIBLE);
                }
            });

            holder.daysLeftText.setTextColor(getResources().getColor(R.color.colorPrimary));

            holder.bindProject(mMilestones.get(position), getContext());

            int completed = 0;
            int total = mMilestones.get(position).getTasks().size();


            for (TaskObject task: mMilestones.get(position).getTasks()){
                if (task.isCompleted()){
                    completed ++;
                }
            }

            if (total == completed){
                holder.entireNoteLayout.setBackgroundColor(getResources().getColor(R.color.completed));
            }else {
                holder.entireNoteLayout.setBackgroundColor(getResources().getColor(R.color.white));
            }

            holder.entireNoteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    TaskFragment newFragment = TaskFragment.newInstance(mileStones.get(position).getId());
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
                }
            });
//            holder.viewExpander.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    if (holder.expandView.isExpanded()){
//                        holder.expandView.setExpanded(false, true);
//                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                        imm.hideSoftInputFromWindow(newTask.getWindowToken(),0);
//                    }else{
//                        holder.expandView.setExpanded(true, true);
//                    }
//
//                }
//            });

//            Picasso.with(mContext)
//                    .load(mMilestones.get(position).getAuthorPhotoUrl())
////                    .placeholder(R.mipmap.contact)
//                    .into(holder.authorImage, new Callback() {
//                        @Override
//                        public void onSuccess() {
//
//                        }
//
//                        @Override
//                        public void onError() {
//
//                        }
//                    });
//
//            holder.authorName.setText(mMilestones.get(position).getAuthorName());

        }

        @Override
        public int getItemCount() {
            return mMilestones.size();
        }

        @Override
        public MilestoneHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.list_item_milestone, parent, false);
            return new MilestoneHolder(view);
        }

        @Override
        public MilestoneHolder onCreateViewHolder(ViewGroup parent) {
            return null;
        }
    }


    public static class MilestoneHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        SourceSansRegularTextView milestoneTitle;
        SourceSansRegularTextView milestoneDescription;
        SourceSansRegularTextView daysLeftText;
        SourceSansRegularTextView taskCount;
//        View viewExpander;
//        ExpandableLayout expandView;
        LinearLayout taskLayout;
        public View entireNoteLayout;
        boolean isExpanded;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


        public MilestoneHolder(View itemView){
            super(itemView);
            isExpanded = false;
            milestoneTitle = (SourceSansRegularTextView) itemView.findViewById(R.id.milestone_title);
            milestoneDescription = (SourceSansRegularTextView) itemView.findViewById(R.id.milestone_description);
            taskCount = (SourceSansRegularTextView) itemView.findViewById(R.id.task_count);
            daysLeftText =(SourceSansRegularTextView) itemView.findViewById(R.id.days_left);
            entireNoteLayout = itemView.findViewById(R.id.entire_milestone);
            //expandView = (ExpandableLayout) itemView.findViewById(R.id.issue_expander);
            taskLayout = (LinearLayout) itemView.findViewById(R.id.add_tasks_here);
            //viewExpander = itemView.findViewById(R.id.main_milestone_stuff);

        }

        @Override
        public void onClick(View view) {

        }

        public void bindProject(final MilestoneObject milestoneObject, final Context context){

            milestoneTitle.setText(milestoneObject.getName());
            milestoneDescription.setText(milestoneObject.getDescription());

            CalendarDay today = CalendarDay.from(new Date());
            Date dueDate;
            try {
                dueDate = dateFormat.parse(milestoneObject.getDueDate());
            }catch (Exception e){
                dueDate = new Date();
            }
            CalendarDay dueDay = CalendarDay.from(dueDate);

            long msDiff = today.getDate().getTime() - dueDay.getDate().getTime();
            long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);

            int completed = 0;
            int total = milestoneObject.getTasks().size();

            for (TaskObject task: milestoneObject.getTasks()){
                if (task.isCompleted()){
                    completed ++;
                }
            }

            if (total == completed){
                daysLeftText.setText("Completed");
            }else if (today.isAfter(dueDay)) {
                daysLeftText.setTextColor(context.getResources().getColor(R.color.errorColor));
                daysLeftText.setText(Long.toString(daysDiff) + " days overdue");
            }else if((today.equals(dueDay))) {
                daysLeftText.setText("Due today");
            }
            else if (today.isBefore(dueDay)){
                daysLeftText.setText(Long.toString(-daysDiff) + " days left");
            }
            else {
                daysLeftText.setTextColor(context.getResources().getColor(R.color.errorColor));
                daysLeftText.setText("Overdue");
            }


            taskCount.setText(completed + "/" + total);





//            noteName.setText(note.getTitle());
//            responseCount.setText(Integer.toString(note.getResponses().size()));
//            noteContent.setText(Html.fromHtml(note.getContent()));
//
//
//            timeDateStamp.setText(timeFormat.format(new Date(note.getLastEdited())));



        }
    }

}
