package org.cheng.wsdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.bean.GroupInfo;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.ui.MessageActivity;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    private Context mContext;

    private List<GroupInfo> mGroupList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView  cardView;
        ImageView userImage;
        TextView  name;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            userImage = (ImageView) view.findViewById(R.id.image);
            name = (TextView) view.findViewById(R.id.name);
        }
    }

    public GroupsAdapter(List<GroupInfo> groupInfos) {
        mGroupList = groupInfos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.friend_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int      position = holder.getAdapterPosition();
                GroupInfo groupInfo = mGroupList.get(position);
                Intent   intent   = new Intent(mContext, MessageActivity.class);
                intent.putExtra(MessageActivity.name, groupInfo.getGname());
                intent.putExtra(MessageActivity.msgTo, groupInfo.getGid());
                intent.putExtra(MessageActivity.receiverImage, groupInfo.getHeadImageUrl());
                intent.putExtra(MessageActivity.msgType, MESSAGETYPE.GROUPCHAT.toString());
                mContext.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GroupInfo groupInfo = mGroupList.get(position);

        holder.name.setText(groupInfo.getGid());
        //Glide.with(mContext).load(groupInfo.getHeadImageUrl()).into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        return mGroupList.size();
    }
}
