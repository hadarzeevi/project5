package com.example.project5.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Mail {
    public String id;
    public String from;
    public String fromMail;
    public String subject;
    public String preview;
    public String body;
    public String date;
    public boolean isStarred;
    public boolean isRead;
    public boolean hasAttachment;
    public String sendTo;

    // נתוני השולח
    public String firstName;
    public String lastName;
    public String mail;
    public String profileImage;
    public String username;

    public List<String> label = new ArrayList<>();
    public String category;

    // ✅ הוספת תמיכה במצורפים
    public List<Attachment> attachments = new ArrayList<>();

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
        mail.category = obj.optString("category", "");
        mail.sendTo = obj.optString("sendTo", "");

        if (obj.has("label")) {
            JSONArray labelsArray = obj.getJSONArray("label");
            for (int i = 0; i < labelsArray.length(); i++) {
                mail.label.add(labelsArray.getString(i));
            }
        }

        if (obj.has("sender")) {
            JSONObject sender = obj.getJSONObject("sender");
            mail.firstName = sender.optString("first_name", "");
            mail.lastName = sender.optString("last_name", "");
            mail.mail = sender.optString("mail", "");
            mail.profileImage = sender.optString("profileImage", "");
        }

        // ✅ קריאת מצורפים מהשרת
        if (obj.has("attachments")) {
            JSONArray attachmentsArray = obj.getJSONArray("attachments");
            for (int i = 0; i < attachmentsArray.length(); i++) {
                JSONObject attObj = attachmentsArray.getJSONObject(i);
                Attachment attachment = new Attachment();
                attachment.filename = attObj.optString("filename", "");
                attachment.path = attObj.optString("path", "");
                attachment.contentType = attObj.optString("contentType", "");
                attachment.size = attObj.optLong("size", 0);
                mail.attachments.add(attachment);
            }
        }

        return mail;
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("_id", id);
            obj.put("from", from);
            obj.put("fromMail", fromMail);
            obj.put("subject", subject);
            obj.put("preview", preview);
            obj.put("body", body);
            obj.put("date", date);
            obj.put("isStarred", isStarred);
            obj.put("isRead", isRead);
            obj.put("hasAttachment", hasAttachment);
            obj.put("category", category);
            obj.put("label", new JSONArray(label));
            obj.put("sendTo", sendTo);

            JSONObject sender = new JSONObject();
            sender.put("first_name", firstName);
            sender.put("last_name", lastName);
            sender.put("mail", mail);
            sender.put("profileImage", profileImage);
            obj.put("sender", sender);

            // ✅ הוספת מצורפים להמרה ל-JSON
            JSONArray attachmentsArray = new JSONArray();
            for (Attachment att : attachments) {
                JSONObject attObj = new JSONObject();
                attObj.put("filename", att.filename);
                attObj.put("path", att.path);
                attObj.put("contentType", att.contentType);
                attObj.put("size", att.size);
                attachmentsArray.put(attObj);
            }
            obj.put("attachments", attachmentsArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
