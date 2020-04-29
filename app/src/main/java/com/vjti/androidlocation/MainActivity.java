package com.vjti.androidlocation;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication3.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.BuildConfig;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String MyPREFERENCES = "MyPrefs" ;
    private SharedPreferences sharedpreferences;


    @BindView(R.id.location_result)
    TextView txtLocationResult;
    @BindView(R.id.updated_on)
    TextView txtUpdatedOn;
    private String mLastUpdateTime;

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    // fastest updates interval - 5 sec
// location updates will be received if another app is requesting the locations
// than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private double[] HOME = new double[]{19.0603782, 73.0059477};
    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private String encryptedLat, encryptedLng;
    private String decryptedLat, decryptedLng;
    private long aesEncyptionTime, aesDecyptionTime, desEncyptionTime, desDecyptionTime, _3desEncyptionTime, _3desDecyptionTime, blowFishEncryptionTime, blowFishDecryptionTimel;

    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;
    private Boolean storagePermission = false;
    private int radioSelect = 0;
    final private String secretKey = "MyDifficultPassw";
    final private String secretKeyDes = "MyDesKey";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
// initialize the necessary libraries
        init();
// restore the values from saved instance state
        restoreValuesFromBundle(savedInstanceState);
    }
    private void init() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
// location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                try {
                    encrypt();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error writting location...", Toast.LENGTH_SHORT).show();
                }
                updateLocationUI();
            }
        };
        mRequestingLocationUpdates = false;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
        checkLocationPermssion();
    }
    byte[] encLat;
    byte[] encLng;
    void encrypt() throws Exception {
        if(storagePermission) {
            String encrypted;
            aesEncyptionTime = System.nanoTime();
            encrypted = do_encrypt("AES");
            aesEncyptionTime = System.nanoTime()-aesEncyptionTime;
            aesDecyptionTime = System.nanoTime();
            decryptedLat = decrypt(encLat,"AES");
            decryptedLng = decrypt(encLng,"AES");
            aesDecyptionTime = System.nanoTime()-aesDecyptionTime;
            desEncyptionTime = System.nanoTime();
            encrypted = do_encrypt("DES");
            desEncyptionTime = System.nanoTime()-desEncyptionTime;
            desDecyptionTime = System.nanoTime();
            decryptedLat = decrypt(encLat,"DES");
            decryptedLng = decrypt(encLng,"DES");
            desDecyptionTime = System.nanoTime()-desDecyptionTime; _3desEncyptionTime = System.nanoTime();
            encrypted = do_encrypt("DESede"); _3desEncyptionTime = System.nanoTime()-_3desEncyptionTime; _3desDecyptionTime = System.nanoTime();
            decryptedLat = decrypt(encLat,"DESede");
            decryptedLng = decrypt(encLng,"DESede"); _3desDecyptionTime = System.nanoTime()-_3desDecyptionTime;
            blowFishEncryptionTime = System.nanoTime();
            encrypted = do_encrypt("Blowfish");
            blowFishEncryptionTime = System.nanoTime()-blowFishEncryptionTime;
            blowFishDecryptionTimel = System.nanoTime();
            decryptedLat = decrypt(encLat,"Blowfish");

            decryptedLng = decrypt(encLng,"Blowfish");
            blowFishDecryptionTimel = System.nanoTime()-blowFishDecryptionTimel;
            createFile("locations.txt",encrypted);
            Toast.makeText(getApplicationContext(), "Writting location...", Toast.LENGTH_SHORT).show();
//this will be removed later
            createFile("md5.txt",md5(encrypted));
        } else {
            Toast.makeText(getApplicationContext(), "Storage permission not available", Toast.LENGTH_SHORT).show();
        }
    }
    private String do_encrypt(String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec sks = new SecretKeySpec(secretKey.getBytes(), algorithm);;
        if(algorithm.equals("DES") || algorithm.equals("3DES")){
            sks = new SecretKeySpec(secretKeyDes.getBytes(), algorithm);
            System.out.println("algorithem is : "+algorithm);
        }
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        String lat = mCurrentLocation.getLatitude()+"";
        String lnt = mCurrentLocation.getLongitude()+"";
        encLat = cipher.doFinal(lat.getBytes("UTF-8"));
        encLng = cipher.doFinal(lnt.getBytes("UTF-8"));
        encryptedLat = encLat.toString();
        encryptedLng = encLng.toString();
        String encrypted = Base64.encodeToString(encLat, Base64.DEFAULT) + " " +
                Base64.encodeToString(encLng, Base64.DEFAULT);
        return encrypted;
    }
    private void createFile(String name,String encrypted) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/"+name);
        FileOutputStream fos = new FileOutputStream(file, false);
        fos.write(encrypted.getBytes());
        fos.flush();
        fos.close();
    }

    private String readFile(String name){
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/"+name);
        String content="";
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String temp = "";
            while((temp=br.readLine())!=null){
                content+=temp;
                content+="\n";
            }
        }catch (Exception e){}
        return content;
    }
    String decrypt(byte[] encrypted,String algotithm) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(), algotithm);
        if(algotithm.equals("DES") || algotithm.equals("3DES")){
            skeySpec = new SecretKeySpec(secretKeyDes.getBytes(), algotithm);
            System.out.println("algorithem is : "+algotithm);
        }
        Cipher cipher = Cipher.getInstance(algotithm);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-8");
    }
    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }
    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[256];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates =
                        savedInstanceState.getBoolean("is_requesting_updates");
            }
            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }
            if (savedInstanceState.containsKey("last_updated_on")) {

                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }
        updateLocationUI();
    }
    private String md5(String s) {
        try {
// Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
// Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * Update the UI displaying the location data
     * and toggling the buttons
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            txtLocationResult.setText("Lat: " + mCurrentLocation.getLatitude() +
                    "\nLng: " + mCurrentLocation.getLongitude() +
                    "\nEncryptedLat: " + encryptedLat +
                    "\nEncryptedLng: " + encryptedLng +
                    "\nDecryptedLat: " + decryptedLat +
                    "\nDecryptedLng: " + decryptedLng +
                    "\nAES_EncryptionTime: "+aesEncyptionTime+
                    "\nAES_DecryptionTime: "+aesDecyptionTime+
                    "\nDES_EncryptionTime: "+desEncyptionTime+
                    "\nDES_DecryptionTime: "+desDecyptionTime+
                    "\n3DES_EncryptionTime: "+_3desEncyptionTime+
                    "\n3DES_DecryptionTime: "+_3desDecyptionTime+
                    "\nBlowFish_EncryptionTime: "+blowFishEncryptionTime+
                    "\nBlowFish_DecryptionTime: "+blowFishDecryptionTimel
            );

            // giving a blink animation on TextView
            txtLocationResult.setAlpha(0);
            txtLocationResult.animate().alpha(1).setDuration(300);
// location last updated time
            txtUpdatedOn.setText("Last updated on: " + mLastUpdateTime);
        }
// toggleButtons();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putString("last_updated_on", mLastUpdateTime);
    }
    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        checkStoragePermssion();
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new
                        OnSuccessListener<LocationSettingsResponse>() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onSuccess(LocationSettingsResponse locationSettingsResponse)
                            {
                                Log.i(TAG, "All location settings are satisfied.");
                                Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();
//noinspection MissingPermission
                                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                                updateLocationUI();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {


                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
// Show the dialog by calling startResolutionForResult(), and check the// result in onActivityResult().
                                ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case  LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot  be " +
                                "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                        updateLocationUI();
                    }
                });
    }
    public void checkLocationPermssion() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
