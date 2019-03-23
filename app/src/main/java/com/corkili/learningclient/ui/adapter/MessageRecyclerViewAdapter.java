package com.corkili.learningclient.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.corkili.learningclient.R;
import com.corkili.learningclient.common.IUtils;
import com.corkili.learningclient.generate.protobuf.Info.MessageInfo;
import com.corkili.learningclient.generate.protobuf.Info.UserInfo;

import java.util.Date;
import java.util.List;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final List<MessageInfo> messageInfos;
    private final UserInfo self;

    public MessageRecyclerViewAdapter(Context context, List<MessageInfo> messageInfos, UserInfo self) {
        this.context = context;
        this.messageInfos = messageInfos;
        this.self = self;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_message_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = messageInfos.get(position);
        if (holder.mItem.getSenderInfo().getUserId() == self.getUserId()) {
            holder.leftTimeView.setVisibility(View.GONE);
            holder.leftUsernameView.setVisibility(View.GONE);
            holder.leftContentView.setVisibility(View.GONE);
            holder.rightTimeView.setVisibility(View.VISIBLE);
            holder.rightUsernameView.setVisibility(View.VISIBLE);
            holder.rightContentView.setVisibility(View.VISIBLE);

            holder.rightTimeView.setText(IUtils.format("{}",
                    IUtils.DATE_TIME_FORMATTER.format(new Date(holder.mItem.getCreateTime()))));
            holder.rightUsernameView.setText("我");
            holder.rightContentView.setText(holder.mItem.getText());
        } else {
            holder.leftTimeView.setVisibility(View.VISIBLE);
            holder.leftUsernameView.setVisibility(View.VISIBLE);
            holder.leftContentView.setVisibility(View.VISIBLE);
            holder.rightTimeView.setVisibility(View.GONE);
            holder.rightUsernameView.setVisibility(View.GONE);
            holder.rightContentView.setVisibility(View.GONE);

            holder.leftTimeView.setText(IUtils.format("{}",
                    IUtils.DATE_TIME_FORMATTER.format(new Date(holder.mItem.getCreateTime()))));
            holder.leftUsernameView.setText(holder.mItem.getSenderInfo().getUsername());
            holder.leftContentView.setText(holder.mItem.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messageInfos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView leftTimeView;
        private final TextView leftUsernameView;
        private final TextView leftContentView;
        private final TextView rightTimeView;
        private final TextView rightUsernameView;
        private final TextView rightContentView;
        private MessageInfo mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            leftTimeView = view.findViewById(R.id.item_time_left);
            leftUsernameView = view.findViewById(R.id.item_username_left);
            leftContentView = view.findViewById(R.id.item_content_left);
            rightTimeView = view.findViewById(R.id.item_time_right);
            rightUsernameView = view.findViewById(R.id.item_username_right);
            rightContentView = view.findViewById(R.id.item_content_right);
        }

        public MessageInfo getMessageInfo() {
            return mItem;
        }
    }

}

