package com.hf.circleseekbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.hf.view.CircleSeekBar;

public class MainActivity extends AppCompatActivity {

    TextView mProgress;
    TextView mCircleCount;
    CircleSeekBar mCircleSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgress = (TextView) findViewById(R.id.progress);
        mCircleCount = (TextView) findViewById(R.id.circle_count);
        mCircleSeekBar = (CircleSeekBar) findViewById(R.id.circleseekbar);

        mCircleSeekBar.setOnProgressListener(new CircleSeekBar.OnProgressListener() {
            @Override
            public void onProgress(int progress, int max) {
                setProgressMsg(progress, max);
            }

            @Override
            public void onCircleCountChanged(int circleCount) {
                setCircleCountMsg(circleCount);
            }
        });

        setCircleCountMsg(mCircleSeekBar.getCircleCount());
        setProgressMsg(mCircleSeekBar.getProgress(), mCircleSeekBar.getMaxProgress());
    }

    private void setCircleCountMsg(int circleCount) {
        mCircleCount.setText(String.valueOf(circleCount));
    }

    private void setProgressMsg(int progress, int max) {
        mProgress.setText(String.format("%d / %d", progress, max));
    }
}
