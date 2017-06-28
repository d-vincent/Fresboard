package biome.fresco.Fragments;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import biome.fresco.CustomTextViewLogo;
import biome.fresco.MainActivity;
import biome.fresco.ProjectDetailActivity;
import biome.fresco.Objects.ProjectListObject;
import biome.fresco.R;
import biome.fresco.SourceSansProBoldTextView;
import biome.fresco.SourceSansRegularTextView;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
import static biome.fresco.MainActivity.mAuth;
import static biome.fresco.MainActivity.mDatabase;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProjectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProjectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectFragment extends Fragment {




    private List<ProjectListObject> projects;
    RecyclerView projectListRecyclerView;
    ProjectListAdapter mAdapter;

    private static final int SCROLL_DIRECTION_UP = -1;

    private String mId;

    public ProjectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProjectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProjectFragment newInstance() {
        ProjectFragment fragment = new ProjectFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projects = new ArrayList<>();
        mId = mAuth.getCurrentUser().getUid();

        mDatabase.child("users").child(mId).child("projects").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String projectId = dataSnapshot.getKey();
                mDatabase.child("projects").child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ProjectListObject project = new ProjectListObject();
                        project.setName(dataSnapshot.child("name").getValue().toString());
                        project.setAuthorId(dataSnapshot.child("author").getValue().toString());
                        try {
                            project.setDescription(dataSnapshot.child("desc").getValue().toString());
                        }catch (Exception e){
                            project.setDescription("");
                        }
                        project.setProjectId(projectId);
                        project.setMemberCount(dataSnapshot.child("members").getChildrenCount());

                        projects.add(project);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_project, container, false);

        projectListRecyclerView = (RecyclerView)view.findViewById(R.id.project_list_recycler);
        projectListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ProjectListAdapter(projects);
        projectListRecyclerView.setAdapter(mAdapter);
        projectListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState== SCROLL_STATE_IDLE){
                    ObjectAnimator anim = ObjectAnimator.ofFloat(  ((MainActivity)getActivity()).getSupportActionBar(),"elevation",0);
                    anim.setDuration(250);
                    anim.start();

                }
                else{
                    ObjectAnimator anim = ObjectAnimator.ofFloat(  ((MainActivity)getActivity()).getSupportActionBar(),"elevation",8f);
                    anim.setDuration(250);
                    anim.start();
                }
            }
        });

// ...
// Put this into your RecyclerView.OnScrollListener > onScrolled() method



        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event




    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class ProjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private CustomTextViewLogo projectName;
        private SourceSansRegularTextView projectDescription;
        private SourceSansProBoldTextView projectMemberCount;
        private View entireProjectListView;


        public ProjectHolder(View itemView){
            super(itemView);
            projectName = (CustomTextViewLogo)itemView.findViewById(R.id.project_name);
            projectDescription = (SourceSansRegularTextView) itemView.findViewById(R.id.project_decsription);
            projectMemberCount = (SourceSansProBoldTextView)itemView.findViewById(R.id.project_member_count);
            entireProjectListView = itemView.findViewById(R.id.entire_project_view);



        }

        @Override
        public void onClick(View view) {

        }

        public void bindProject(ProjectListObject project){


            projectName.setText(project.getName());
            projectMemberCount.setText(project.getMemberCount().toString() + " Members");
            projectDescription.setText(project.getDescription());

        }
    }

    private class ProjectListAdapter extends RecyclerView.Adapter<ProjectHolder>{

        public List<ProjectListObject> mProjects;
        public ProjectListAdapter(List<ProjectListObject> projects){
            mProjects = projects;
        }

        @Override
        public void onBindViewHolder(ProjectHolder holder, final int position) {
            holder.bindProject(mProjects.get(position));

            holder.entireProjectListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        float x = motionEvent.getX();
                        float y = motionEvent.getY();

                        y += view.getHeight();

                        for (int i = 0; i < position; i++) {
                            y += view.getHeight();
                        }


                        Intent intent = new Intent(getContext(), ProjectDetailActivity.class);
                        intent.putExtra("xcoord", x);
                        intent.putExtra("ycoord", y);
                        intent.putExtra("projectId", mProjects.get(position).getProjectId());
                        startActivity(intent);
                        return false;
                    }
                    return true;
                }


            });
        }

        @Override
        public int getItemCount() {
            return mProjects.size();
        }

        @Override
        public ProjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(R.layout.list_item_project, parent, false);
            return new ProjectHolder(view);
        }
    }


}
