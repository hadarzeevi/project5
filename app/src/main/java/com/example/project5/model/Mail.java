package com.example.project5.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Mail {
    public String id;
    public String from;
    public String subject;
    public String preview;
    public String date;
    public boolean isStarred;
    public boolean isRead;
    public boolean hasAttachment;

    public static Mail fromJson(JSONObject obj) throws JSONException {
        Mail mail = new Mail();
        mail.id = obj.getString("_id");
        mail.from = obj.getString("from");
        mail.subject = obj.getString("subject");
        mail.preview = obj.getString("preview");
        mail.date = obj.getString("date");
        mail.isStarred = obj.getBoolean("isStarred");
        mail.isRead = obj.getBoolean("isRead");
        mail.hasAttachment = obj.getBoolean("hasAttachment");
        return mail;
    }
}
