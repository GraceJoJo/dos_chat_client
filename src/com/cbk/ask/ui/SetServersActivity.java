package com.cbk.ask.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.cbk.ask.AskModel;
import com.cbk.ask.R;
import com.hyphenate.easeui.widget.EaseTitleBar;

public class SetServersActivity extends BaseActivity {

    EditText restEdit;
    EditText imEdit;
    EaseTitleBar titleBar;

    AskModel askModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_servers);

        restEdit = (EditText) findViewById(R.id.et_rest);
        imEdit = (EditText) findViewById(R.id.et_im);
        titleBar = (EaseTitleBar) findViewById(R.id.title_bar);

        askModel = new AskModel(this);
        if(askModel.getRestServer() != null)
            restEdit.setText(askModel.getRestServer());
        if(askModel.getIMServer() != null)
            imEdit.setText(askModel.getIMServer());
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(!TextUtils.isEmpty(restEdit.getText()))
            askModel.setRestServer(restEdit.getText().toString());
        if(!TextUtils.isEmpty(imEdit.getText()))
            askModel.setIMServer(imEdit.getText().toString());
        super.onBackPressed();
    }
}
