package com.heimann.andreas.visualvault;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas on 03.04.2017.
 */

public class PointCollector implements OnTouchListener {

    private List<Point> pointList = new ArrayList<>();
    private PointCollectorListener listener;

    public void setListener(PointCollectorListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int x = Math.round(event.getX());
        int y = Math.round(event.getY());

        pointList.add(new Point(x, y));

        if (pointList.size() == 4) {
            if (listener != null) {
                listener.pointsCollected(pointList);
            }
        }

        return false;
    }

    public void clear() {
        pointList.clear();
    }

}
