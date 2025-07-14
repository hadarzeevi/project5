package com.example.project5.model;

/**
 * מחלקת Attachment לייצוג מצורפים במיילים,
 * תואמת לנתונים שהשרת מחזיר (filename, path, contentType, size)
 * לצורך הצגת שם, סוג וגודל קובץ במסך MailDetailActivity.
 */
public class Attachment {
    // מחלקת Attachment עם בדיקות null safety

        public String filename;
        public String contentType;
        public long size;
        public String path;
        public String data; //  base64

        public Attachment() {
            // empty constructor
        }

        public boolean isValid() {
            return filename != null && !filename.isEmpty();
        }

        public String getFormattedSize() {
            if (size <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
        }

        public boolean hasData() {
            return data != null && !data.isEmpty();
        }

        public boolean hasPath() {
            return path != null && !path.isEmpty();
        }

        @Override
        public String toString() {
            return "Attachment{" +
                    "filename='" + filename + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", size=" + size +
                    ", path='" + path + '\'' +
                    ", hasData=" + hasData() +
                    '}';
        }
    }
