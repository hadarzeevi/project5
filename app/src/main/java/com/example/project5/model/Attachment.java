package com.example.project5.model;

/**
 * מחלקת Attachment לייצוג מצורפים במיילים,
 * תואמת לנתונים שהשרת מחזיר (filename, path, contentType, size)
 * לצורך הצגת שם, סוג וגודל קובץ במסך MailDetailActivity.
 */
public class Attachment {
    public String filename;
    public String path;
    public String contentType;
    public long size;

    // נדרש לפיענוח JSON אוטומטי (GSON / Moshi או org.json)
    public Attachment() {}

    public Attachment(String filename, String path, String contentType, long size) {
        this.filename = filename;
        this.path = path;
        this.contentType = contentType;
        this.size = size;
    }
}
