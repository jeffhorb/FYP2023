package com.ecom.fyp2023.Adapters;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.ModelClasses.FileModel;
import com.ecom.fyp2023.R;
import com.ecom.fyp2023.TextEditor.RichEditorFileEditor;

import java.io.File;
import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {
    private List<FileModel> fileList;
    private Context context;

    public FilesAdapter(List<FileModel> fileList, Context context) {

        this.fileList = fileList;
        this.context = context;
    }

    public void updateList(List<FileModel> itemList) {
        this.fileList = itemList;
        notifyDataSetChanged();

    }


    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileModel file = fileList.get(position);
        holder.fileNameTextView.setText(file.getFileName());

        String fileExtension = getFileExtension(file.getFileName());
        int iconResource = getFileIconResource(fileExtension);
        holder.fileIconImageView.setImageResource(iconResource);

        holder.more.setOnClickListener(v -> showPopupMenu(holder.more, holder.getAdapterPosition()));

    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;
        ImageView fileIconImageView,more;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            fileIconImageView = itemView.findViewById(R.id.fileIconImageView);
            more = itemView.findViewById(R.id.more);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileModel file = fileList.get(getAdapterPosition());
                    Intent intent = new Intent(itemView.getContext(), RichEditorFileEditor.class);
                    intent.putExtra("filePath",file.getFilePath());
                    itemView.getContext().startActivity(intent);

                }
            });
        }
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_file, popupMenu.getMenu());

        // Set up a click listener for the menu items
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.downloadFile) {

                FileModel file = fileList.get(position);
                downloadFile(file.getDownloadUrl(),file.getFileName());

                return true;
            }else if(id == R.id.deleteFile){

                FileModel file = fileList.get(position);
                Intent intent = new Intent(context, RichEditorFileEditor.class);
                intent.putExtra("filePath",file.getFilePath());
                context.startActivity(intent);

                return true;
            }

            return false;
        });
        popupMenu.show();
    }

    @NonNull
    private String getFileExtension(@NonNull String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "";
    }

    private int getFileIconResource(@NonNull String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case "pdf":
                return R.drawable._72699_pdf_icon;
            case "doc":
            case "docx":
                return R.drawable._14538_document_docx_file_icon;
            case "xls":
            case "xlsx":
                return R.drawable._422413_exel_spreadsheet_sheets_table_icon;
            case "ppt":
            case "pptx":
                return R.drawable._375048_logo_powerpoint_icon;
            default:
                return R.drawable.twotone_insert_drive_file_24;
        }
    }
    private void downloadFile(String fileUrl, String fileName) {
        // Create a download request using the provided URL
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setTitle(fileName);
        request.setDescription("Downloading file");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Set the destination directory for the downloaded file
        File destinationDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        request.setDestinationUri(Uri.fromFile(new File(destinationDirectory, fileName)));

        // Get the download service and enqueue the download request
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(context, "Downloading file...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to start download", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFile(File file) {
        // Implement code to open the file (e.g., using Intent to open a PDF viewer)
        // For example, if you want to open a PDF file, you can use:
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No PDF viewer installed", Toast.LENGTH_SHORT).show();
        }
    }


}

