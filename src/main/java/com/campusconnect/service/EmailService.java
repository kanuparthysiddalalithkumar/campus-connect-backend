package com.campusconnect.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ─── Welcome Email (on Registration) ───────────────────────────────────────
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("🎓 Welcome to CampusConnect!");
            helper.setText(buildWelcomeHtml(name), true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Could not send welcome email: " + e.getMessage());
        }
    }

    // ─── Login Notification Email ───────────────────────────────────────────────
    public void sendLoginNotificationEmail(String toEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("✅ New Login to Your CampusConnect Account");
            helper.setText(buildLoginHtml(name, toEmail), true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Could not send login email: " + e.getMessage());
        }
    }

    // ─── HTML: Login Notification ───────────────────────────────────────────────
    private String buildLoginHtml(String name, String email) {
        String time = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a z"));

        return "<!DOCTYPE html>" +
            "<html lang='en'>" +
            "<head><meta charset='UTF-8'/><meta name='viewport' content='width=device-width,initial-scale=1'/>" +
            "<style>" +
            "  * { margin:0; padding:0; box-sizing:border-box; }" +
            "  body { background:#0f0f1a; font-family:'Segoe UI',Arial,sans-serif; }" +
            "  .wrapper { max-width:600px; margin:40px auto; background:linear-gradient(145deg,#1a1a2e,#16213e); border-radius:20px; overflow:hidden; box-shadow:0 25px 60px rgba(0,0,0,0.5); }" +
            "  .header { background:linear-gradient(135deg,#6c63ff,#3b82f6); padding:48px 40px 36px; text-align:center; }" +
            "  .header .icon { font-size:56px; margin-bottom:16px; }" +
            "  .header h1 { color:#fff; font-size:28px; font-weight:700; letter-spacing:-.5px; }" +
            "  .header p { color:rgba(255,255,255,.8); font-size:15px; margin-top:6px; }" +
            "  .body { padding:40px; }" +
            "  .greeting { color:#e2e8f0; font-size:17px; margin-bottom:24px; line-height:1.6; }" +
            "  .greeting strong { color:#a78bfa; }" +
            "  .card { background:rgba(255,255,255,.05); border:1px solid rgba(255,255,255,.1); border-radius:14px; padding:24px; margin-bottom:28px; }" +
            "  .card-title { color:#94a3b8; font-size:12px; font-weight:600; text-transform:uppercase; letter-spacing:1.5px; margin-bottom:16px; }" +
            "  .detail-row { display:flex; align-items:center; gap:12px; margin-bottom:12px; }" +
            "  .detail-row:last-child { margin-bottom:0; }" +
            "  .detail-icon { width:36px; height:36px; border-radius:8px; display:flex; align-items:center; justify-content:center; font-size:16px; background:rgba(99,102,241,.2); text-align:center; line-height:36px; }" +
            "  .detail-label { color:#64748b; font-size:13px; }" +
            "  .detail-value { color:#e2e8f0; font-size:14px; font-weight:500; }" +
            "  .alert-box { background:linear-gradient(135deg,rgba(239,68,68,.15),rgba(220,38,38,.1)); border:1px solid rgba(239,68,68,.3); border-radius:12px; padding:18px 22px; margin-bottom:28px; }" +
            "  .alert-box p { color:#fca5a5; font-size:14px; line-height:1.6; }" +
            "  .alert-box strong { color:#f87171; }" +
            "  .btn { display:block; width:fit-content; margin:0 auto 32px; padding:14px 40px; background:linear-gradient(135deg,#6c63ff,#3b82f6); color:#fff; text-decoration:none; border-radius:50px; font-size:15px; font-weight:600; letter-spacing:.3px; box-shadow:0 8px 24px rgba(108,99,255,.4); }" +
            "  .footer { border-top:1px solid rgba(255,255,255,.07); padding:28px 40px; text-align:center; }" +
            "  .footer p { color:#475569; font-size:13px; line-height:1.7; }" +
            "  .footer .brand { color:#6c63ff; font-weight:700; font-size:15px; display:block; margin-bottom:4px; }" +
            "  .badge { display:inline-block; background:rgba(52,211,153,.15); border:1px solid rgba(52,211,153,.3); color:#6ee7b7; font-size:12px; font-weight:600; padding:4px 12px; border-radius:20px; margin-bottom:20px; }" +
            "</style></head>" +
            "<body>" +
            "<div class='wrapper'>" +
            "  <div class='header'>" +
            "    <div class='icon'>🔐</div>" +
            "    <h1>Successful Login</h1>" +
            "    <p>Your CampusConnect account was just accessed</p>" +
            "  </div>" +
            "  <div class='body'>" +
            "    <span class='badge'>✓ Login Verified</span>" +
            "    <p class='greeting'>Hey <strong>" + name + "</strong>,<br/>" +
            "    We detected a new login to your CampusConnect account. Here are the details:</p>" +
            "    <div class='card'>" +
            "      <div class='card-title'>Login Details</div>" +
            "      <div class='detail-row'>" +
            "        <div class='detail-icon'>👤</div>" +
            "        <div><div class='detail-label'>Account</div><div class='detail-value'>" + email + "</div></div>" +
            "      </div>" +
            "      <div class='detail-row'>" +
            "        <div class='detail-icon'>🕐</div>" +
            "        <div><div class='detail-label'>Time</div><div class='detail-value'>" + time + "</div></div>" +
            "      </div>" +
            "      <div class='detail-row'>" +
            "        <div class='detail-icon'>✅</div>" +
            "        <div><div class='detail-label'>Status</div><div class='detail-value'>Login Successful</div></div>" +
            "      </div>" +
            "    </div>" +
            "    <div class='alert-box'>" +
            "      <p><strong>⚠ Wasn't you?</strong><br/>" +
            "      If you did not perform this login, your account may be at risk. Please change your password immediately and contact support.</p>" +
            "    </div>" +
            "    <a href='#' class='btn'>Go to My Dashboard →</a>" +
            "  </div>" +
            "  <div class='footer'>" +
            "    <span class='brand'>CampusConnect</span>" +
            "    <p>Stay connected. Stay ahead.<br/>This is an automated security notification — please do not reply.</p>" +
            "  </div>" +
            "</div>" +
            "</body></html>";
    }

    // ─── HTML: Welcome Email ────────────────────────────────────────────────────
    private String buildWelcomeHtml(String name) {
        return "<!DOCTYPE html>" +
            "<html lang='en'>" +
            "<head><meta charset='UTF-8'/>" +
            "<style>" +
            "  * { margin:0; padding:0; box-sizing:border-box; }" +
            "  body { background:#0f0f1a; font-family:'Segoe UI',Arial,sans-serif; }" +
            "  .wrapper { max-width:600px; margin:40px auto; background:linear-gradient(145deg,#1a1a2e,#16213e); border-radius:20px; overflow:hidden; box-shadow:0 25px 60px rgba(0,0,0,0.5); }" +
            "  .header { background:linear-gradient(135deg,#f59e0b,#ef4444); padding:48px 40px 36px; text-align:center; }" +
            "  .header .icon { font-size:56px; margin-bottom:16px; }" +
            "  .header h1 { color:#fff; font-size:28px; font-weight:700; }" +
            "  .header p { color:rgba(255,255,255,.85); font-size:15px; margin-top:6px; }" +
            "  .body { padding:40px; }" +
            "  .greeting { color:#e2e8f0; font-size:17px; line-height:1.7; margin-bottom:28px; }" +
            "  .greeting strong { color:#fbbf24; }" +
            "  .features { display:block; margin-bottom:28px; }" +
            "  .feature { background:rgba(255,255,255,.05); border:1px solid rgba(255,255,255,.09); border-radius:12px; padding:16px 20px; margin-bottom:12px; display:flex; gap:14px; align-items:center; }" +
            "  .feature .fi { font-size:24px; }" +
            "  .feature .ft { color:#cbd5e1; font-size:14px; line-height:1.5; }" +
            "  .feature .ft strong { color:#f8fafc; display:block; margin-bottom:2px; font-size:15px; }" +
            "  .btn { display:block; width:fit-content; margin:0 auto 32px; padding:14px 40px; background:linear-gradient(135deg,#f59e0b,#ef4444); color:#fff; text-decoration:none; border-radius:50px; font-size:15px; font-weight:600; box-shadow:0 8px 24px rgba(245,158,11,.4); }" +
            "  .footer { border-top:1px solid rgba(255,255,255,.07); padding:28px 40px; text-align:center; }" +
            "  .footer p { color:#475569; font-size:13px; line-height:1.7; }" +
            "  .footer .brand { color:#f59e0b; font-weight:700; font-size:15px; display:block; margin-bottom:4px; }" +
            "</style></head>" +
            "<body>" +
            "<div class='wrapper'>" +
            "  <div class='header'>" +
            "    <div class='icon'>🎓</div>" +
            "    <h1>Welcome to CampusConnect!</h1>" +
            "    <p>Your campus life just got smarter</p>" +
            "  </div>" +
            "  <div class='body'>" +
            "    <p class='greeting'>Hello <strong>" + name + "</strong>,<br/><br/>" +
            "    We're thrilled to have you on board! Your account has been successfully created. Here's what you can do on CampusConnect:</p>" +
            "    <div class='features'>" +
            "      <div class='feature'><div class='fi'>📅</div><div class='ft'><strong>Discover Events</strong>Browse and register for campus events, workshops, and activities.</div></div>" +
            "      <div class='feature'><div class='fi'>🤝</div><div class='ft'><strong>Connect with Peers</strong>Network with students across departments and years.</div></div>" +
            "      <div class='feature'><div class='fi'>📢</div><div class='ft'><strong>Stay Updated</strong>Get real-time announcements and notifications from your college.</div></div>" +
            "    </div>" +
            "    <a href='#' class='btn'>Explore CampusConnect →</a>" +
            "  </div>" +
            "  <div class='footer'>" +
            "    <span class='brand'>CampusConnect</span>" +
            "    <p>Stay connected. Stay ahead.<br/>This is an automated message — please do not reply.</p>" +
            "  </div>" +
            "</div>" +
            "</body></html>";
    }
}
