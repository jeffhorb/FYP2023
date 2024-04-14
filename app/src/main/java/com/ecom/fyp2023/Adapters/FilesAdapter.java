package com.ecom.fyp2023.Adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.ModelClasses.FileModel;
import com.ecom.fyp2023.R;
import com.ecom.fyp2023.VersionControlClasses.RichEditorFileEditor;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
                    FirestoreManager firestoreManager = new FirestoreManager();

                    firestoreManager.getDocumentId("files", "fileName", file.getFileName(), documentId -> {
                        if (documentId != null) {
                            Intent intent = new Intent(itemView.getContext(), RichEditorFileEditor.class);
                            intent.putExtra("filePath",file.getFilePath());
                            intent.putExtra("filesDocId",documentId);
                            itemView.getContext().startActivity(intent);

                        }
                    });

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
                downloadFile(file.getDownloadUrl(), file.getFileName());

                return true;
            }else if (id == R.id.deleteFile) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirm Delete");
                    builder.setMessage("Are you sure you want to delete this file?");

                    // Add the buttons
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked Delete button
                            FileModel file = fileList.get(position);
                            String filePath = file.getFilePath();

                            // Delete file from Firebase Storage
                            deleteFileFromStorage(filePath);

                            FirestoreManager firestoreManager = new FirestoreManager();
                            firestoreManager.getDocumentId("files", "fileName", file.getFileName(), documentId -> {
                                if (documentId != null) {

                                    // Delete file metadata from Firestore
                                    deleteFileMetadataFromFirestore(documentId);
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked Cancel button
                            dialog.dismiss();
                        }
                    });

                    // Create and show the dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;

            } else if (id == R.id.changeFileName) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Enter New File Name");

                // Set up the input
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newFileName = input.getText().toString();
                        if (!TextUtils.isEmpty(newFileName)) {
                            FileModel file = fileList.get(position);
                            String oldFileName = file.getFileName();
                            String fileExtension = getFileExt(oldFileName); // Get the file extension

                            // Remove the extension from the old file name
                            String oldFileNameWithoutExtension = oldFileName.substring(0, oldFileName.lastIndexOf('.'));

                            // Concatenate the new file name with the existing extension
                            String newFileNameWithExtension = newFileName + fileExtension;

                            // Get a reference to the Firestore collection
                            CollectionReference filesRef = FirebaseFirestore.getInstance().collection("files");

                            // Update the file name in Firestore
                            filesRef.whereEqualTo("fileName", oldFileName)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                            // Update the file name
                                            document.getReference().update("fileName", newFileNameWithExtension)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Update the file name in the RecyclerView
                                                        file.setFileName(newFileNameWithExtension);
                                                        notifyDataSetChanged(); // Notify adapter about the change
                                                        Toast.makeText(context, "File name updated successfully", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(context, "Failed to update file name", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to update file name", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(context, "Please enter a valid file name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void deleteFileFromStorage(String filePath) {
        // Get a reference to the file in Firebase Storage
        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(filePath);

        // Delete the file
        fileRef.delete().addOnSuccessListener(aVoid -> {
            // File deleted successfully
            Toast.makeText(context, "File deleted from storage", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            // Error occurred while deleting the file
            Toast.makeText(context, "Failed to delete file from storage: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteFileMetadataFromFirestore(String fileId) {
        // Get a reference to the Firestore collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference filesRef = db.collection("files");
        CollectionReference versionsRef = filesRef.document(fileId).collection("versions");

        // Delete all documents in the 'versions' sub-collection
        versionsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                WriteBatch batch = db.batch();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    batch.delete(document.getReference());
                }
                batch.commit().addOnCompleteListener(deleteTask -> {
                    if (deleteTask.isSuccessful()) {
                        // 'versions' sub-collection deleted successfully, now delete the file document
                        filesRef.document(fileId).delete().addOnSuccessListener(aVoid -> {
                            // File document deleted successfully
                            Toast.makeText(context, "File deleted from Firestore", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            // Error occurred while deleting file document
                            Toast.makeText(context, "Failed to delete file from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // Error occurred while deleting 'versions' sub-collection
                        Toast.makeText(context, "Failed to delete versions from Firestore: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Error occurred while getting 'versions' sub-collection
                Toast.makeText(context, "Failed to get versions from Firestore: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to get file extension
    @NonNull
    private String getFileExt(@NonNull String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf("."));
        } else {
            return "";
        }
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

}

