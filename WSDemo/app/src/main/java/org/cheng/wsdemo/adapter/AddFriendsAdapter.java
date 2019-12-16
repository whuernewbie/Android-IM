package org.cheng.wsdemo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.bean.AddFriendsBean;

import java.util.List;


public class AddFriendsAdapter extends RecyclerView.Adapter<AddFriendsAdapter.MyViewHolder> implements View.OnClickListener {

    private List<AddFriendsBean> list;//数据源
    private Context              context;//上下文

    public AddFriendsAdapter(List<AddFriendsBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_friend_item, parent, false);
        return new MyViewHolder(view);
    }

    //绑定
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AddFriendsBean data = list.get(position);
        holder.ivIcon.setBackgroundResource(R.drawable.icon);
        //TODO 判断发出还是接受
        holder.tvUsername.setText(data.getFrom());
        //holder.tvMessage.setText(data.getMessage());

        holder.itemView.setTag(position);
        holder.btnAgree.setTag(position);
        holder.btnRefuse.setTag(position);
    }


    //有多少个item
    @Override
    public int getItemCount() {
        return list.size();
    }


    //创建MyViewHolder继承RecyclerView.ViewHolder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView  tvUsername, tvMessage;
        private Button btnAgree, btnRefuse;

        public MyViewHolder(View itemView) {
            super(itemView);

            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvMessage = itemView.findViewById(R.id.tv_message);
            btnAgree = itemView.findViewById(R.id.btn_agree);
            btnRefuse = itemView.findViewById(R.id.btn_refuse);
            // 为ItemView添加点击事件
            itemView.setOnClickListener(AddFriendsAdapter.this);
            btnAgree.setOnClickListener(AddFriendsAdapter.this);
            btnRefuse.setOnClickListener(AddFriendsAdapter.this);
        }

    }


    //=======================以下为item中的button控件点击事件处理===================================

    //item里面有多个控件可以点击
    public enum ViewName {
        ITEM,
        PRACTISE
    }

    //自定义一个回调接口来实现Click和LongClick事件
    public interface OnItemClickListener {
        void onItemClick(View v, ViewName viewName, int position);

        void onItemLongClick(View v);
    }

    private OnItemClickListener mOnItemClickListener;//声明自定义的接口

    //定义方法并暴露给外面的调用者
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag(); //getTag()获取数据
        if (mOnItemClickListener != null) {
            switch (v.getId()) {
                case R.id.rv_recyclerView:
                    mOnItemClickListener.onItemClick(v, ViewName.PRACTISE, position);
                    break;
                default:
                    mOnItemClickListener.onItemClick(v, ViewName.ITEM, position);
                    break;
            }
        }
    }
}