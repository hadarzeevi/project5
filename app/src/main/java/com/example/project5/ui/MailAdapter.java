package com.example.project5.ui;

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
import java.util.function.Consumer; // חשוב

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private final List<Mail> mailList;
    private final Consumer<Mail> onMailClicked;
    private final Consumer<Mail> onStarClicked;

    public MailAdapter(List<Mail> mailList, Consumer<Mail> onMailClicked, Consumer<Mail> onStarClicked) {
        this.mailList = mailList;
        this.onMailClicked = onMailClicked;
        this.onStarClicked = onStarClicked;
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
            holder.ivStar.setImageResource(R.drawable.ic_star);
        } else {
            holder.ivStar.setImageResource(R.drawable.ic_star);
        }

        holder.ivAttachment.setVisibility(mail.hasAttachment ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> onMailClicked.accept(mail));
        holder.ivStar.setOnClickListener(v -> onStarClicked.accept(mail));
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
