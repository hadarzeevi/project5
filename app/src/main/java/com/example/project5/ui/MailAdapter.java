
package com.example.project5.ui;

import android.graphics.Typeface;
import android.util.Log;
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
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {
    private String searchQuery = null;

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

        String senderDisplay;
        if (mail.firstName != null && !mail.firstName.isEmpty()) {
            senderDisplay = mail.firstName + " " + mail.lastName;
        } else if (mail.fromMail != null && !mail.fromMail.isEmpty()) {
            senderDisplay = mail.fromMail;
        } else {
            senderDisplay = mail.from != null ? mail.from : "(Unknown)";
        }
        holder.tvSender.setText(getHighlightedText(senderDisplay, searchQuery));


        // נושא ו-preview
        holder.tvSubject.setText(getHighlightedText(mail.subject, searchQuery));
        holder.tvPreview.setText(getHighlightedText(mail.preview, searchQuery));

        // תאריך
        holder.tvDate.setText(mail.date != null ? mail.date : "");

        // הדגשה אם לא נקרא
        if (!mail.isRead) {
            holder.tvSender.setTypeface(null, Typeface.BOLD);
            holder.tvSubject.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvSender.setTypeface(null, Typeface.NORMAL);
            holder.tvSubject.setTypeface(null, Typeface.NORMAL);
        }

        // הצגת תמונת פרופיל
        if (mail.profileImage != null && !mail.profileImage.isEmpty()) {
            String url = "http://10.0.2.2:4000/" + mail.profileImage.replace("\\", "/");
            Log.d("MailAdapter", "Loading image from URL: " + url);
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_avatar);
        }
        Log.d("MailAdapter", "profileImage path: " + mail.profileImage);

        // כוכב
        holder.ivStar.setImageResource(mail.isStarred ? R.drawable.ic_star : R.drawable.ic_star_border);

        // הצגת אייקון קובץ אם יש קובץ מצורף
        holder.ivAttachment.setVisibility(mail.hasAttachment ? View.VISIBLE : View.GONE);

        // האזנה להקלקות
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
    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query.toLowerCase() : null;
        notifyDataSetChanged();
    }
    private CharSequence getHighlightedText(String text, String query) {
        if (text == null || query == null || query.isEmpty()) return text;

        String lowerText = text.toLowerCase();
        int start = lowerText.indexOf(query);
        if (start < 0) return text;

        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        while (start >= 0) {
            int end = start + query.length();
            builder.setSpan(
                    new BackgroundColorSpan(0xFFFFFF00),  // צהוב
                    start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            start = lowerText.indexOf(query, end);
        }
        return builder;
    }

}

