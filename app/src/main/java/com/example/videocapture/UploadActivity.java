package com.example.videocapture;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.videocapture.http.HttpMultipartPost;

import java.io.File;

/**
 * Created by zhangzhaolei on 15-6-30.
 */

public class UploadActivity extends AppCompatActivity {//implements View.OnClickListener {

    private Context context;

    private EditText et_filepath;
    private Button btn_upload;
    private Button btn_cancle;

    private HttpMultipartPost post;
    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //setContentView(R.layout.uplaod);
        Intent intentPath = getIntent();
        videoPath = intentPath.getStringExtra("path");
        Upload(videoPath);
        /*
        et_filepath = (EditText) findViewById(R.id.et_filepath);
        btn_upload = (Button) findViewById(R.id.btn_upload);
        btn_cancle = (Button) findViewById(R.id.btn_cancle);

        btn_upload.setOnClickListener(this);
        btn_cancle.setOnClickListener(this);
        */
    }

    public void Upload (String videoPath){
        //String filePath = videoPath;//"/system/build.prop";
        File file = new File(videoPath);
        if (file.exists()) {
            post = new HttpMultipartPost(context, videoPath);
            post.execute();
        } else {
            Toast.makeText(context, "file not exists", Toast.LENGTH_LONG).show();
            finish();
        }
    }
/*
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upload:
                //String filePath = et_filepath.getText().toString();
                String filePath = videoPath;//"/system/build.prop";
                File file = new File(filePath);
                if (file.exists()) {
                    post = new HttpMultipartPost(context, filePath);
                    post.execute();
                } else {
                    Toast.makeText(context, "file not exists", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_cancle:
                if (post != null) {
                    if (!post.isCancelled()) {
                        post.cancel(true);
                    }
                }
                break;
        }

    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
