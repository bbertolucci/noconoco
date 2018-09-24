package com.bbproject.noconoco.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.activities.VideoActivity;
import com.bbproject.noconoco.constants.CategoryType;
import com.bbproject.noconoco.constants.Constants;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.model.json.Show;
import com.bbproject.noconoco.connection.AsyncResponse;
import com.bbproject.noconoco.connection.ConnectNoco;
import com.bbproject.noconoco.utils.ObjectSerializer;
import com.bbproject.noconoco.utils.SettingUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class DownloadService extends Service implements AsyncResponse {

    private static final String TAG = "DownloadService";
    private static final int STATUS_WAIT = 0;
    private static final int STATUS_DOWNLOADING = 1;
    private static final int STATUS_DOWNLOADED = 2;
    private final Context mContext;
    private final SettingUtils mSettings;
    private boolean mIsDownloading = false;
    private Show mShow;
    private String mTextTitle;


    public DownloadService() {
        super();
        mContext = this;
        mSettings = new SettingUtils(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStart(Intent pIntent, int pStartId) {
        try {
            ArrayList<Show> currentRecorded = (ArrayList<Show>) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
            for (Iterator<Show> iterator = currentRecorded.iterator(); iterator.hasNext(); ) {
                Show show = iterator.next();
                if (show.getRecordedStatus() == STATUS_DOWNLOADED) {
                    String path = show.getPath();
                    File f = new File(path);
                    if (!f.exists()) {
                        iterator.remove();
                    }
                }
            }
            mSettings.setString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(currentRecorded));
        } catch (IOException e) {
            Log.e(TAG, "an IOException occurred !", e);
        }

        if (null != pIntent) {
            String type = pIntent.getStringExtra("type");
            if ("addUrl".equals(type)) {
                Show show = (Show) pIntent.getSerializableExtra("show");
                ArrayList<Show> currentList = new ArrayList<>();
                try {
                    currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
                } catch (IOException e) {
                    Log.e(TAG, "an IOException occurred !", e);
                }
                int count = 0;
                for (Show currentShow : currentList) {
                    if (currentShow.getShowPaid() != 1) {
                        count++;
                    }
                }
                if (count < Constants.MAX_OFFLINE_FILE) {
                    show.setRecordedStatus(STATUS_WAIT);//Waiting
                    currentList.add(show);
                    try {
                        mSettings.setString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(currentList));
                    } catch (IOException e) {
                        Log.e(TAG, "an IOException occurred !", e);
                    }

                    type = "refresh";
                } else {
                    Toast.makeText(mContext, R.string.download_end, Toast.LENGTH_LONG).show();
                }
            }
            if ("downloadNext".equals(type)) {
                ArrayList<Show> currentList = new ArrayList<>();
                try {
                    currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
                } catch (IOException e) {
                    Log.e(TAG, "an IOException occurred !", e);
                }
                for (Show show : currentList) {
                    if (show.getRecordedStatus() == STATUS_DOWNLOADING) {
                        return;
                    }
                    if (show.getRecordedStatus() == STATUS_WAIT) {
                        show.setRecordedStatus(STATUS_DOWNLOADING);
                        currentList.set(currentList.indexOf(show), show);
                        Log.d(TAG, "Start Service");
                        try {
                            mSettings.setString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(currentList));
                        } catch (IOException e) {
                            Log.e(TAG, "an IOException occurred !", e);
                        }
                        startDownload(show);
                        return;
                    }
                }
                stopSelf();
            }
            if ("refresh".equals(type) && !mIsDownloading) {
                ArrayList<Show> currentList = new ArrayList<>();
                try {
                    currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
                } catch (IOException e) {
                    Log.e(TAG, "an IOException occurred !", e);
                }
                for (Show show : currentList) {
                    if (show.getRecordedStatus() == STATUS_DOWNLOADING) {
                        Log.d(TAG, "Start Service");
                        startDownload(show);
                        return;
                    }
                }
                for (Show show : currentList) {
                    if (show.getRecordedStatus() == STATUS_WAIT) {
                        show.setRecordedStatus(STATUS_DOWNLOADING);
                        currentList.set(currentList.indexOf(show), show);
                        try {
                            mSettings.setString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(currentList));
                        } catch (IOException e) {
                            Log.e(TAG, "an IOException occurred !", e);
                        }
                        sendBroadcast(new Intent("Refresh"));
                        startDownload(show);
                        return;
                    }
                }
                stopSelf();
            }
        }
        stopSelf();
    }

    @Override
    public void processFinish(String pResponse, int pNextStep) {
        Log.d(TAG, "step" + pNextStep);
        if (CategoryType.RECORD == pNextStep) {
            if (!pResponse.startsWith("NG")) {
                try {
                    JSONObject root_object = new JSONObject(pResponse);
                    String code_response = root_object.getString("code_reponse");
                    switch (code_response) {
                        case "1":
                            String strUrl = root_object.getString("file");
                            new RetrieveFile(strUrl, DownloadService.this, mShow, mTextTitle).execute();

                            break;
                        case "2":
                        case "3": {
                            JSONObject first = root_object.getJSONObject("popmessage");
                            String message = first.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            Intent serviceIntent = new Intent(mContext, DownloadService.class);
                            serviceIntent.putExtra("type", "refresh");
                            mIsDownloading = false;
                            onStart(serviceIntent, 1);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Ex:", e);
                }
            }
        }
    }

    @Override
    public void processRetry(String pMethod, Map<String, String> pMapParam,
                             String pTokenType,
                             String pUrl, int pNextStep, int pRetry) {
        new ConnectNoco("GET", null, "Bearer", this, mShow.getStreamUrl(), CategoryType.RECORD);
    }

    @Override
    public Context getContext() {
        return DownloadService.this;
    }

    @Override
    public void restart(int pNextStep) {
        stopSelf();
    }

    private void startDownload(Show pShow) {
        mIsDownloading = true;
        mShow = pShow;
        Log.d(TAG, "Enter Service");
        mTextTitle = mShow.getFamilyTT();
        if (0 < mShow.getEpisode() + mShow.getSeason()) {
            if (!"".equals(mTextTitle)) mTextTitle += " - ";
            if (0 < mShow.getSeason()) {
                mTextTitle += getString(R.string.prefix_season) + mShow.getSeason();
                if (0 < mShow.getEpisode()) {
                    mTextTitle += getString(R.string.prefix_episode) + mShow.getEpisode() + getString(R.string.suffix_episode);
                }
            } else if (0 < mShow.getEpisode()) {
                mTextTitle += "#" + mShow.getEpisode();
            }
        }
        PendingIntent intent = PendingIntent.getActivity(DownloadService.this, 0, new Intent(), 0);
        createNotificationChannel();
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(DownloadService.this, Constants.CHANNEL_ID)
                        .setSmallIcon(R.drawable.icon_loading_notification)
                        .setContentTitle(mTextTitle)
                        .setContentText(getString(R.string.download_start))
                        .setContentIntent(intent);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != notificationManager) {
            notificationManager.notify(Integer.parseInt(mShow.getIdShow()), builder.build());
        }
        new ConnectNoco("GET", null, "Bearer", this, mShow.getStreamUrl(), CategoryType.RECORD);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Constants.CHANNEL_NAME;
            String description = Constants.CHANNEL_DESCRIPTIOM;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (null != notificationManager) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private static class RetrieveFile extends AsyncTask<String, Void, File> {

        private final String mStrUrl;
        private final String mFileName;
        private final WeakReference<DownloadService> mDownloadService;
        private final Show mShow;
        private final String mTextTitle;
        private final NotificationManager mNotificationManager;
        private final SettingUtils mSettings;
        private String mError = "";
        private String mPath = "";

        private RetrieveFile(String pStrUrl, DownloadService pDownloadService, Show pShow, String pTextTitle) {
            super();
            this.mStrUrl = pStrUrl;
            this.mFileName = Uri.parse(pStrUrl).getLastPathSegment();
            this.mDownloadService = new WeakReference<>(pDownloadService);
            this.mShow = pShow;
            this.mTextTitle = pTextTitle;
            this.mNotificationManager = (NotificationManager) mDownloadService.get().getSystemService(Context.NOTIFICATION_SERVICE);
            this.mSettings = new SettingUtils(mDownloadService.get());
        }

        protected File doInBackground(String... urls) {
            File f;
            long downloaded = 0;
            try {
                URL url = new URL(this.mStrUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                mPath = mDownloadService.get().getCacheDir().getAbsolutePath() + File.separator + this.mFileName;
                if (mShow.getShowPaid() == 1) {
                    if (Environment.getExternalStorageDirectory().canWrite()) {
                        mPath = Environment.getExternalStorageDirectory() + File.separator + "data" + File.separator + TAG + File.separator;
                    } else {
                        mPath = Environment.getDataDirectory() + File.separator + TAG + File.separator;
                    }
                    File file = new File(mPath);
                    boolean isExist = file.mkdirs();
                    mPath += this.mFileName;
                    Log.d(TAG, "path :" + mPath + " isExist : " + isExist);
                }

                f = new File(mPath);
                if (!f.exists()) {
                    Log.d(TAG, "Downloading : new");
                    boolean isCreated = f.createNewFile();
                    Log.d(TAG, "Downloading : isCreated: " + isCreated);

                } else {
                    downloaded = f.length();
                    Log.d(TAG, "Downloading : resume " + downloaded);
                }
                connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
                connection.connect();
                long fileLength = connection.getContentLength() + downloaded;
                InputStream in = new BufferedInputStream(connection.getInputStream());
                FileOutputStream fos;
                if (downloaded > 0) {
                    fos = new FileOutputStream(f, true);
                } else {
                    fos = new FileOutputStream(f);
                }
                BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
                byte[] data = new byte[1024];
                int x;
                while ((x = in.read(data, 0, 1024)) >= 0) {
                    bout.write(data, 0, x);
                    downloaded += x;
                    if (((int) (downloaded * 100000 / fileLength)) % 1000 == 0) {
                        Log.d(TAG, "Downloading : " + downloaded + "/" + fileLength);
                        PendingIntent intent = PendingIntent.getActivity(mDownloadService.get(), 0, new Intent(), 0);
                        mDownloadService.get().createNotificationChannel();
                        @SuppressLint("IconColors") NotificationCompat.Builder vBuilder =
                                new NotificationCompat.Builder(mDownloadService.get(), Constants.CHANNEL_ID)
                                        .setSmallIcon(R.drawable.icon_loading_notification)
                                        .setContentTitle(mTextTitle)
                                        .setContentText(mDownloadService.get().getString(R.string.download_progress) + ((int) (downloaded * 100 / fileLength)) + "%")
                                        .setProgress(100, ((int) (downloaded * 100 / fileLength)), false)
                                        .setContentIntent(intent);
                        mNotificationManager.notify(Integer.parseInt(mShow.getIdShow()), vBuilder.build());
                    }
                }
            } catch (IOException e) {
                f = null;
                if (e.toString().contains("EACCES"))
                    mError = mDownloadService.get().getString(R.string.error_f_eacces);
                else if (e.toString().contains("ENOSPC"))
                    mError = mDownloadService.get().getString(R.string.error_f_enospc);
                else mError = mDownloadService.get().getString(R.string.error_f_default);

                Log.e(TAG, "An IOException occurred !", e);
            }
            return f;
        }

        @SuppressLint("IconColors")
        @SuppressWarnings("unchecked")
        protected void onPostExecute(File pFile) {
            Log.d(TAG, "DownloadEnd - " + this.mFileName);

            try {
                ArrayList<Show> currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
                mShow.setRecordedStatus(STATUS_DOWNLOADED);
                mShow.setPath(mPath);
                if (null != pFile) currentList.set(currentList.indexOf(mShow), mShow); //End
                else currentList.remove(mShow);
                mSettings.setString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(currentList));
            } catch (IOException e) {
                Log.e(TAG, "an IOException occurred !", e);
            }

            Intent intent = new Intent(mDownloadService.get(), VideoActivity.class);
            intent.putExtra("show", (Serializable) mShow);
            intent.putExtra("recorded", true);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mDownloadService.get());
            stackBuilder.addParentStack(VideoActivity.class);
            stackBuilder.addNextIntent(intent);

            String contentText = mDownloadService.get().getString(R.string.download_end);
            if (mShow.getShowPaid() == 1) {
                contentText += " - " + mShow.getPath();
            }
            NotificationCompat.Builder builder;
            mDownloadService.get().createNotificationChannel();
            if ("".equals(mError)) {
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(Integer.parseInt(mShow.getIdShow()), PendingIntent.FLAG_CANCEL_CURRENT);
                builder =
                        new NotificationCompat.Builder(mDownloadService.get(), Constants.CHANNEL_ID)
                                .setSmallIcon(R.drawable.icon_loading_end_notification)
                                .setContentTitle(mTextTitle)
                                .setContentText(contentText)
                                .setContentIntent(pendingIntent);
            } else {
                PendingIntent pendingIntent = PendingIntent.getActivity(mDownloadService.get(), 0, new Intent(), 0);
                builder =
                        new NotificationCompat.Builder(mDownloadService.get(), Constants.CHANNEL_ID)
                                .setSmallIcon(R.drawable.icon_loading_end_notification)
                                .setContentTitle(mTextTitle)
                                .setContentText(mError)
                                .setContentIntent(pendingIntent);
            }
            mNotificationManager.notify(Integer.parseInt(mShow.getIdShow()), builder.build());

            Intent serviceIntent = new Intent(mDownloadService.get(), DownloadService.class);
            serviceIntent.putExtra("type", "downloadNext");
            mDownloadService.get().mIsDownloading = false;
            mDownloadService.get().onStart(serviceIntent, 1);
        }
    }
}
