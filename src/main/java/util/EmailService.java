package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import model.data.DbConnection_AppSupport;

public class EmailService {

    private static final String SENDER_EMAIL = "studiomastercode@gmail.com";

    public static void sendEmailAsync(String to, String subject, String bodyHtml) {
        CompletableFuture.runAsync(() -> sendEmail(to, subject, bodyHtml));
    }

    public static void sendEmail(String to, String subject, String bodyHtml) {

        String senderPassword = getSecretConfig("SENDER_PASSWORD");

        if (senderPassword == null || senderPassword.isBlank()) {
            System.err.println("[EmailService] No se pudo obtener la contraseña del correo.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(
                    SENDER_EMAIL,
                    "ConneXtion Help Desk"
            ));

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );

            message.setSubject(subject);
            message.setContent(bodyHtml, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("[EmailService] Correo enviado correctamente a " + to);

        } catch (Exception e) {
            System.err.println("[EmailService] Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getSecretConfig(String key) {

        String sql = "SELECT ConfigValue FROM SystemConfig WHERE ConfigKey = ?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ConfigValue");
                }
            }

        } catch (Exception e) {
            System.err.println("[EmailService] Error al obtener configuración: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}