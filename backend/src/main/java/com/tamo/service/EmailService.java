package com.tamo.service;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import javax.net.ssl.*;
import java.io.File;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of EmailService.
 * Provides necessary operations and logic for the system.
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.qq.com";
    private static final int SMTP_PORT = 465;
    private static final String SENDER_EMAIL = "2971475892@qq.com";
    private static final String SENDER_PASSWORD = "geogolgngypjdfcj";
    private static final String SENDER_NAME = "TA&MO Platform";

    private static EmailService instance;
    private final Session mailSession;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private String imagesBasePath;

    /**
     * Executes the setImagesBasePath operation.
     * @param path the path argument
     */
    /**
     * Executes the setImagesBasePath operation.
     * @param path the path argument
     */
    public void setImagesBasePath(String path) {
        this.imagesBasePath = path;
    }

    private EmailService() {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.ssl.checkserveridentity", "false");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");

        // Trust all certificates (handles proxy/VPN SSL interception)
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                /**
                 * Executes the checkClientTrusted operation.
                 * @param certs the certs argument
                 * @param authType the authType argument
                 */
                /**
                 * Executes the checkClientTrusted operation.
                 * @param certs the certs argument
                 * @param authType the authType argument
                 */
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                /**
                 * Executes the checkServerTrusted operation.
                 * @param certs the certs argument
                 * @param authType the authType argument
                 */
                /**
                 * Executes the checkServerTrusted operation.
                 * @param certs the certs argument
                 * @param authType the authType argument
                 */
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }}, new SecureRandom());
            props.put("mail.smtp.ssl.socketFactory", sslContext.getSocketFactory());
        } catch (Exception e) {
            System.err.println("[EmailService] Failed to create trust-all SSL context: " + e.getMessage());
        }

        mailSession = Session.getInstance(props, new Authenticator() {
            @Override
            /**
             * Executes the getPasswordAuthentication operation.
             * @return the resulting object or status
             */
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
    }

    /**
     * Executes the getInstance operation.
     * @return the resulting object or status
     */
    /**
     * Executes the getInstance operation.
     * @return the resulting object or status
     */
    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    /**
     * Send email asynchronously without blocking the main thread

     */
    /**
     * Executes the sendAsync operation.
     * @param toEmail the toEmail argument
     * @param subject the subject argument
     * @param htmlContent the htmlContent argument
     */
    /**
     * Executes the sendAsync operation.
     * @param toEmail the toEmail argument
     * @param subject the subject argument
     * @param htmlContent the htmlContent argument
     */
    public void sendAsync(String toEmail, String subject, String htmlContent) {
        executor.submit(() -> {
            try {
                send(toEmail, subject, htmlContent);
            } catch (Exception e) {
                System.err.println("[EmailService] Failed to send email to " + toEmail + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void send(String toEmail, String subject, String htmlContent) throws Exception {
        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME, "UTF-8"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject(subject, "UTF-8");

        MimeMultipart multipart = new MimeMultipart("related");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        addInlineImage(multipart, "email_bg", "image.png");

        message.setContent(multipart);
        Transport.send(message);
        System.out.println("[EmailService] Email sent to " + toEmail + " | Subject: " + subject);
    }

    private void addInlineImage(MimeMultipart multipart, String cid, String filename) throws Exception {
        if (imagesBasePath == null) return;
        File imgFile = new File(imagesBasePath, filename);
        if (!imgFile.exists()) {
            System.err.println("[EmailService] Image not found: " + imgFile.getAbsolutePath());
            return;
        }
        MimeBodyPart imagePart = new MimeBodyPart();
        FileDataSource fds = new FileDataSource(imgFile);
        imagePart.setDataHandler(new DataHandler(fds));
        imagePart.setHeader("Content-ID", "<" + cid + ">");
        imagePart.setDisposition(MimeBodyPart.INLINE);
        imagePart.setFileName(filename);
        multipart.addBodyPart(imagePart);
    }

    // ─── Status Change Notifications to TA ───

    /**
     * Executes the notifyStatusChange operation.
     * @param toEmail the toEmail argument
     * @param applicantName the applicantName argument
     * @param moduleCode the moduleCode argument
     * @param oldStatus the oldStatus argument
     * @param newStatus the newStatus argument
     */
    /**
     * Executes the notifyStatusChange operation.
     * @param toEmail the toEmail argument
     * @param applicantName the applicantName argument
     * @param moduleCode the moduleCode argument
     * @param oldStatus the oldStatus argument
     * @param newStatus the newStatus argument
     */
    public void notifyStatusChange(String toEmail, String applicantName, String moduleCode, String oldStatus, String newStatus) {
        String subject = "【TA&MO】Your application status has been updated";
        String statusLabel = getStatusLabel(newStatus);
        String statusColor = getStatusColor(newStatus);
        String statusMessage = getStatusMessage(moduleCode, newStatus);

        String html = buildHtml(
                "Application Status Update",
                "<p style='font-size:16px;color:#1e293b;margin:0 0 12px;font-weight:500;'>Dear <strong style='color:#0f172a;font-weight:700;'>" + escapeHtml(applicantName) + "</strong>,</p>" +
                "<p style='font-size:15px;color:#334155;line-height:1.6;margin:0 0 32px;font-weight:500;'>" + escapeHtml(statusMessage) + "</p>" +
                "<table style='width:100%;border-collapse:collapse;margin:0;'>" +
                "  <tr><td style='padding:16px 0;color:#475569;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);width:140px;font-weight:600;'>Status</td><td style='padding:16px 0;border-top:1px solid rgba(0,0,0,0.1);'>" +
                "    <span style='color:" + statusColor + ";font-size:14px;font-weight:700;letter-spacing:0.02em;'>" + escapeHtml(statusLabel) + "</span>" +
                "  </td></tr>" +
                "  <tr><td style='padding:16px 0;color:#475569;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);font-weight:600;'>Module</td><td style='padding:16px 0;font-weight:700;color:#0f172a;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);'>" + escapeHtml(moduleCode) + "</td></tr>" +
                "  <tr><td style='padding:16px 0;color:#475569;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);border-bottom:1px solid rgba(0,0,0,0.1);font-weight:600;'>Previous Status</td><td style='padding:16px 0;color:#334155;font-weight:600;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);border-bottom:1px solid rgba(0,0,0,0.1);'>" + escapeHtml(getStatusLabel(oldStatus)) + "</td></tr>" +
                "</table>"
        );
        sendAsync(toEmail, subject, html);
    }

    // ─── TA Application Notification to Teachers ───

    /**
     * Executes the notifyNewApplication operation.
     * @param toEmail the toEmail argument
     * @param organiserName the organiserName argument
     * @param applicantName the applicantName argument
     * @param moduleCode the moduleCode argument
     * @param jobTitle the jobTitle argument
     */
    /**
     * Executes the notifyNewApplication operation.
     * @param toEmail the toEmail argument
     * @param organiserName the organiserName argument
     * @param applicantName the applicantName argument
     * @param moduleCode the moduleCode argument
     * @param jobTitle the jobTitle argument
     */
    public void notifyNewApplication(String toEmail, String organiserName, String applicantName, String moduleCode, String jobTitle) {
        String subject = "【TA&MO】New TA Application Received - " + moduleCode;

        String html = buildHtml(
                "New TA Application",
                "<p style='font-size:16px;color:#1e293b;margin:0 0 12px;font-weight:500;'>Dear <strong style='color:#0f172a;font-weight:700;'>" + escapeHtml(organiserName) + "</strong>,</p>" +
                "<p style='font-size:15px;color:#334155;line-height:1.6;margin:0 0 32px;font-weight:500;'>A new TA application has been submitted for your position. Please review it at your earliest convenience.</p>" +
                "<table style='width:100%;border-collapse:collapse;margin:0;'>" +
                "  <tr><td style='padding:16px 0;color:#475569;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);width:140px;font-weight:600;'>Applicant</td><td style='padding:16px 0;font-weight:700;color:#0f172a;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);'>" + escapeHtml(applicantName) + "</td></tr>" +
                "  <tr><td style='padding:16px 0;color:#475569;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);font-weight:600;'>Module</td><td style='padding:16px 0;font-weight:700;color:#0f172a;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);'>" + escapeHtml(moduleCode) + "</td></tr>" +
                "  <tr><td style='padding:16px 0;color:#475569;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);border-bottom:1px solid rgba(0,0,0,0.1);font-weight:600;'>Position</td><td style='padding:16px 0;font-weight:700;color:#0f172a;font-size:14px;border-top:1px solid rgba(0,0,0,0.1);border-bottom:1px solid rgba(0,0,0,0.1);'>" + escapeHtml(jobTitle) + "</td></tr>" +
                "</table>" +
                "<p style='font-size:13px;color:#64748b;margin-top:32px;font-weight:500;'>Log in to the TA&amp;MO platform to review this application.</p>"
        );
        sendAsync(toEmail, subject, html);
    }

    private String buildHtml(String title, String bodyContent) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>" +
                "<body style='margin:0;padding:0;background:#f4f4f5;font-family:Inter,-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,\"Helvetica Neue\",Arial,sans-serif;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td align='center' style='padding:0;'>" +

                "<table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width:600px;width:100%;'>" +

                // background image area (use bgcolor to match the bottom part of the image)
                "<tr><td background='cid:email_bg' style='background-image:url(cid:email_bg);background-size:cover;background-position:top center;background-repeat:no-repeat;background-color:#e8edf3;' valign='top'>" +

                // top spacing for logo/banner area of background image
                "<div style='height:200px;'></div>" +

                // content directly on background
                "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td style='padding:0 48px;'>" +

                // title — minimalist high-end style
                "<h2 style='margin:0 0 24px;font-size:22px;color:#0f172a;font-weight:600;letter-spacing:-0.03em;border-bottom:1px solid rgba(0,0,0,0.1);padding-bottom:16px;text-align:center;'>" + title + "</h2>" +

                // body content
                bodyContent +

                "</td></tr></table>" +

                // bottom spacer to reveal the rest of the background image
                "<div style='height:150px;'></div>" +

                // footer — minimalist, inside background image area
                "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td style='padding:0 48px 32px;text-align:center;'>" +
                "  <p style='font-size:12px;color:#475569;margin:0 0 6px;font-weight:600;letter-spacing:0.05em;text-transform:uppercase;'>TA&amp;MO Platform</p>" +
                "  <p style='font-size:11px;color:#64748b;margin:0 0 8px;font-weight:400;'>Beijing University of Posts and Telecommunications &times; Queen Mary University of London</p>" +
                "  <p style='font-size:10px;color:#94a3b8;margin:0;font-weight:400;'>This is an automated notification. Please do not reply.</p>" +
                "</td></tr></table>" +

                "</td></tr>" +

                "</table>" +
                "</td></tr></table>" +
                "</body></html>";
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Unknown";
        return switch (status) {
            case "applied" -> "Applied";
            case "review" -> "Under Review";
            case "shortlisted" -> "Shortlisted";
            case "interview" -> "Interview Stage";
            case "offer" -> "Offer Received";
            case "rejected" -> "Not Selected";
            default -> status;
        };
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "applied" -> "#475569";
            case "review" -> "#64748b";
            case "shortlisted" -> "#334155";
            case "interview" -> "#1e293b";
            case "offer" -> "#0f172a";
            case "rejected" -> "#b91c1c";
            default -> "#64748b";
        };
    }

    private String getStatusIcon(String status) {
        return switch (status) {
            case "review" -> "🔍";
            case "shortlisted" -> "⭐";
            case "interview" -> "🎤";
            case "offer" -> "🎉";
            case "rejected" -> "📋";
            default -> "📬";
        };
    }

    private String getStatusMessage(String moduleCode, String status) {
        return switch (status) {
            case "review" -> "Your application for " + moduleCode + " is now being reviewed by the hiring team.";
            case "shortlisted" -> "Congratulations! Your application for " + moduleCode + " has been shortlisted. You are among the top candidates.";
            case "interview" -> "Great news! Your application for " + moduleCode + " has advanced to the interview stage. An interview will be scheduled soon.";
            case "offer" -> "Congratulations! You have received an offer for the TA position at " + moduleCode + "!";
            case "rejected" -> "Thank you for your interest in " + moduleCode + ". After careful consideration, we are unable to move forward with your application at this time.";
            default -> "Your application status for " + moduleCode + " has been updated to: " + status + ".";
        };
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
