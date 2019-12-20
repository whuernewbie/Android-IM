package org.cheng.wsdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.ui.MessageActivity;
import org.cheng.wsdemo.util.FakeDataUtil;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupAdapter extends RecyclerView.Adapter<CreateGroupAdapter.ViewHolder> {

    private static final String TAG = "MsgAdapter";

    private Context mContext;

    private List<Boolean> booleanList=new ArrayList<>();

    private  String [] result=new String[FakeDataUtil.MaxGroupNum];

    private List<UserInfo> mFriendsList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView  cardView;
        ImageView userImage;
        TextView  name;
        CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            checkBox=(CheckBox) view.findViewById(R.id.choose);
            cardView = (CardView) view;
            userImage = (ImageView) view.findViewById(R.id.image);
            name = (TextView) view.findViewById(R.id.name);
        }
    }

    public CreateGroupAdapter(List<UserInfo> msgsList,Context context) {
        mContext=context;
        mFriendsList = msgsList;
        for (int i = 0; i < msgsList.size(); i++) {
            //设置默认的显示
            booleanList.add(false);
        }
        notifyDataSetChanged();
    }

    public void addData(List<UserInfo> userInfos){
        mFriendsList.addAll(userInfos);
        for (int i = 0; i < userInfos.size(); i++) {
            booleanList.add(false);
        }
        notifyDataSetChanged();
    }

    public void initCheck(boolean flag){
        for (int i = 0; i < mFriendsList.size(); i++) {
            //更改指定位置的数据
            booleanList.set(i,flag);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.create_group_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        UserInfo userInfo = mFriendsList.get(position);
        holder.name.setText(userInfo.getUname());
        Glide.with(mContext).load(userInfo.getImageUrl()).into(holder.userImage);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleanList.set(position,isChecked);
            }
        });

        holder.checkBox.setChecked(booleanList.get(position));
    }

    @Override
    public int getItemCount() {
        return mFriendsList.size();
    }

    //清空所有数据
    public void deleteAllData() {

        mFriendsList.clear();
        booleanList.clear();
        notifyDataSetChanged();

    }

    public String [] ChooseSelectData() {

        int y=0;
        for (int i = 0; i < mFriendsList.size(); i++) {
            if(booleanList.get(i)!=null && booleanList.get(i) ) {
                result[y]=mFriendsList.get(i).getUid();
                mFriendsList.remove(i);
                y++;
                i--;
            }
        }
        notifyDataSetChanged();
        if(y==0){
            //TODO 提示未选择数据
        }
        return result;
    }

    public void selectAll(){
        initCheck(true);
        notifyDataSetChanged();
    }
    public void unSelectAll(){
        initCheck(false);
        notifyDataSetChanged();
    }

}
