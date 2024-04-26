package com.example.downloadmanager;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class downloadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<downloadModel> downloadModels = new ArrayList<>();

    public downloadAdapter(Context context, List<downloadModel> downloadModels) {
        this.context = context;
        this.downloadModels = downloadModels;
    }

    public class DownloadViewHolder extends RecyclerView.ViewHolder {
        //Declaration
        TextView file_title;
        TextView file_size;
        TextView file_status;
        ProgressBar file_progress;
        Button pause_resume;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            //Accessing Layout Elements by ID
            file_title = itemView.findViewById(R.id.file_title);
            file_size = itemView.findViewById(R.id.file_size);
            file_status = itemView.findViewById(R.id.file_status);
            file_progress = itemView.findViewById(R.id.file_progress);
            pause_resume = itemView.findViewById(R.id.pause_resume);

        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Accessing Layout file & storing into View object
        RecyclerView.ViewHolder vh;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_row1, parent, false);
        vh = new DownloadViewHolder(view);//passing View object in to DownloadViewHolder CLass Constructor
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        downloadModel downloadModel = downloadModels.get(position);//Accessing the DownloadModel Object at Position
        DownloadViewHolder downloadViewHolder = (DownloadViewHolder) holder;//Accessing DownloadViewHolder object from holder

        //Setting Data of DownloadModel into DownloadViewHolder object
        downloadViewHolder.file_title.setText(downloadModel.getTitle());
        downloadViewHolder.file_size.setText("downloaded" + downloadModel.getFile_size());
        downloadViewHolder.file_status.setText(downloadModel.getStatus());
        downloadViewHolder.file_progress.setProgress(Integer.parseInt(downloadModel.getProgress()));
        if (downloadModel.isIs_paused()) {
            downloadViewHolder.pause_resume.setText("RESUME");
        } else {
            downloadViewHolder.pause_resume.setText("PAUSE");
        }
        //Checking when Status is Resume then it will Set Status to Running
        if (downloadModel.getStatus().equalsIgnoreCase("RESUME")) {
            downloadViewHolder.file_status.setText("Running");
        }
//Adding PAUSE_RESUME Button Event Listener
// when Status is Pause ,it will change to Resume & Vice-versa
        downloadViewHolder.pause_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloadModel.isIs_paused()) {
                    downloadModel.setIs_paused(false);
                    downloadViewHolder.pause_resume.setText("PAUSE");
                    downloadModel.setStatus("RESUME");
                    downloadViewHolder.file_status.setText("Running");
                    if (!resumeDownload(downloadModel)) {
                        Toast.makeText(context, "Failed to resume", Toast.LENGTH_SHORT).show();
                    }

                    notifyItemChanged(position);

                } else {
                    downloadModel.setIs_paused(true);
                    downloadViewHolder.pause_resume.setText("RESUME");
                    downloadModel.setStatus("PAUSE");
                    downloadViewHolder.file_status.setText("PAUSE");
                    if (!pauseDownload(downloadModel)) {
                        Toast.makeText(context, "Failed to Pause", Toast.LENGTH_SHORT).show();
                    }
                    notifyItemChanged(position);//calling notifyItemChanged() method & passing position

                }
            }
        });

    }

    private boolean pauseDownload(downloadModel downloadModel) {
        int updateRow = 0;
        //Creating Content values object
        ContentValues contentValues = new ContentValues();
        //Passing controlkey value,1=pause_download, 0=Resume_download
        contentValues.put("control", 1);
        try {
            //Updating download directory file,download status by title
//Passing the content value object & title
            updateRow = context.getContentResolver().update(Uri.parse("content://downloads/my_downloads"), contentValues, "title=?", new String[]{downloadModel.getTitle()});
        } catch (Exception e) {
            e.printStackTrace();
        }
//if updateRow>0;it will return true,otherwise false
        return 0 < updateRow;
    }

    private boolean resumeDownload(downloadModel downloadModel) {
        int updateRow = 0;

        ContentValues contentValues = new ContentValues();

        //Passing controlkey value,1=pause_download, 0=Resume_download

        contentValues.put("controll", 0);
        try {
            //Updating download directory file,download status by title
//Passing the content value object & title
            updateRow = context.getContentResolver().update(Uri.parse("content://downloads/my_downloads"), contentValues, "title=?", new String[]{downloadModel.getTitle()});

        } catch (Exception e) {
            e.printStackTrace();

        }
//if updateRow>0;it will return true,otherwise false
        return 0 < updateRow;
    }

    @Override
    public int getItemCount() {

        return downloadModels.size(); //returning size of list
    }

    //Creating Method for NotifyItemChanged by DownloadId at specific position
    public void changeItem(long downloadId) {
        int i = 0;
        //creating For loop and finding the position of Download ID so calling notifyItemChange() and passing position
        for (downloadModel downloadModel : downloadModels) {
            if (downloadId == downloadModel.getDownloadId()) {
                notifyItemChanged(i);
            }
            i++;
        }
    }

    //when file status is changed ,it will return true
    public boolean ChangeItemWithStatus(String message, long downloadId) {
        boolean comp = false;
        int i = 0;
        //creating For loop and finding the position of Download ID so calling notifyItemChange() and passing position
        for (downloadModel downloadModel : downloadModels) {
            if (downloadId == downloadModel.getDownloadId()) {
                downloadModels.get(i).setStatus(message);
                notifyItemChanged(i);
                comp = true;
            }
            i++;
        }
        return comp;
    }

    public void setChangeItemFilePath(String path, long id) {
        int i = 0;
        //creating For loop and finding the position of Download ID so calling notifyItemChange() and passing position
        for (downloadModel downloadModel : downloadModels) {
            if (id == downloadModel.getDownloadId()) {
                downloadModels.get(i).setFile_path(path);//updating file path now
                notifyItemChanged(i);
            }
            i++;
        }
    }

}

