package com.bhavyathacker.jobscheduler;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyJobService extends JobService {

    public static final String TAG = MyJobService.class.getSimpleName();
    private static final String CHANNEL_ID = "JobId";
    private boolean jobCancelled = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private FusedLocationProviderClient mFusedLocationClient;
    LocationCallBackListner locationCallBackListner;

//    private static final int INTERVAL = 1000 * 60 * 15; //15 Mins
    private static final int INTERVAL = 10000; //15 Mins
    private static final int FAST_INTERVAL = 1000 * 60; //5 Seconds




    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job Started ");
//        doBackgroundWork(jobParameters);
        doBackgroundWorkForLocation(jobParameters);
        return true;//so device can stay awake and our task completes successfully
    }

    private void doBackgroundWorkForLocation(JobParameters jobParameters) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallBackListner = new LocationCallBackListner();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FAST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallBackListner, null);
            Looper.myLooper();

        }

        Log.d(TAG, "doBackgroundWorkForLocation: jobFinished");

        jobFinished(jobParameters,true);



    }

    private void doBackgroundWork(final JobParameters jobParameters) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    Log.d(TAG, "run: " + i);
//                    showToast("Job Running " + i);
                    sendNotification("JobScheduler", "Job running " + i);
                    if (jobCancelled) {
                        return;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Job finished");
                jobFinished(jobParameters, false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled before completion ");
        jobCancelled = true;
        return true;
    }

    // Helper for showing tests
    void showToast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyJobService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails,String text) {
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
//        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
//        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
//        PendingIntent notificationPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text);
//                .setContentIntent(notificationPendingIntent);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }



    public class LocationCallBackListner extends LocationCallback {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.e("OnLocationChange", ":BackGroundService");

            if (locationResult != null) {
                Location location = locationResult.getLastLocation();
                Log.e("OnLocationChange", ":BackGroundService" + location.getLatitude() + " : " + location.getLongitude());

                String address = getCompleteAddressString(location.getLatitude(), location.getLongitude());
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy H:mm:ss");
                String dateObj = sdf.format(new Date());
                /*sharepreferanceObj.SessionStore(ConstantData.LatMinuteLatitudeKEY, location.getLatitude());
                sharepreferanceObj.SessionStore(ConstantData.LatMinuteLongitudeKEY, location.getLongitude());
                sharepreferanceObj.SessionStore(ConstantData.LatMinuteAltitudeKEY, location.getAltitude());
                sharepreferanceObj.SessionStore(ConstantData.LatMinuteAccuracyKEY, location.getAccuracy());
                sharepreferanceObj.SessionStore(ConstantData.LatMinuteAddressKEY, address);
                sharepreferanceObj.SessionStore(ConstantData.LatMinuteDateTimeKEY, dateObj);
                if (sharepreferanceObj.PickDoubleValue(sharedLongitude) != 0 && sharepreferanceObj.PickDoubleValue(sharedLatitiude) != 0) {
                    Log.e(TAG, "Getting Location Sqlite: ");
                    if (sharepreferanceObj.PickDoubleValue(sharedLongitude) == location.getLongitude() && sharepreferanceObj.PickDoubleValue(sharedLatitiude) == location.getLatitude()) {
                        Log.e("Sahred Longitude Not", ":" + sharepreferanceObj.PickDoubleValue(sharedLongitude));
                        Log.e("Current Longitude Not", ":" + location.getLongitude());
                        return;
                    } else {
                        Log.e("Sahred Longitude Saving", ":" + sharepreferanceObj.PickDoubleValue(sharedLongitude));
                        Log.e("CurrLongitude Saving", ":" + location.getLongitude());
                        storeCurrentLocationInSession(location);
                    }
                } else {
                    storeCurrentLocationInSession(location);
                }*/

                /*SimpleDateFormat df = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                String datewithtime = df.format(Calendar.getInstance()
                        .getTime());
                String user_id = "";
                String connectionId = "";

                if (getSharedPrefrenceValue(getApplicationContext(), "userId") != null) {
                    user_id = getSharedPrefrenceValue(getApplicationContext(),
                            "userId");
                    connectionId = getSharedPrefrenceValue(getApplicationContext(),
                            "connectionId");
                }

                Log.e("DataInsert", ":" + location.getLatitude() + "" + location.getLongitude() +
                        "" + location.getAltitude() + "" + datewithtime + ":" + user_id + ":" + connectionId + ":" + location.getProvider());
                constant.gpsDetailsInsert("" + location.getLatitude(), "" + location.getLongitude(),
                        "" + location.getAltitude(), datewithtime, user_id, connectionId, location.getProvider());*/
                Log.e("Location Changed", location.getLatitude() + ":" + location.getLongitude());
            } else {
                Log.e("Location ", "Null Received");
            }
            super.onLocationResult(locationResult);
        }

        @Override

        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    }


    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            Log.e("My Current address", addresses.toString());
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.e("My Current address", strReturnedAddress.toString());
//                sendNotification("",strReturnedAddress.toString());
                sendNotification("LAT:"+LATITUDE+"LONG:"+LONGITUDE,strReturnedAddress.toString());
            } else {
                Log.e("My Current address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current address", "Canont get Address!");
        }
        return strAdd;
    }


}
