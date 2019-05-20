package com.example.soundsettingchooser;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.FileUriExposedException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnShowDialog, btnPlaySound, btnPlayNotif;
    TextView txvSoundChoose;
    MediaPlayer mMediaPlayer;
    private static int REQUEST_READ_EXTERNAL = 90;
    private static int RESULT_CODE_RINGTONE = 22;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnShowDialog = findViewById(R.id.btnShowDialog);
        btnPlaySound = findViewById(R.id.btnPlaySound);
        btnPlayNotif = findViewById(R.id.btnPlayNotif);
        txvSoundChoose = findViewById(R.id.txvSoundChoose);
        btnShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying();
                changeRingtone();
            }
        });
        btnPlaySound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying();
                try {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer = MediaPlayer.create(MainActivity.this, Uri.parse(new Preferences(MainActivity.this).getRingtone()));
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setLooping(false);
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
        btnPlayNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying();
                showNotif("Hai", "Ini Notif");
            }
        });
        getRecentRingtone();
    }

    private void stopPlaying() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlaying();
    }

    public void changeRingtone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL);
            } else {
                showDialogRingtone();
            }
        } else {
            showDialogRingtone();
        }
    }

    public void showDialogRingtone() {
        Uri defaultSoundUri = Uri.parse(new Preferences(this).getRingtone());
        Uri sound = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Pilih Nada Dering");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, sound);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultSoundUri);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        startActivityForResult(intent, RESULT_CODE_RINGTONE);
    }

    private void getRecentRingtone(){
        Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(new Preferences(this).getRingtone()));
        txvSoundChoose.setText(ringtone.getTitle(this));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showDialogRingtone();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE_RINGTONE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null) {
                    new Preferences(this).setIsSetRingtone(uri.toString());
                    getRecentRingtone();
                } else {
                    new Preferences(this).setIsSetRingtone("");
                    getRecentRingtone();
                }
            }
        }
    }

    private void showNotif(String title, String body){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = Uri.parse(new Preferences(this).getRingtone());
        Log.e("SOUND",defaultSoundUri.toString());
        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(1)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel(BuildConfig.APPLICATION_ID+".notif", "Pemberitahuan", NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(false);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});
            channel.setSound(defaultSoundUri, audioAttributes);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(BuildConfig.APPLICATION_ID+".notif");
        }
        try {
            notificationBuilder.setSound(defaultSoundUri);
            notificationManager.notify(101 /* ID of notification */, notificationBuilder.build());
        } catch (Exception e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && e instanceof FileUriExposedException) {
                notificationBuilder.setSound(null);
                notificationManager.notify(101 /* ID of notification */, notificationBuilder.build());
            }
        }
    }
}
