package com.example.forwardingiphone;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class KeepAliveJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
