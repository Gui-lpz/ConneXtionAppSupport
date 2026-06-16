package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import model.data.IssueData;
import model.data.ServiceData;
import model.entities.Issue;


@WebServlet("/api/issues/incoming")
public class ServiceController extends HttpServlet {

    private final IssueData issueData = new IssueData();
    private final ServiceData serviceData = new ServiceData();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String[] ALLOWED_CLASSIFICATIONS = {"Baja", "Media", "Alta"};
    private static final String[] ALLOWED_STATUSES = {"Ingresado", "En Progreso", "Resuelto"};

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCors(resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        setCors(resp);

        Map<?, ?> body;
        try {
            body = mapper.readValue(req.getInputStream(), Map.class);
        } catch (Exception parseEx) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "JSON invalido.");
            return;
        }

        try {
            String reference = asString(body.get("reference"));
            Integer serviceId = toInt(body.get("serviceId"));
            String classification = asString(body.get("classification"));
            String status = asString(body.get("status"));
            String resolutionComment = asString(body.get("resolutionComment"));
            LocalDateTime issueTimestamp = parseTimestamp(body.get("issueTimestamp"));
            String issueDescription = asString(body.get("description"));
            if (issueDescription == null) {
                issueDescription = asString(body.get("issueDescription"));
            }

            if (reference == null || reference.isBlank()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "'refeence' es necesario.");
                return;
            }
            if (serviceId == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "'serviceId' es necesario como numero.");
                return;
            }
            // validar la foreing key de service
            if (serviceData.findById(serviceId) == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid serviceId: no matching service in the support database.");
                return;
            }

          
            // status=Ingresado, timestamp=now); 
            Issue issue = new Issue();
            issue.setReference(reference);
            issue.setServiceId(serviceId);
            if (inList(classification, ALLOWED_CLASSIFICATIONS)) {
                issue.setClassification(classification);
            }
            if (inList(status, ALLOWED_STATUSES)) {
                issue.setStatus(status);
            }
            if (issueTimestamp != null) {
                issue.setIssueTimestamp(issueTimestamp);
            }
            if (resolutionComment != null) {
                issue.setResolutionComment(resolutionComment);
            }
            issue.setIssueDescription(issueDescription);
            // supporterId / supervisorId queda en 0 (sin asignar)

            issueData.add(issue);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(resp.getWriter(),
                    Map.of("message", "Issue registrado en soporte.", "reference", reference));

        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la solicitud en el soporte: " + e.getMessage());
        }
    }

    // ── helpers ──────────────────────────────────────────────

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        mapper.writeValue(resp.getWriter(), Map.of("error", message));
    }

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    private Integer toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String && !((String) value).isBlank()) {
            try {
                return Integer.valueOf(((String) value).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private LocalDateTime parseTimestamp(Object value) {
        if (value instanceof String && !((String) value).isBlank()) {
            try {
                return LocalDateTime.parse(((String) value).trim());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private boolean inList(String value, String[] allowed) {
        if (value == null) {
            return false;
        }
        for (String item : allowed) {
            if (item.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
