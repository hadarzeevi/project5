package com.example.project5.ui;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project5.R;
import com.example.project5.model.Mail;

import java.util.List;
import java.util.function.Consumer;

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

        // הצגת שם משתמש בבולד ובגדול יותר
        if (mail.username != null && !mail.username.isEmpty()) {
            holder.tvSender.setText(mail.username);
        } else
            if (mail.fromMail != null && !mail.fromMail.isEmpty()) {
            holder.tvSender.setText(mail.fromMail);
        } else {
            holder.tvSender.setText(mail.from != null ? mail.from : "(Unknown)");
        }
        holder.tvSender.setTypeface(null, Typeface.BOLD);
        holder.tvSender.setTextSize(18); // גודל גדול יותר כדי להדגיש

        // נושא ו-preview
        holder.tvSubject.setText(mail.subject != null ? mail.subject : "(No Subject)");
        holder.tvPreview.setText(mail.preview != null ? mail.preview : "");

        // תאריך
        holder.tvDate.setText(mail.date != null ? mail.date : "");

        // הדגשה אם לא נקרא
        if (!mail.isRead) {
            holder.tvSubject.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvSubject.setTypeface(null, Typeface.NORMAL);
        }

        // טעינת תמונת פרופיל עם Glide
        if (mail.profileImage != null && !mail.profileImage.isEmpty()) {
            String url = "http://10.0.2.2:4000/" + mail.profileImage.replace("\\", "/");
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_avatar);
        }

        // כוכב
        holder.ivStar.setImageResource(mail.isStarred ? R.drawable.ic_star : R.drawable.ic_star_border);

        // הצגת אייקון קובץ אם יש קובץ מצורף
        holder.ivAttachment.setVisibility(mail.hasAttachment ? View.VISIBLE : View.GONE);

        // האזנה לקליקים
        holder.itemView.setOnClickListener(v -> onMailClicked.accept(mail));
        holder.ivStar.setOnClickListener(v -> onStarClicked.accept(mail));
    }

    @Override
    public int getItemCount() {
        return mailList.size();
    }

    public static class MailViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivStar, ivAttachment;
        TextView tvSender, tvSubject, tvPreview, tvDate;

        public MailViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivStar = itemView.findViewById(R.id.ivStar);
            ivAttachment = itemView.findViewById(R.id.ivAttachment);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvPreview = itemView.findViewById(R.id.tvPreview);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
