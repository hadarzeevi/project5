package com.example.project5.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Mail {
    public String id;
    public String from;
    public String fromMail;  // ✅ הוספנו כאן
    public String subject;
    public String preview;
    public String body;
    public String date;
    public boolean isStarred;
    public boolean isRead;
    public boolean hasAttachment;

    // נתוני השולח
    public String firstName;
    public String lastName;
    public String mail;
    public String profileImage;
    public String username;

    public static Mail fromJson(JSONObject obj) throws JSONException {
        Mail mail = new Mail();
        mail.id = obj.optString("_id", "");
        mail.from = obj.optString("from", "");
        mail.fromMail = obj.optString("fromMail", "");
        mail.subject = obj.optString("subject", "");
        mail.preview = obj.optString("preview", "");
        mail.body = obj.optString("body", "");
        mail.date = obj.optString("date", "");
        mail.isStarred = obj.optBoolean("isStarred", false);
        mail.isRead = obj.optBoolean("isRead", false);
        mail.hasAttachment = obj.optBoolean("hasAttachment", false);

        if (obj.has("sender")) {
            JSONObject sender = obj.getJSONObject("sender");
            mail.firstName = sender.optString("first_name", "");
            mail.lastName = sender.optString("last_name", "");
            mail.mail = sender.optString("mail", "");
            mail.profileImage = sender.optString("profileImage", "");
        }

        return mail;
    }
}
