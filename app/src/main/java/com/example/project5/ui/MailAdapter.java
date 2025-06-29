//package com.example.project5.ui;
//
//import android.graphics.Typeface;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.project5.R;
//import com.example.project5.model.Mail;
//import java.util.List;
//import java.util.function.Consumer; // חשוב
//
//public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {
//
//    private final List<Mail> mailList;
//    private final Consumer<Mail> onMailClicked;
//    private final Consumer<Mail> onStarClicked;
//
//    public MailAdapter(List<Mail> mailList, Consumer<Mail> onMailClicked, Consumer<Mail> onStarClicked) {
//        this.mailList = mailList;
//        this.onMailClicked = onMailClicked;
//        this.onStarClicked = onStarClicked;
//    }
//
//    @NonNull
//    @Override
//    public MailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mail, parent, false);
//        return new MailViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MailViewHolder holder, int position) {
//        Mail mail = mailList.get(position);
//
//        holder.tvSender.setText(mail.from);
//        holder.tvSubject.setText(mail.subject);
//        holder.tvPreview.setText(mail.preview);
//        holder.tvDate.setText(mail.date);
//
//        if (!mail.isRead) {
//            holder.tvSender.setTypeface(null, Typeface.BOLD);
//            holder.tvSubject.setTypeface(null, Typeface.BOLD);
//        } else {
//            holder.tvSender.setTypeface(null, Typeface.NORMAL);
//            holder.tvSubject.setTypeface(null, Typeface.NORMAL);
//        }
//
//        if (mail.isStarred) {
//            holder.ivStar.setImageResource(R.drawable.ic_star);
//        } else {
//            holder.ivStar.setImageResource(R.drawable.ic_star);
//        }
//
//        holder.ivAttachment.setVisibility(mail.hasAttachment ? View.VISIBLE : View.GONE);
//
//        holder.itemView.setOnClickListener(v -> onMailClicked.accept(mail));
//        holder.ivStar.setOnClickListener(v -> onStarClicked.accept(mail));
//    }
//
//    @Override
//    public int getItemCount() {
//        return mailList.size();
//    }
//
//    public static class MailViewHolder extends RecyclerView.ViewHolder {
//        TextView tvSender, tvSubject, tvPreview, tvDate;
//        ImageView ivStar, ivAttachment;
//
//        public MailViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvSender = itemView.findViewById(R.id.tvSender);
//            tvSubject = itemView.findViewById(R.id.tvSubject);
//            tvPreview = itemView.findViewById(R.id.tvPreview);
//            tvDate = itemView.findViewById(R.id.tvDate);
//            ivStar = itemView.findViewById(R.id.ivStar);
//            ivAttachment = itemView.findViewById(R.id.ivAttachment);
//        }
//    }
//}

package com.example.project5.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project5.R;
import com.example.project5.model.Mail;

import org.json.JSONObject;

import java.util.List;
import java.util.function.Consumer;

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private final List<Mail> mailList;
    private final Consumer<Mail> onMailClicked;
    private final Consumer<Mail> onStarClicked;

    private final Context context;
    public MailAdapter(Context context, List<Mail> mailList, Consumer<Mail> onMailClicked, Consumer<Mail> onStarClicked) {
        this.context = context;
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

        // הצגת שם שולח
        if (mail.firstName != null && !mail.firstName.isEmpty()) {
            holder.tvSender.setText(mail.firstName + " " + mail.lastName);
        } else if (mail.fromMail != null && !mail.fromMail.isEmpty()) {
            holder.tvSender.setText(mail.fromMail);
        } else {
            holder.tvSender.setText(mail.from != null ? mail.from : "(Unknown)");
        }

        // נושא ו-preview
        holder.tvSubject.setText(mail.subject != null ? mail.subject : "(No Subject)");
        holder.tvPreview.setText(mail.preview != null ? mail.preview : "");

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
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_avatar)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_avatar);
        }

        // כוכב
        holder.ivStar.setImageResource(mail.isStarred ? R.drawable.ic_star : R.drawable.ic_star_border);

        // הצגת אייקון קובץ אם יש קובץ מצורף
        holder.ivAttachment.setVisibility(mail.hasAttachment ? View.VISIBLE : View.GONE);

        // האזנה להקלקות
        holder.itemView.setOnClickListener(v -> {
            try {
                Log.d("MAIL_ADAPTER", "Building intent");
                Intent intent = new Intent(holder.itemView.getContext(), MailDetailActivity.class);
                JSONObject mailJson = mail.toJson();
                Log.d("MAIL_ADAPTER", "Mail JSON: " + mailJson.toString());
                intent.putExtra("MAIL_JSON", mailJson.toString());
                holder.itemView.getContext().startActivity(intent);
            } catch (Exception e) {
                Log.e("MAIL_ADAPTER", "Error opening mail", e);
                Toast.makeText(holder.itemView.getContext(), "שגיאה בפתיחת מייל", Toast.LENGTH_SHORT).show();
            }
        });


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

