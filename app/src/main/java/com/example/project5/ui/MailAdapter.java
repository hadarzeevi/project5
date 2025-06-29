package com.example.project5.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project5.R;
import com.example.project5.model.Mail;
import java.util.List;

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    public interface OnMailActionListener {
        void onMailClicked(Mail mail);
        void onStarClicked(Mail mail);
    }

    private List<Mail> mailList;
    private OnMailActionListener listener;

    public MailAdapter(List<Mail> mailList, OnMailActionListener listener) {
        this.mailList = mailList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mail, parent, false);
        return new MailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MailViewHolder holder, int position) {
        Mail mail = mailList.get(position);

        holder.tvSender.setText(mail.from);
        holder.tvSubject.setText(mail.subject);
        holder.tvPreview.setText(mail.preview);
        holder.tvDate.setText(mail.date);

        if (!mail.isRead) {
            holder.tvSender.setTypeface(null, Typeface.BOLD);
            holder.tvSubject.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvSender.setTypeface(null, Typeface.NORMAL);
            holder.tvSubject.setTypeface(null, Typeface.NORMAL);
        }

        if (mail.isStarred) {
            holder.ivStar.setImageResource(R.drawable.ic_star); // ------?
            // Not got! need to change it to ic_star_filled
        } else {
            holder.ivStar.setImageResource(R.drawable.ic_star);
        }

        holder.ivAttachment.setVisibility(mail.hasAttachment ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onMailClicked(mail));
        holder.ivStar.setOnClickListener(v -> listener.onStarClicked(mail));
    }

    @Override
    public int getItemCount() {
        return mailList.size();
    }

    public static class MailViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvSubject, tvPreview, tvDate;
        ImageView ivStar, ivAttachment;

        public MailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvPreview = itemView.findViewById(R.id.tvPreview);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivStar = itemView.findViewById(R.id.ivStar);
            ivAttachment = itemView.findViewById(R.id.ivAttachment);
        }
    }
}