// open device settings when the permission is
// denied permanently
                            openSettings();
                        }
                    }


                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest
                                                                           permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }
    public void checkStoragePermssion() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        storagePermission = true;
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        storagePermission = false;
                        if (response.isPermanentlyDenied()) {
// open device settings when the permission is
// denied permanently
                            openSettings();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest
                                                                           permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }
    public void checkCameraPermssion() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        startActivity(intent);
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {

                            // open device settings when the permission is
// denied permanently
                            openSettings();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest
                                                                           permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }
    public void stopLocationUpdates() {
// Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
// toggleButtons();
                    }
                });
    }

    @OnClick(R.id.btn_sys)
    public void showOutRange() {
    String hash = readFile("md5.txt").trim();
    String encrypted=readFile("locations.txt");
    Log.e(TAG,"this is stored hash");

    Log.e(TAG,md5(encrypted).trim()+" this is generated hash");
    if(!hash.equals(md5(encrypted).trim())){
        Log.e(TAG,"Location file have been changed !");
        Toast.makeText(getApplicationContext(), "Location file have been changed !", Toast.LENGTH_SHORT).show();
        return;
    }
Log.i(TAG,"Location file is secure");
if(radioSelect == 0) {
        Toast.makeText(getApplicationContext(), "Within range!", Toast.LENGTH_SHORT).show();

        checkCameraPermssion();
    } else {
        Toast.makeText(getApplicationContext(), "Out of range!", Toast.LENGTH_SHORT).show();
    }
}
    public void onRadioButtonClicked(View view) {
// Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
// Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_in:
                if (checked)
                    radioSelect = 0;
                break;
            case R.id.radio_out:
                if (checked)
                    radioSelect = 1;
                break;
        }
    }
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to            startResolutionForResult().
    case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
// Nothing to do. startLocationupdates() gets called in onResume again.
                    break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }
    }
    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);

        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    @Override
    public void onResume() {
        super.onResume();
// Resuming location updates depending on button state and
// allowed permissions
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }
        updateLocationUI();
    }
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mRequestingLocationUpdates) {
// pausing location updates
            stopLocationUpdates();
        }
    }
}
