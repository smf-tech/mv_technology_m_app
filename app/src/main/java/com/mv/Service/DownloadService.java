package com.mv.Service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.mv.Activity.CommunityHomeActivity;
import com.mv.Activity.SalaryDetailActivity;
import com.mv.ActivityMenu.ThetSavandFragment;
import com.mv.ActivityMenu.TrainingFragment;
import com.mv.Model.Download;
import com.mv.R;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.UnzipUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class DownloadService extends IntentService {

    public DownloadService() {
        super("Download Service");
    }

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private int totalFileSize;
    private String url, fragment_flag;
    private String StorezipFileLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/";
    private String fileName;
    private String tempFileName;
    private String DirectoryName = Environment.getExternalStorageDirectory() + "/MV/UnZip/";
    private String filetype;
    private Intent intent;

    @Override
    protected void onHandleIntent(Intent intent) {

        int time = (int) (System.currentTimeMillis());
        tempFileName =  new Timestamp(time).toString();
        url = intent.getStringExtra("URL");
        fileName = intent.getStringExtra("FILENAME");
        filetype = intent.getStringExtra("FILETYPE");
        fragment_flag = intent.getStringExtra("fragment_flag");
//        StorezipFileLocation = StorezipFileLocation + fileName;
        StorezipFileLocation = StorezipFileLocation + tempFileName;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "mv";
            String description = "blod";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("MV", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        notificationBuilder = new NotificationCompat.Builder(this,"MV")
                .setSmallIcon(R.mipmap.app_logo)
                .setContentTitle("Download")
                .setContentText("Downloading " + fileName)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
        initDownload();
    }

    private final static AtomicInteger c = new AtomicInteger(0);

    private static int getID() {
        return c.incrementAndGet();
    }

    private void initDownload() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://download.learn2crack.com/")
                .build();

        ServiceRequest retrofitInterface = retrofit.create(ServiceRequest.class);

        Call<ResponseBody> request = retrofitInterface.downloadFile(url);

        try {

            downloadFile(request.execute().body());

        } catch (IOException e) {

            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void downloadFile(ResponseBody body) throws IOException {

        int count;
        byte data[] = new byte[1024 * 4];
        if (body != null) {
            long fileSize = body.contentLength();

            InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
            File outputFile = new File(StorezipFileLocation);
            Log.i("outputFile", outputFile.getAbsolutePath());
            if (outputFile.exists()) {
                boolean delete = outputFile.delete();
                System.out.print("File deleted ->" + delete);
            }
            OutputStream output = new FileOutputStream(outputFile);
            long total = 0;
            long startTime = System.currentTimeMillis();
            int timeCount = 1;
            while ((count = bis.read(data)) != -1) {

                total += count;
                totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
                double current = Math.round(total / (Math.pow(1024, 2)));

                int progress = (int) ((total * 100) / fileSize);

                long currentTime = System.currentTimeMillis() - startTime;

                Download download = new Download();
                download.setTotalFileSize(totalFileSize);

                if (currentTime > 1000 * timeCount) {

                    download.setCurrentFileSize((int) current);
                    download.setProgress(progress);
                    sendNotification(download);
                    timeCount++;
                }

                output.write(data, 0, count);
            }
            onDownloadComplete();
            output.flush();
            output.close();
            bis.close();
        }
    }

    private void sendNotification(Download download) {

//        sendIntent(download);
        notificationBuilder.setProgress(100, download.getProgress(), false);
        notificationBuilder.setContentText("Downloading file " + download.getCurrentFileSize() + "/" + totalFileSize + " MB");

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private void sendIntent(Download download) {

        if (fragment_flag != null) {
            if (fragment_flag.equalsIgnoreCase("ThetSanvad_Fragment")) {
                intent = new Intent(ThetSavandFragment.MESSAGE_PROGRESS);
            } else if (fragment_flag.equalsIgnoreCase("Training_Fragment")) {
                intent = new Intent(TrainingFragment.MESSAGE_PROGRESS);

            } else if (fragment_flag.equalsIgnoreCase("My_Community")) {
                intent = new Intent(CommunityHomeActivity.MESSAGE_PROGRESS);
            } else if (fragment_flag.equalsIgnoreCase("Salary_Detail_Activity")){
                intent = new Intent(SalaryDetailActivity.MESSAGE_PROGRESS);
            }
            intent.putExtra("download", download);

            LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
        }
    }

    private void onDownloadComplete() {
        if (notificationManager != null) {
            Download download = new Download();
            download.setProgress(100);
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/");
            if(dir.exists()){
                File from = new File(dir,tempFileName);
                File to = new File(dir,fileName);
                if(from.exists())
                    from.renameTo(to);
            }
            sendIntent(download);
            notificationManager.cancel(0);
            notificationBuilder.setProgress(0, 0, false);
            notificationBuilder.setContentText(fileName + " downloaded");
            notificationManager.notify(0, notificationBuilder.build());

            if (filetype.equalsIgnoreCase("zip")) {
                startUnZipping();
            }
        }
    }

    private void startUnZipping() {
        String filePath = StorezipFileLocation;
        String destinationPath = DirectoryName;

        File archive = new File(filePath);
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, destinationPath);
            }
            UnzipUtil d = new UnzipUtil(StorezipFileLocation, DirectoryName);
            d.unzip();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

    }

    private void createDir(File dir) {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (notificationManager != null) {
            notificationManager.cancel(0);
        }
    }
}
