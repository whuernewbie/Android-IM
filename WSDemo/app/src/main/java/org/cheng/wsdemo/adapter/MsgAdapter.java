package org.cheng.wsdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.bean.Msgbean;
import org.cheng.wsdemo.ui.UserInfoActivity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {

    private List<Msgbean> mMsgList;

    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftLayout;

        LinearLayout rightLayout;

        CircleImageView leftImage;

        CircleImageView rightImage;

        TextView leftMsg;

        TextView rightMsg;

        TextView leftName;

        TextView rightName;

        public ViewHolder(View view) {
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            leftMsg = (TextView) view.findViewById(R.id.left_msg);
            rightMsg = (TextView) view.findViewById(R.id.right_msg);
            leftImage=(CircleImageView)view.findViewById(R.id.left_head_image);
            rightImage=(CircleImageView)view.findViewById(R.id.right_head_image);
            leftName=(TextView)view.findViewById(R.id.left_name);
            rightName=(TextView)view.findViewById(R.id.right_name);
        }
    }

    public MsgAdapter(List<Msgbean> msgList) {
        mMsgList = msgList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item, parent, false);
        final MsgAdapter.ViewHolder holder = new MsgAdapter.ViewHolder(view);
        holder.leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Msgbean msg=mMsgList.get(position);

                Intent intent=new Intent(mContext, UserInfoActivity.class);
                intent.putExtra(UserInfoActivity.NAME,msg.getName());
                intent.putExtra(UserInfoActivity.IMAGE_ID,msg.getImage());
                intent.putExtra(UserInfoActivity.ID,msg.getId());
                mContext.startActivity(intent);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Msgbean msg = mMsgList.get(position);
        if (msg.getType() == Msgbean.TYPE_RECEIVED) {
            // 如果是收到的消息，则显示左边的消息布局，将右边的消息布局隐藏
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
            holder.leftName.setText(msg.getName()+msg.getId());
        } else if(msg.getType() == Msgbean.TYPE_SENT) {
            // 如果是发出的消息，则显示右边的消息布局，将左边的消息布局隐藏
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.getContent());
            holder.rightName.setText(msg.getName()+msg.getId());
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

}

