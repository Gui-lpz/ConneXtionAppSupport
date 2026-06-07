package util;

import java.time.format.DateTimeFormatter;
import model.entities.Issue;
import model.entities.Supporter;

public class EmailTemplates {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String issueAssignedTemplate(Issue issue, Supporter supporter) {
        return buildTemplate(
                "Solicitud asignada",
                "Su solicitud fue asignada a un soportista.",
                "ASIGNADO",
                "#2980b9",
                issue,
                supporter,
                null
        );
    }

    public static String issueInProgressTemplate(Issue issue, Supporter supporter) {
        return buildTemplate(
                "Solicitud en proceso",
                "Su solicitud ya se encuentra en proceso de atención.",
                "EN PROCESO",
                "#f39c12",
                issue,
                supporter,
                null
        );
    }

    public static String issueResolvedTemplate(Issue issue, Supporter supporter) {
        return buildTemplate(
                "Solicitud resuelta",
                "Su solicitud fue resuelta correctamente.",
                "RESUELTO",
                "#27ae60",
                issue,
                supporter,
                issue.getResolutionComment()
        );
    }

    private static String buildTemplate(
            String title,
            String message,
            String status,
            String statusColor,
            Issue issue,
            Supporter supporter,
            String resolutionComment
    ) {

        String issueReference = issue.getReference() != null
                ? issue.getReference()
                : "Sin referencia";

        String issueDate = issue.getIssueTimestamp() != null
                ? issue.getIssueTimestamp().format(DATE_TIME_FMT)
                : "No registrado";

        String issueClassification = issue.getClassification() != null
                ? issue.getClassification()
                : "No clasificada";

        String supporterName = supporter != null
                ? supporter.getName() + " " + supporter.getFirstSurname() + " " + supporter.getSecondSurname()
                : "Pendiente de asignación";

        String supporterEmail = supporter != null && supporter.getEmail() != null
                ? supporter.getEmail()
                : "Pendiente";

        String resolution = resolutionComment != null && !resolutionComment.isBlank()
                ? resolutionComment
                : "Sin comentario final.";

        return ""
                + "<div style='background:#071B2C;padding:30px 0;font-family:Arial,sans-serif;'>"

                + "<div style='width:420px;margin:auto;background:#ffffff;border-radius:18px;"
                + "padding:25px;box-shadow:0 2px 12px rgba(0,0,0,0.25);text-align:center;'>"

                + "<div style='font-size:45px;'>🎧</div>"

                + "<h2 style='margin:5px 0;color:#0A2E42;'>ConneXtion Help Desk</h2>"

                + "<p style='color:#777;margin-top:4px;'>"
                + "Seguimiento de solicitudes de soporte"
                + "</p>"

                + "<div style='border-top:2px dashed #999;margin:15px 0;'></div>"

                + "<h3 style='color:#444;margin-bottom:10px;'>"
                + title
                + "</h3>"

                + "<p style='text-align:left;font-size:14px;line-height:1.7;color:#333;'>"
                + message
                + "</p>"

                + "<div style='text-align:left;font-size:14px;line-height:1.8;color:#222;'>"

                + "<p><strong>Número de solicitud:</strong><br>"
                + issueReference
                + "</p>"

                + "<p><strong>Clasificación:</strong><br>"
                + issueClassification
                + "</p>"

                + "<p><strong>Fecha de registro:</strong><br>"
                + issueDate
                + "</p>"

                + "<p><strong>Soportista asignado:</strong><br>"
                + supporterName
                + "</p>"

                + "<p><strong>Correo del soportista:</strong><br>"
                + supporterEmail
                + "</p>"

                + "<p><strong>Comentario de resolución:</strong><br>"
                + resolution
                + "</p>"

                + "<p><strong>Estado:</strong> "
                + "<span style='color:" + statusColor + ";font-weight:bold;'>"
                + status
                + "</span></p>"

                + "</div>"

                + "<div style='border-top:2px dashed #999;margin:20px 0;'></div>"

                + "<p style='font-size:12px;color:#999;'>"
                + "Gracias por utilizar ConneXtion Help Desk."
                + "</p>"

                + "</div>"
                + "</div>";
    }
}