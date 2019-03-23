package com.corkili.learningclient.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.corkili.learningclient.R;
import com.corkili.learningclient.common.IntentParam;
import com.corkili.learningclient.generate.protobuf.Info.MessageInfo;
import com.corkili.learningclient.generate.protobuf.Info.UserInfo;
import com.corkili.learningclient.generate.protobuf.Response.MessageCreateResponse;
import com.corkili.learningclient.generate.protobuf.Response.MessageFindAllResponse;
import com.corkili.learningclient.service.MessageService;
import com.corkili.learningclient.service.ServiceResult;
import com.corkili.learningclient.ui.adapter.MessageRecyclerViewAdapter;
import com.corkili.learningclient.ui.other.MyRecyclerViewDivider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private EditText contentEditor;
    private Button sendContentButton;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private UserInfo userInfo;
    private UserInfo selfUserInfo;

    private List<MessageInfo> messageInfos;

    private MessageRecyclerViewAdapter recyclerViewAdapter;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        userInfo = (UserInfo) getIntent().getSerializableExtra(IntentParam.USER_INFO);
        selfUserInfo = (UserInfo) getIntent().getSerializableExtra(IntentParam.SELF_USER_INFO);
        if (userInfo == null || selfUserInfo == null) {
            throw new RuntimeException("Intent param lost");
        }
        messageInfos = new ArrayList<>();
        int count = getIntent().getIntExtra(IntentParam.COUNT, 0);
        for (int i = 0; i < count; i++) {
            messageInfos.add((MessageInfo) getIntent().getSerializableExtra(IntentParam.MESSAGE_INFO + i));
        }
        Collections.sort(messageInfos, (o1, o2) -> {
            if (o1.getCreateTime() == o2.getCreateTime()) {
                return 0;
            } else {
                return o1.getCreateTime() < o2.getCreateTime() ? -1 : 1;
            }
        });
        contentEditor = findViewById(R.id.content_editor);
        sendContentButton = findViewById(R.id.send_content);

        sendContentButton.setOnClickListener(v -> MessageService.getInstance().createMessage(
                handler, contentEditor.getText().toString().trim(),
                selfUserInfo.getUserId(), userInfo.getUserId()));

        recyclerView = findViewById(R.id.activity_message_list);
        swipeRefreshLayout = findViewById(R.id.activity_message_swipe_refresh_layout);
        recyclerViewAdapter = new MessageRecyclerViewAdapter(this, messageInfos, selfUserInfo);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addItemDecoration(new MyRecyclerViewDivider(this, LinearLayoutManager.HORIZONTAL,
                1,ContextCompat.getColor(this,R.color.colorBlack)));
        swipeRefreshLayout.setOnRefreshListener(this::refreshMessageInfos);

        setTitle(userInfo.getUsername());

    }

    private void refreshMessageInfos() {
        MessageService.getInstance().findAllMessage(handler, selfUserInfo.getUserId(), null, true);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MessageService.FIND_ALL_MESSAGE_MSG) {
                handleFindAllMessageMsg(msg);
            } else if (msg.what == MessageService.CREATE_MESSAGE_MSG) {
                handleCreateMessageMsg(msg);
            }
        }
    };

    private void handleFindAllMessageMsg(Message msg) {
        ServiceResult serviceResult = (ServiceResult) msg.obj;
        Toast.makeText(this, serviceResult.msg(), Toast.LENGTH_SHORT).show();
        if (serviceResult.isSuccess()) {
            messageInfos.clear();
            messageInfos.addAll(serviceResult.extra(MessageFindAllResponse.class).getMessageInfoList());
            Collections.sort(messageInfos, (o1, o2) -> {
                if (o1.getCreateTime() == o2.getCreateTime()) {
                    return 0;
                } else {
                    return o1.getCreateTime() < o2.getCreateTime() ? -1 : 1;
                }
            });
            swipeRefreshLayout.setRefreshing(false);
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private void handleCreateMessageMsg(Message msg) {
        ServiceResult serviceResult = (ServiceResult) msg.obj;
        Toast.makeText(this, serviceResult.msg(), Toast.LENGTH_SHORT).show();
        if (serviceResult.isSuccess()) {
            contentEditor.setText("");
            MessageInfo messageInfo = serviceResult.extra(MessageCreateResponse.class).getMessageInfo();
            if (messageInfo != null) {
                messageInfos.add(messageInfo);
                Collections.sort(messageInfos, (o1, o2) -> {
                    if (o1.getCreateTime() == o2.getCreateTime()) {
                        return 0;
                    } else {
                        return o1.getCreateTime() < o2.getCreateTime() ? -1 : 1;
                    }
                });
                recyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IntentParam.USER_INFO, userInfo);
        intent.putExtra(IntentParam.COUNT, messageInfos.size());
        List<MessageInfo> messageInfoList = messageInfos;
        for (int i = 0; i < messageInfoList.size(); i++) {
            intent.putExtra(IntentParam.MESSAGE_INFO + i, messageInfoList.get(i));
        }
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
