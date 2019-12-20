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
import org.cheng.wsdemo.bean.DynamicBean;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.ui.MessageActivity;
import org.cheng.wsdemo.ui.UserInfoActivity;
import org.w3c.dom.Text;

import java.util.List;

public class DynamicAdapter extends RecyclerView.Adapter<DynamicAdapter.ViewHolder> {

    private static final String TAG = "DynamicAdapter";

    private Context mContext;

    private List<DynamicBean> DynamicList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView  cardView;
        ImageView userImage;
        TextView  name;

        TextView context;
        TextView location;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            context=(TextView) view.findViewById(R.id.context);
            location=(TextView) view.findViewById(R.id.location);
            userImage = (ImageView) view.findViewById(R.id.image);
            name = (TextView) view.findViewById(R.id.name);
        }
    }

    public DynamicAdapter(List<DynamicBean> dynamicBeans) {
        DynamicList = dynamicBeans;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.dynamic_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int  position = holder.getAdapterPosition();
                DynamicBean dynamicBean=DynamicList.get(position);

                Intent intent=new Intent(mContext, UserInfoActivity.class);

                intent.putExtra(UserInfoActivity.IMAGE_ID,dynamicBean.getHeadImageUrl());
                intent.putExtra(UserInfoActivity.NAME,dynamicBean.getUname());
                intent.putExtra(UserInfoActivity.ID,dynamicBean.getUid());
                mContext.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DynamicBean dynamicBean = DynamicList.get(position);

        holder.context.setText(dynamicBean.getContext());
        holder.location.setText(dynamicBean.getAddress());
        holder.name.setText(dynamicBean.getUname());
        Glide.with(mContext).load(dynamicBean.getHeadImageUrl()).into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        return DynamicList.size();
    }
}
