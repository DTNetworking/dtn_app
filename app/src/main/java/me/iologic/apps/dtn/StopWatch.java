package me.iologic.apps.dtn;

import android.os.SystemClock;

/**
 * Created by vinee on 20-01-2018.
 */

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.os.Handler;

public class StopWatch extends Thread {
    long MillisecondTime, startTime, timeBuf, updateTime=0L;

    private java.util.logging.Handler sHandler = new Handler();

    int seconds, minutes, milliseconds;

    public StopWatch(Handler handler){

    }

    public void start(){
        startTime = SystemClock.uptimeMillis();
        handler
    }

    public void pause(){
        timeBuf += MillisecondTime;
        handler
    }

    public void reset(){
        MillisecondTime = 0L ;
        startTime = 0L ;
        timeBuf = 0L ;
        updateTime = 0L ;
        seconds = 0 ;
        minutes = 0 ;
        milliseconds = 0 ;

        textView.setText("00:00:00");
    }

    public Runnable runnable = new Runnable() {
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - startTime;
            updateTime = timeBuf + MillisecondTime;
            seconds = (int) (updateTime / 1000);
            minutes = seconds / 60;
            seconds = seconds % 60;
            milliseconds = (int) (updateTime % 1000);
            textView.setText("" + minutes + ":" + String.format("%02d", seconds) + ":" + String.format("%03d", milliseconds));
            handler.postDelayed(this, 0);
        }
    };
}
