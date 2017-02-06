package biome.fresco;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static biome.fresco.MainActivity.mAuth;
import static biome.fresco.MainActivity.mDatabase;

/**
 * Created by Drew McDonald on 1/9/2017.
 */

public class SelectGroupDialog extends DialogFragment {

    private GroupAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private List<String> mGroupNames;

    public String uid;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = mAuth.getCurrentUser().getUid();
        mGroupNames = new ArrayList<>();

        mDatabase.child("users").child(uid).child("groups").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mGroupNames.add(dataSnapshot.getKey());
                mAdapter.notifyDataSetChanged();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_group_dialog, container);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.group_recycler);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new GroupAdapter(mGroupNames);

        return view;
    }

    private class GroupHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView contactName;
        private ImageView contactImage;
        public GroupHolder(View itemView){
            super(itemView);
            contactName = (TextView)itemView.findViewById(R.id.dm_name);
            contactImage = (ImageView)itemView.findViewById(R.id.contact_image);


        }

        @Override
        public void onClick(View view) {

        }

        public void bindName(String groupName){

            contactName.setText(groupName);

        }
    }

    private class GroupAdapter extends RecyclerView.Adapter<GroupHolder>{

        public List<String> mGroups;
        public GroupAdapter(List<String> groups){
            mGroups = groups;
        }

        @Override
        public void onBindViewHolder(GroupHolder holder, final int position) {
            holder.bindName(mGroups.get(position));
//            Picasso.with(getContext())
//                    .load(mChats.get(position).getToUserImageUrl())
//                    .transform(new CircleTransformation()).fit()
//                    .centerCrop()
////                    .placeholder(R.mipmap.contact)
//                    .into(holder.contactImage, new Callback() {
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

            holder.contactName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DatabaseReference groupChat =  mDatabase.child("groups").child(mGroups.get(position)).child("chats").push();
                    final String groupChatId = groupChat.getKey();
                    groupChat.setValue(true);

                    final List<String> groupUserIds = new ArrayList<String>();
                    mDatabase.child("groups").child(mGroups.get(position)).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            HashMap<String, Object> chat = new HashMap<String, Object>();
                            chat.put("created",System.currentTimeMillis());
                            chat.put("group", mGroupNames.get(position));
                            HashMap<String, Object> members = new HashMap<String, Object>();

                            for(DataSnapshot snap:dataSnapshot.getChildren()){
                                groupUserIds.add(snap.getKey());
//                                HashMap<String, Long> role = new HashMap<String, Long>();
//                                if (snap.getKey().equals(uid)){
//                                    role.put("role", 1L);
//                                }else {
//                                    role.put("role", 0L);
//                                }
                                members.put(snap.getKey(), true);
                            }
                            chat.put("members", members);
                            chat.put("name",groupChatId);
                            mDatabase.child("chats").child(groupChatId).setValue(chat);

                            for (String user:groupUserIds){
                                HashMap<String, Boolean> chatThing = new HashMap<String, Boolean>();
                                chatThing.put("notified", false);
                                chatThing.put("unread",false);
                                mDatabase.child("users").child(user).child("chats").child(groupChatId).setValue(chatThing);
                            }

                            //TODO chat has been created, head over to chat window
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

//                    FragmentManager fm = getActivity().getSupportFragmentManager();
//                    fm.beginTransaction().replace(R.id.container, DirectMessage.newInstance(mChats.get(position).getRoomId(), mChats.get(position).getToUserImageUrl(), mChats.get(position).getToUserId())).addToBackStack("").commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mGroups.size();
        }

        @Override
        public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(R.layout.list_item_direct_message, parent, false);
            return new GroupHolder(view);
        }
    }

}
