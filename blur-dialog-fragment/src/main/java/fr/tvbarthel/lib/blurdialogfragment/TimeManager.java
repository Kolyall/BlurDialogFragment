package fr.tvbarthel.lib.blurdialogfragment;

import android.util.Log;

import java.util.HashMap;

/**
 * Created by Nick Unuchek on 29.11.2017.
 */

public class TimeManager {
    public static final String TAG = TimeManager.class.getSimpleName();

    private static TimeManager instance;
    private HashMap<Integer,Long> mHashMap;

    public TimeManager() {
        mHashMap = new HashMap<>();
    }

    public void addTime(int id){
        mHashMap.put(id,System.nanoTime());
    }

    public void print(int id){
        if (!mHashMap.containsKey(id)) {
            Log.e(TAG, "print: no id = "+id );
            return;
        }
        Long startTime = mHashMap.get(id);
        Long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        Log.e(TAG, "print: id = " + id+" Estimation Time: " + duration/1000000  + " ms");
    }


    public static TimeManager getInstance() {
        TimeManager localInstance = instance;
        if (localInstance == null) {
            synchronized (TimeManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new TimeManager();
                }
            }
        }
        return localInstance;
    }
}
