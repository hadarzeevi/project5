package com.example.project5.model;

import android.util.Log;

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

    public List<Attachment> attachments = new ArrayList<>();

    public static Mail fromJson(JSONObject obj) throws JSONException {
        Mail mail = new Mail();

        mail.id = obj.optString("_id", obj.optString("id", ""));
        mail.from = obj.optString("from", "");
        mail.fromMail = obj.optString("fromMail", "");
        mail.subject = obj.optString("subject", "");
        mail.preview = obj.optString("preview", "");
        mail.body = obj.optString("body", "");
        mail.date = obj.optString("date", "");
        mail.isStarred = obj.optBoolean("isStarred", false);
        mail.isRead = obj.optBoolean("isRead", false);
        mail.hasAttachment = obj.optBoolean("hasAttachment", false);
        mail.category = obj.optString("category", "inbox");
        mail.sendTo = obj.optString("sendTo", "");

        // תוויות
        if (obj.has("label") && !obj.isNull("label")) {
            JSONArray labelsArray = obj.getJSONArray("label");
            for (int i = 0; i < labelsArray.length(); i++) {
                String label = labelsArray.optString(i, "");
                if (!label.isEmpty()) {
                    mail.label.add(label);
                }
            }
        }

        // נתוני השולח כאובייקט (אם קיימים)
        if (obj.has("from") && obj.opt("from") instanceof JSONObject) {
            JSONObject sender = obj.getJSONObject("from");
            mail.firstName = sender.optString("first_name", "");
            mail.lastName = sender.optString("last_name", "");
            mail.mail = sender.optString("mail", "");
            mail.profileImage = sender.optString("profileImage", "");
        }

        // מצורפים
        if (obj.has("attachments") && !obj.isNull("attachments")) {
            JSONArray attachmentsArray = obj.getJSONArray("attachments");
            mail.attachments = new ArrayList<>();

            for (int i = 0; i < attachmentsArray.length(); i++) {
                try {
                    JSONObject attObj = attachmentsArray.getJSONObject(i);
                    Attachment attachment = new Attachment();

                    attachment.filename = attObj.optString("filename", "");
                    attachment.contentType = attObj.optString("contentType", "");
                    attachment.size = attObj.optLong("size", 0);
                    attachment.path = attObj.optString("path", "");

                    if (attObj.has("data") && !attObj.isNull("data")) {
                        attachment.data = attObj.getString("data");
                    }

                    if (!attachment.filename.isEmpty()) {
                        mail.attachments.add(attachment);
                    }
                } catch (JSONException e) {
                    Log.e("Mail", "Error parsing attachment " + i + ": " + e.getMessage());
                }
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

            // מצורפים
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

    // פונקציית דיבוג
    public void debugPrint() {
        Log.d("Mail", "=== Mail Debug Info ===");
        Log.d("Mail", "ID: " + id);
        Log.d("Mail", "Subject: " + subject);
        Log.d("Mail", "From: " + fromMail);
        Log.d("Mail", "Category: " + category);
        Log.d("Mail", "Attachments count: " + (attachments != null ? attachments.size() : 0));
        if (attachments != null) {
            for (int i = 0; i < attachments.size(); i++) {
                Attachment att = attachments.get(i);
                Log.d("Mail", "Attachment " + i + ": " + att.filename + " (" + att.size + " bytes)");
            }
        }
        Log.d("Mail", "=====================");
    }
}
