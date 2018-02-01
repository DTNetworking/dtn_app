package me.iologic.apps.dtn;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

class StopWatch extends Thread {
    private TextView delayTV; //Temporary TextView
    private Handler mHandler = new Handler();
    private long startTime;
    private long elapsedTime;
    private final int REFRESH_RATE = 100;
    private boolean stopped = false;

    ArrayList<String> msgTiming = new ArrayList<String>();

    public StopWatch(TextView delay){
        delayTV = delay;
    }


    private void updateTimer(float time) {
    	/* Although we are not using milliseconds on the timer in this example
    	 * I included the code in the event that you wanted to include it on your own
    	 */
        Log.i(Constants.TAG, " Time : " + time);

		/* Setting the timer text to the elapsed time */
        delayTV.setText(time + " ms");

        // Update Message Timing List and Reset The Timer
        updateList(time);
    }

    private void updateList(float updateTime){
        msgTiming.add(updateTime + " ms");
    }

    private Runnable startTimer = new Runnable() {
        public void run() {
            elapsedTime = System.currentTimeMillis() - startTime;
            updateTimer(elapsedTime);
            mHandler.postDelayed(this,REFRESH_RATE);
        }
    };

    public void start (){
        if(stopped){
            startTime = System.currentTimeMillis() - elapsedTime;
        }
        else{
            startTime = System.currentTimeMillis();
        }
        mHandler.removeCallbacks(startTimer);
        mHandler.postDelayed(startTimer, 0);
    }

    public void halt (){
        mHandler.removeCallbacks(startTimer);
        stopped = true;
    }

    public void reset (){
        stopped = false;
        // delayTV.setText("StopWatch Reset");
    }

    public ArrayList<String> getTimings(){
        return msgTiming;
    }

}