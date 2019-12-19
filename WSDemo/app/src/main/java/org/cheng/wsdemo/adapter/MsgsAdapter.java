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
import org.cheng.wsdemo.bean.MsgUi;
import org.cheng.wsdemo.ui.MessageActivity;
import org.cheng.wsdemo.ui.MessagesActivity;
import org.cheng.wsdemo.ui.UserInfoActivity;

import java.util.List;

public class MsgsAdapter extends RecyclerView.Adapter<MsgsAdapter.ViewHolder> {

    private static final String TAG = "MsgAdapter";

    private Context mContext;

    private List<MsgUi> mMsgsList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView userImage;
        TextView name;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            userImage = (ImageView) view.findViewById(R.id.image);
            name = (TextView) view.findViewById(R.id.name);
        }
    }

    public MsgsAdapter(List<MsgUi> msgsList) {
        mMsgsList = msgsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.msgs_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                MsgUi msgUi = mMsgsList.get(position);
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra(MessageActivity.name, msgUi.getMsgname());
                intent.putExtra(MessageActivity.msgTo, msgUi.getMsgId());
                intent.putExtra(MessageActivity.receiverImage, msgUi.getMsgImageUrl());
                intent.putExtra(MessageActivity.msgType,msgUi.getMsgType().toString());
                mContext.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MsgUi MsgUi = mMsgsList.get(position);
        holder.name.setText(MsgUi.getMsgname());
        Glide.with(mContext).load(MsgUi.getMsgImageUrl()).into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        return mMsgsList.size();
    }
}
