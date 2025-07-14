package com.example.project5.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpamUtils {
    private static final String TAG = "SpamUtils";
    private static final String PREFS_NAME = "SpamPrefs";
    private static final String BLACKLIST_KEY = "blacklist";
    private static final String LINK_BLACKLIST_KEY = "link_blacklist";
    private static final String API_BASE_URL = "http://10.0.2.2:4000/api";

    // Pattern לזיהוי קישורים
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?i)\\b(?:https?://|www\\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    );

    /**
     * הוספת אימייל לרשימה השחורה
     */
    public static void addToBlacklist(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> blacklist = prefs.getStringSet(BLACKLIST_KEY, new HashSet<>());
        blacklist.add(email.toLowerCase());
        prefs.edit().putStringSet(BLACKLIST_KEY, blacklist).apply();
        Log.d(TAG, "Added email to blacklist: " + email);
    }

    /**
     * הסרת אימייל מהרשימה השחורה
     */
    public static void removeFromBlacklist(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> blacklist = prefs.getStringSet(BLACKLIST_KEY, new HashSet<>());
        blacklist.remove(email.toLowerCase());
        prefs.edit().putStringSet(BLACKLIST_KEY, blacklist).apply();
        Log.d(TAG, "Removed email from blacklist: " + email);
    }

    /**
     * בדיקה אם אימייל נמצא ברשימה השחורה
     */
    public static boolean isEmailBlacklisted(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> blacklist = prefs.getStringSet(BLACKLIST_KEY, new HashSet<>());
        return blacklist.contains(email.toLowerCase());
    }

    /**
     * חילוץ קישורים מטקסט
     */
    public static List<String> extractLinksFromText(String text) {
        List<String> links = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return links;
        }

        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            String link = matcher.group();
            // נרמול הקישור
            if (!link.startsWith("http")) {
                link = "http://" + link;
            }
            links.add(link.toLowerCase());
        }
        return links;
    }

    /**
     * הוספת קישורים לרשימה השחורה
     */
    public static void addLinksToBlacklist(Context context, List<String> links) {
        if (links == null || links.isEmpty()) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> linkBlacklist = prefs.getStringSet(LINK_BLACKLIST_KEY, new HashSet<>());

        for (String link : links) {
            linkBlacklist.add(link.toLowerCase());
        }

        prefs.edit().putStringSet(LINK_BLACKLIST_KEY, linkBlacklist).apply();
        Log.d(TAG, "Added " + links.size() + " links to blacklist");
    }

    /**
     * הסרת קישורים מהרשימה השחורה
     */
    public static void removeLinksFromBlacklist(Context context, List<String> links) {
        if (links == null || links.isEmpty()) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> linkBlacklist = prefs.getStringSet(LINK_BLACKLIST_KEY, new HashSet<>());

        for (String link : links) {
            linkBlacklist.remove(link.toLowerCase());
        }

        prefs.edit().putStringSet(LINK_BLACKLIST_KEY, linkBlacklist).apply();
        Log.d(TAG, "Removed " + links.size() + " links from blacklist");
    }

    /**
     * בדיקה אם קישור נמצא ברשימה השחורה
     */
    public static boolean isLinkBlacklisted(Context context, String link) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> linkBlacklist = prefs.getStringSet(LINK_BLACKLIST_KEY, new HashSet<>());
        return linkBlacklist.contains(link.toLowerCase());
    }

    /**
     * בדיקה אם מייל מכיל קישורים מהרשימה השחורה
     */
    public static boolean containsBlacklistedLinks(Context context, String subject, String body) {
        String fullText = (subject != null ? subject : "") + " " + (body != null ? body : "");
        List<String> links = extractLinksFromText(fullText);

        for (String link : links) {
            if (isLinkBlacklisted(context, link)) {
                Log.d(TAG, "Found blacklisted link: " + link);
                return true;
            }
        }
        return false;
    }

    /**
     * בדיקה מקיפה אם מייל הוא ספאם
     */
    public static boolean isSpamMail(Context context, String senderEmail, String subject, String body) {
        // בדיקת אימייל ברשימה השחורה
        if (isEmailBlacklisted(context, senderEmail)) {
            Log.d(TAG, "Mail is spam - sender in blacklist: " + senderEmail);
            return true;
        }

        // בדיקת קישורים ברשימה השחורה
        if (containsBlacklistedLinks(context, subject, body)) {
            Log.d(TAG, "Mail is spam - contains blacklisted links");
            return true;
        }

        return false;
    }

    /**
     * סימון מייל כספאם והוספת קישורים לרשימה השחורה
     */
    public static void markMailAsSpam(Context context, String mailId, String senderEmail, String subject, String body) {
        // הוספת השולח לרשימה השחורה
        if (senderEmail != null && !senderEmail.isEmpty()) {
            addToBlacklist(context, senderEmail);
        }

        // חילוץ והוספת קישורים לרשימה השחורה
        String fullText = (subject != null ? subject : "") + " " + (body != null ? body : "");
        List<String> links = extractLinksFromText(fullText);
        if (!links.isEmpty()) {
            addLinksToBlacklist(context, links);
        }

        // עדכון קטגוריית המייל לספאם בשרת
        updateMailCategoryOnServer(context, mailId, "spam");
    }

    /**
     * הסרת מייל מספאם והסרת קישורים מהרשימה השחורה
     */
    public static void removeMailFromSpam(Context context, String mailId, String senderEmail, String subject, String body, String newCategory) {
        // הסרת השולח מהרשימה השחורה (אופציונלי)
        if (senderEmail != null && !senderEmail.isEmpty()) {
            removeFromBlacklist(context, senderEmail);
        }

        // חילוץ והסרת קישורים מהרשימה השחורה
        String fullText = (subject != null ? subject : "") + " " + (body != null ? body : "");
        List<String> links = extractLinksFromText(fullText);
        if (!links.isEmpty()) {
            removeLinksFromBlacklist(context, links);
        }

        // עדכון קטגוריית המייל בשרת
        updateMailCategoryOnServer(context, mailId, newCategory);
    }

    /**
     * עדכון קטגוריית מייל בשרת
     */
    private static void updateMailCategoryOnServer(Context context, String mailId, String category) {
        new Thread(() -> {
            try {
                Log.d("SpamUtils", "מנסה לעדכן קטגוריה ל: " + category + " עבור מייל " + mailId);

                URL url = new URL(API_BASE_URL + "/mails/" + mailId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");

                SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                } else {
                    Log.e("SpamUtils", "אין טוקן! אי אפשר לשלוח את הבקשה.");
                    return;
                }

                conn.setDoOutput(true);
                JSONObject payload = new JSONObject();
                payload.put("category", category);
                payload.put("previousCategory", category); // אם רלוונטי לך

                Log.d("SpamUtils", "שולחת לשרת את ה־payload: " + payload.toString());

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("SpamUtils", "קיבלתי קוד תגובה: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorMsg = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorMsg.append(line);
                    }
                    reader.close();
                    Log.e("SpamUtils", "שגיאה מהשרת: " + errorMsg);
                }

            } catch (Exception e) {
                Log.e("SpamUtils", "שגיאה בעדכון קטגוריה לשרת: " + e.getMessage(), e);
            }
        }).start();
    }



    /**
     * בדיקת ספאם מול השרת (אופציונלי)
     */
    public static void checkSpamWithServer(Context context, String subject, String body, SpamCheckCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/blacklist/check");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                conn.setDoOutput(true);
                JSONObject bodyJson = new JSONObject();
                bodyJson.put("subject", subject);
                bodyJson.put("body", body);

                conn.getOutputStream().write(bodyJson.toString().getBytes("UTF-8"));

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();

                JSONObject response = new JSONObject(json.toString());
                boolean isSpam = response.optBoolean("spam", false);

                if (callback != null) {
                    callback.onResult(isSpam);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error checking spam with server: " + e.getMessage());
                if (callback != null) {
                    callback.onResult(false);
                }
            }
        }).start();
    }

    /**
     * קבלת כל הקישורים ברשימה השחורה
     */
    public static Set<String> getBlacklistedLinks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(LINK_BLACKLIST_KEY, new HashSet<>());
    }

    /**
     * קבלת כל האימיילים ברשימה השחורה
     */
    public static Set<String> getBlacklistedEmails(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(BLACKLIST_KEY, new HashSet<>());
    }

    /**
     * ניקוי רשימות שחורות
     */
    public static void clearBlacklists(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(BLACKLIST_KEY)
                .remove(LINK_BLACKLIST_KEY)
                .apply();
        Log.d(TAG, "Cleared all blacklists");
    }

    /**
     * Interface לקבלת תוצאות בדיקת ספאם
     */
    public interface SpamCheckCallback {
        void onResult(boolean isSpam);
    }
}