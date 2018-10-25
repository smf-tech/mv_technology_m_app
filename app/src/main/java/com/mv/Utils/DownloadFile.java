package com.mv.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.mv.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Rohit Gujar on 30-11-2017.
 */

public class DownloadFile {
    private Context context;
    private ProgressDialog mProgressDialog;
    private String StorezipFileLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/";
    private String DirectoryName = Environment.getExternalStorageDirectory() + "/MV/UnZip/";
    private ProgressDialog pDialog;
    private DownloadFileFromURL downloadFileFromURL;

    public DownloadFile(Context cntx) {
        this.context = cntx;
        pDialog = new ProgressDialog(context);
        pDialog.setTitle("Downloading file...");
        pDialog.setMessage("Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                downloadFileFromURL.cancel(true);
            }
        });
        pDialog.setCancelable(false);
    }

    public void startDownload(String link, String name) {
        downloadFileFromURL = new DownloadFileFromURL();
        downloadFileFromURL.execute(link, name);
    }


    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                String fileName = f_url[1];
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);
                StorezipFileLocation = StorezipFileLocation + fileName;
                // String file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/download.zip";
                if (new File(StorezipFileLocation).exists())
                    new File(StorezipFileLocation).delete();
                // Output stream
                OutputStream output = new FileOutputStream(StorezipFileLocation);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called

                    Log.i("onProgressTotal", "" + total);
                    Log.i("onProgressLenghtOfFile", "" + lenghtOfFile);
                    Log.i("onProgressSend", "" + total / lenghtOfFile);
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                return "false";
            }

            return "true";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            Log.i("onProgressUpdate", "" + Integer.parseInt(progress[0]));
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            pDialog.dismiss();

            if (file_url.equalsIgnoreCase("true")) {
                try {
                    unzip();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    public void unzip() {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setMessage("Unzipping Files...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        new UnZipTask().execute(StorezipFileLocation, DirectoryName);
    }

    private class UnZipTask extends AsyncTask<String, Void, Boolean> {
        @SuppressWarnings("rawtypes")
        @Override
        protected Boolean doInBackground(String... params) {
            String filePath = params[0];
            String destinationPath = params[1];

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
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();

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

            // Log.v("", "Extracting: " + entry);
            BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            try {

            } finally {
                outputStream.flush();
                outputStream.close();
                inputStream.close();
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
    }
}
