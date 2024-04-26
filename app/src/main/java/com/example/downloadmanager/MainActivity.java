package com.example.downloadmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    downloadAdapter downloadAdapter; //Creating downloadAdapter Object
    List<downloadModel> downloadModels = new ArrayList<>(); // Creating downloadModel ArrayList

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Regestering the broadcast receiver
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        //Accessing Add Download Item Button  RecyclerView
        Button add_download_list = findViewById(R.id.add_download_list);
        RecyclerView data_list = findViewById(R.id.data_list);
        //add Add Download Item Button Event listener; will display Dialog for input URL
        add_download_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });
        downloadAdapter = new downloadAdapter(MainActivity.this, downloadModels); //Intializing downloadAdaopter object
//setting layoutManager into RecyclerView Object
        data_list.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//setting adapter into RecyclerView
        data_list.setAdapter(downloadAdapter);


    }

    private void showInputDialog() {
//Creating alertdialog
        AlertDialog.Builder al = new AlertDialog.Builder(MainActivity.this);
        //Accessing input_dialog layout file & store into view object
        View view = getLayoutInflater().inflate(R.layout.input_dialog, null);
        al.setView(view); //Setting layout file into AlertDialog Object

        //Accessing the input or EditText from view object
        EditText editText = view.findViewById(R.id.input);
        //Accessing paste button object from view Object
        Button paste = view.findViewById(R.id.paste);
        //when Clicking paste button ,here passes the clipboard Text into EditText
        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating ClipboardManager object
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                try {
                    // accessing the text at 0 index in Clipboard
                    CharSequence charSequence = clipboardManager.getPrimaryClip().getItemAt(0).getText();
                    editText.setText(charSequence);//setting the text into Input Box
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //setting two button Positive & Negative in AlertDialog.As clicked on Positive button,it will start download else it will close the dialog
        al.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadFile(editText.getText().toString()); //Passing the editText Value into downloadFile() method
            }
        });
        al.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        al.show();
    }

    private void downloadFile(String url) {
//Accessing the File name from Url by Method URLUtil.guessFileName() and passing the URl
        String filename = URLUtil.guessFileName(url, null, null);
        //Creating download path string Varriable
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        //Creating File object & passing parent (download folder location ), then passing file name
        File file = new File(downloadPath, filename);
        //Declaring DownloadManager Request object
        DownloadManager.Request request = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)  //if android version is greater than Noughat we will use this download manager installation else we will use other installation
        {
            request = new DownloadManager.Request(Uri.parse(url))  //Passing download file url
                    .setTitle(filename) // setting title(file name)
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) //setting notification visibility to VISIBLE
                    .setDestinationUri(Uri.fromFile(file)) //setting destination path (which is File path)
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true);
        } else {
            request = new DownloadManager.Request(Uri.parse(url))
                    .setTitle(filename)
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationUri(Uri.fromFile(file))
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true);
        }
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);//Creating Download Manager Object
        //Passing Download Request Object in Download manager   enqueue() Method; This will return  the Download ID
        long downloadId = downloadManager.enqueue(request);
        downloadModel downloadModel = new downloadModel();//creating downloadModel object
        downloadModel.setId(11);
        downloadModel.setStatus("Downloading");
        downloadModel.setTitle(filename);
        downloadModel.setFile_size("0");
        downloadModel.setProgress("0");
        downloadModel.setIs_paused(false);
        downloadModel.setDownloadId(downloadId);
        downloadModel.setFile_path("");
        //adding downloadModel into downloadModel List & calling the method of downloadAdapter.notifyITemInserted() & then passing size of the list -1
        downloadModels.add(downloadModel);
        downloadAdapter.notifyItemInserted(downloadModels.size() - 1);

//Creating DownloadTask Object in downloadFile() Method & passing the DownloadModel

        DownloadStatusTask downloadStatusTask = new DownloadStatusTask(downloadModel);
//Calling the runTask() Method & passing DownloadTask object & Download ID
        runTask(downloadStatusTask, "" + downloadId);
    }

    //This task will update the progress of Downloading file
    public class DownloadStatusTask extends AsyncTask<String, String, String> {
        //creating DownloadStatusTask Constructor
        downloadModel downloadModel;

        public DownloadStatusTask(downloadModel downloadModel) {
            this.downloadModel = downloadModel;


        }

        @Override
        protected String doInBackground(String... strings) {
            //calling the method downloadFileProcess() & passing download id in doinBackground() Method
            downloadFileProcess(strings[0]);
            return null;
        }

        private void downloadFileProcess(String downloadId) {
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//creating DownloadManager Object
            boolean downloading = true;
            //now running this while loop until Download is not finished
            while (downloading) {
                DownloadManager.Query query = new DownloadManager.Query();// Creating DownloadManager Query Object
                query.setFilterById(Long.parseLong(downloadId));
//Creating Cursor object
                Cursor cursor = downloadManager.query(query);
                cursor.moveToFirst();// moves cursor to first position

                int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)); //Accessing downloaded size from Cursor
                int total_size = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));//total size of file

                //if downloadStatus of File is successful then change varriable to downloading=false
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false;
                }
//calculating download progress
                int progress = (int) ((bytes_downloaded * 100L) / total_size);
                String status = getStatusMessage(cursor);//Acess the status of file
                publishProgress(new String[]{String.valueOf(progress), String.valueOf(bytes_downloaded), status});
                cursor.close();  //closing cursor
            }
        }
        //overriding onProgressUpdate() Method

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //updating model data in publishProgress
            this.downloadModel.setFile_size(values[1]);
            this.downloadModel.setProgress(values[0]);
            //Update the status only when current status of file is  not equal to pause or resume
            if (!this.downloadModel.getStatus().equalsIgnoreCase("PAUSE") && !this.downloadModel.getStatus().equalsIgnoreCase("RESUME")) {
                this.downloadModel.setStatus(values[2]);
            }
            //calling this method in publishProgress from DownloadAdapter
            downloadAdapter.changeItem(downloadModel.getDownloadId());

        }
    }

    private String getStatusMessage(Cursor cursor) {
        String msg = "-"; //Creating msg String Variable
        //Creating Switch Condition to get the Status of File
        switch (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            //adding case for different condition
            //this method returns status of download
            case DownloadManager.STATUS_FAILED:
                msg = "Failed";
                break;
            case DownloadManager.STATUS_PAUSED:
                msg = "Paused";
                break;
            case DownloadManager.STATUS_RUNNING:
                msg = "Running";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                msg = "Completed";
                break;
            case DownloadManager.STATUS_PENDING:
                msg = "Pending";
                break;
            default:
                msg = "Unknown";
                break;

        }
        return msg;

    }

    //creating Broadcast Receiver Object ,this will work on Download Completed
    BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Acessing the DownloadCompleted ID from intent
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //checking this Download Id exist in our AdapterLIst
            boolean comp = downloadAdapter.ChangeItemWithStatus("Completed", id);

            if (comp) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(id));
                cursor.moveToFirst();
                String downloaded_path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                downloadAdapter.setChangeItemFilePath(downloaded_path, id);
            }

        }
    };
// on Destroy unregister the BroadcastReceiver


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }

    //Creating runTask() Method & passing DownloadTask object
    //in this method it will run multiple asyncTask at same time
    public void runTask(DownloadStatusTask downloadStatusTask, String id) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                downloadStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{id});
            } else {
                downloadStatusTask.execute(new String[]{id});
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}