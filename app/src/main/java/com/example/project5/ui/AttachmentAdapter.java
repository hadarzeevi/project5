package com.example.project5.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project5.R;
import com.example.project5.model.Attachment;

import java.util.List;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder> {

    private List<Attachment> attachments;
    private OnAttachmentClickListener listener;

    public interface OnAttachmentClickListener {
        void onAttachmentClick(Attachment attachment);
    }

    public AttachmentAdapter(List<Attachment> attachments, OnAttachmentClickListener listener) {
        this.attachments = attachments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachment, parent, false);
        return new AttachmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        Attachment attachment = attachments.get(position);
        holder.bind(attachment);
    }

    @Override
    public int getItemCount() {
        return attachments != null ? attachments.size() : 0;
    }

    class AttachmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName, tvFileSize;
        ImageView ivFileIcon;

        public AttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
            ivFileIcon = itemView.findViewById(R.id.ivFileIcon);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAttachmentClick(attachments.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Attachment attachment) {
            //name
            String displayName = attachment.filename;
            if (displayName == null || displayName.isEmpty()) {
                displayName = attachment.path != null ? attachment.path : "קובץ מצורף";
            }
            tvFileName.setText(displayName);
            // size
            tvFileSize.setText(formatFileSize(attachment.size));
            setFileIcon(attachment);

        }

        private void setFileIcon(Attachment attachment) {
            if (attachment.contentType != null && !attachment.contentType.isEmpty()) {
                if (attachment.contentType.startsWith("image/")) {
                    ivFileIcon.setImageResource(android.R.drawable.ic_menu_gallery);
                } else if (attachment.contentType.equals("application/pdf")) {
                    ivFileIcon.setImageResource(android.R.drawable.ic_menu_report_image);
                } else if (attachment.contentType.startsWith("text/")) {
                    ivFileIcon.setImageResource(android.R.drawable.ic_menu_edit);
                } else if (attachment.contentType.startsWith("video/")) {
                    ivFileIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
                } else if (attachment.contentType.startsWith("audio/")) {
                    ivFileIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                } else {
                    ivFileIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                }
            } else {
                String filename = attachment.filename != null ? attachment.filename : attachment.path;
                if (filename != null) {
                    String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
                    switch (extension) {
                        case "jpg":
                        case "jpeg":
                        case "png":
                        case "gif":
                        case "bmp":
                            ivFileIcon.setImageResource(android.R.drawable.ic_menu_gallery);
                            break;
                        case "pdf":
                            ivFileIcon.setImageResource(android.R.drawable.ic_menu_report_image);
                            break;
                        case "txt":
                        case "doc":
                        case "docx":
                            ivFileIcon.setImageResource(android.R.drawable.ic_menu_edit);
                            break;
                        case "mp4":
                        case "avi":
                        case "mkv":
                            ivFileIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
                            break;
                        case "mp3":
                        case "wav":
                        case "flac":
                            ivFileIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                            break;
                        default:
                            ivFileIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                    }
                } else {
                    ivFileIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                }
            }
        }

        private String formatFileSize(long size) {
            if (size <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
        }
    }
}