package com.example.videocapture;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videocapture.http.HttpMultipartPost;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhangzhaolei on 2015/6/30.
 */

public class VideoRecordActivity extends Activity {

    private final static String TAG = "VideoRecordActivity";
    private VideoRecorderView recoderView;
    private Button videoController;
    private TextView message;
    private boolean isCancel = false;
    private HttpMultipartPost post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record);

        recoderView = (VideoRecorderView) findViewById(R.id.recoder);
        videoController = (Button) findViewById(R.id.videoController);
        message = (TextView) findViewById(R.id.message);

        ViewGroup.LayoutParams params = recoderView.getLayoutParams();
        int[] dev = getDisplayMetrics();//PhoneUtil.getResolution(this);
        params.width = dev[0];
        params.height = dev[1]*2/3;//(int) (((float) dev[1])*4/5);
        recoderView.setLayoutParams(params);
        videoController.setOnTouchListener(new VideoTouchListener());

        recoderView.setRecorderListener(new VideoRecorderView.RecorderListener() {

            @Override
            public void recording(int maxtime, int nowtime) {

            }

            @SuppressLint("LongLogTag")
            @Override
            public void recordSuccess(File videoFile) {
                //System.out.println("recordSuccess");
                if (videoFile != null)
                    Log.e(TAG+".recordSuccess()","videoFilePath======"+videoFile.getAbsolutePath());
                if (videoFile.getAbsolutePath() != null) {
                    showDialog(videoFile.getAbsolutePath());
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void recordStop() {
                Log.e(TAG+".recordStop()","=========");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void recordCancel() {
                Log.e(TAG+".recordCancel()","=========");
                releaseAnimations();
            }

            @SuppressLint("LongLogTag")
            @Override
            public void recordStart() {
                Log.e(TAG+".recordStart()","=========");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void videoStop() {
                Log.e(TAG+".videoStop()","=========");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void videoStart() {
                Log.e(TAG+".videoStart()","=========");
            }


        });

    }

    @SuppressLint("LongLogTag")
    protected void showDialog(final String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoRecordActivity.this);
        builder.setMessage("上传到服务器？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Log.e(TAG+".showDialog()","path===="+path);
                Upload(path);
                //uploadFile(path);
                /*
                Intent intentPath = new Intent();
                intentPath.setClass(VideoRecordActivity.this,UploadActivity.class);
                intentPath.putExtra("path",path);
                //startActivity(new Intent(VideoRecordActivity.this, UploadActivity.class));
                startActivity(intentPath);
                */
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void Upload (String videoPath){
        //String filePath = videoPath;//"/system/build.prop";
        File file = new File(videoPath);
        if (file.exists()) {
            post = new HttpMultipartPost(this, videoPath);
            post.execute();
        } else {
            Toast.makeText(this, "file not exists", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void uploadFile(String path) {

        String uploadUrl = "http://192.168.1.228:8080/temp";
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url
                    .openConnection();
            httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(httpURLConnection
                    .getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos
                    .writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
                            + path.substring(path.lastIndexOf("/") + 1)
                            + "\"" + end);
            dos.writeBytes(end);

            FileInputStream fis = new FileInputStream(path);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);

            }
            fis.close();

            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();

            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            dos.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
            setTitle(e.getMessage());
        }

    }

    public class VideoTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {


            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    recoderView.startRecord();
                    isCancel = false;
                    pressAnimations();
                    holdAnimations();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getX() > 0
                            && event.getX() < videoController.getWidth()
                            && event.getY() > 0
                            && event.getY() < videoController.getHeight()) {
                        //showPressMessage();
                        isCancel = false;
                    } else {
                        //cancelAnimations();
                        isCancel = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isCancel) {
                        recoderView.cancelRecord();
                    }else{
                        recoderView.endRecord();
                    }
                    message.setVisibility(View.GONE);
                    releaseAnimations();
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    /**
     * 移动取消弹出动画
     */
    /*
    public void cancelAnimations() {
        message.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        message.setTextColor(getResources().getColor(android.R.color.white));
        message.setText("松手取消");
    }*/

    /**
     * 显示提示信息
     */
    /*
    public void showPressMessage() {
        message.setVisibility(View.VISIBLE);
        message.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        message.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        message.setText("上移取消");
    }*/


    /**
     * 按下时候动画效果
     */
    public void pressAnimations() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.5f,
                1, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(200);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setFillAfter(true);
        //message.setText("松手完成");
        videoController.startAnimation(animationSet);
    }

    /**
     * 释放时候动画效果
     */
    public void releaseAnimations() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1f,
                1.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(200);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setFillAfter(true);

        //message.setVisibility(View.GONE);
        videoController.setText("按住拍");
        videoController.startAnimation(animationSet);
    }

    public void holdAnimations() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1f,
                1.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(200);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setFillAfter(true);

        videoController.setText("松手完成");
        videoController.startAnimation(animationSet);
    }

    /**
     * get resolution
     *
     * @return
     */
    @SuppressLint("LongLogTag")
    public int[] getDisplayMetrics() {
        int resolution[] = new int[2];
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        resolution[0] = dm.widthPixels;
        resolution[1] = dm.heightPixels;
        Log.e(TAG+".getDisplayMetrics()","DisplayMetrics.width="+dm.widthPixels+"***DisplayMetrics.height=dm.heightPixels");
        return resolution;
    }

}
