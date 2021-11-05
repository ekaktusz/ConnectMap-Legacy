package com.example.connectmap;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.connectmap.database.Post;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HelperMethods {

    public static double distanceFromPost(Post post, double latitude, double longitude) {
        Location locationA = new Location("point A");
        locationA.setLatitude(latitude);
        locationA.setLongitude(longitude);
        Location locationB = new Location("point B");
        locationB.setLatitude(post.getLatitude());
        locationB.setLongitude(post.getLongitude());
        return locationA.distanceTo(locationB);
    }

    public static boolean isPostInTime(Post post, Date dateOrigo, int timeInterval) {
        long timeDiff = Math.abs(dateOrigo.getTime() + milliSecondsSinceMidnight() - post.getDateLong());
        boolean inTime = TimeUnit.HOURS.convert(timeDiff, TimeUnit.MILLISECONDS) < timeInterval;
        return inTime;
    }

    public static boolean isPostNearby(Post post, double latitude, double longitude, int radius) {
        boolean inRadius = HelperMethods.distanceFromPost(post, latitude, longitude) < radius * 1000;
        return inRadius;
    }

    private static long milliSecondsSinceMidnight() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return now - calendar.getTimeInMillis();
    }

    public static boolean isNetworkAvailable(Context mContext) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
