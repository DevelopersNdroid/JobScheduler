package com.bhavyathacker.jobscheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private Button btnScheduleJob, btnCancelJob;
    private int JOB_ID = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnScheduleJob = findViewById(R.id.btnScheduleJob);
        btnCancelJob = findViewById(R.id.btnCancelJob);
        btnScheduleJob.setOnClickListener(this);
        btnCancelJob.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnScheduleJob:
                scheduleJob();
                break;
            case R.id.btnCancelJob:
                cancelJob();
                break;
        }
    }


    private void scheduleJob() {
        ComponentName componentName = new ComponentName(this, MyJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_METERED)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000) //15 minutes Note: from nougat onwards we can't set < 15 min if we set then it will be reset to 15 minutes
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job Scheduled Successfully ");
        } else {
            Log.d(TAG, "Job Scheduling failed ");
        }
    }

    private void cancelJob() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
        Log.d(TAG, "Job cancelled");
    }
}
