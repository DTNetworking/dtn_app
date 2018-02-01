package me.iologic.apps.dtn;

import android.util.Log;

/**
 * Created by vinee on 01-02-2018.
 */

public class StopWatchBW extends StopWatch {

    public StopWatchBW(){

    }

    private void updateTimer(float time) {
    	/* Although we are not using milliseconds on the timer in this example
    	 * I included the code in the event that you wanted to include it on your own
    	 */
        Log.i(Constants.TAG, " Time : " + time);

		/* Setting the timer text to the elapsed time */
        globalTime = time;
    }

    public float getTime(){
        return globalTime;
    }
}
