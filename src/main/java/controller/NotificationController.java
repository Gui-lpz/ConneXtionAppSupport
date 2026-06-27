package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.data.IssueData;
import model.data.SupervisorData;
import model.data.SupporterData;
import model.entities.Issue;
import model.entities.Supervisor;
import model.entities.Supporter;

@WebServlet("/api/notifications")
public class NotificationController extends HttpServlet {

    private final IssueData issueData = new IssueData();
    private final SupporterData supporterData = new SupporterData();
    private final SupervisorData supervisorData = new SupervisorData();
    private final ObjectMapper mapper = new ObjectMapper();

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCors(resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        setCors(resp);

        String role = req.getParameter("role");

        if (role == null || role.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    Map.of("error", "El parámetro role es obligatorio."));
            return;
        }

        try {
            if ("SUPERVISOR".equalsIgnoreCase(role)) {
                handleSupervisorNotifications(req, resp);
                return;
            }

            if ("SUPPORTER".equalsIgnoreCase(role)) {
                handleSupporterNotifications(req, resp);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    Map.of("error", "Rol no reconocido: " + role));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(),
                    Map.of("error", e.getMessage()));
        }
    }

    private void handleSupervisorNotifications(HttpServletRequest req,
            HttpServletResponse resp) throws Exception {

        String idParam = req.getParameter("supervisorId");

        if (idParam == null || idParam.isBlank()) {
            idParam = req.getParameter("id");
        }

        if (idParam == null || idParam.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    Map.of("error", "El parámetro supervisorId es obligatorio."));
            return;
        }

        int supervisorId = Integer.parseInt(idParam);
        Supervisor supervisor = supervisorData.findById(supervisorId);

        if (supervisor == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(resp.getWriter(),
                    Map.of("error", "Supervisor no encontrado."));
            return;
        }

        /*
         * Supervisor ve todos los tiquetes nuevos ingresados por los clientes
        
         */
        ArrayList<Issue> pendingIssues = issueData.getNewUnassignedIssues();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Issue issue : pendingIssues) {
            result.add(buildSupervisorNotificationDto(issue));
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(resp.getWriter(), result);
    }

    private void handleSupporterNotifications(HttpServletRequest req,
            HttpServletResponse resp) throws Exception {

        String idParam = req.getParameter("supporterId");

        if (idParam == null || idParam.isBlank()) {
            idParam = req.getParameter("id");
        }

        if (idParam == null || idParam.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    Map.of("error", "El parámetro supporterId es obligatorio."));
            return;
        }

        int supporterId = Integer.parseInt(idParam);
        Supporter supporter = supporterData.findById(supporterId);

        if (supporter == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(resp.getWriter(),
                    Map.of("error", "Soportista no encontrado."));
            return;
        }

        /*
         * Soportista ve únicamente los tiquetes que el supervisor ya le asignó
         */
        ArrayList<Issue> assignedIssues =
                issueData.getBySupporterId(supporterId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Issue issue : assignedIssues) {
            if (shouldNotifySupporter(issue, supporterId)) {
                result.add(buildSupporterNotificationDto(issue));
            }
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(resp.getWriter(), result);
    }

    private boolean shouldNotifySupporter(Issue issue, int supporterId) {
        if (issue == null) {
            return false;
        }

        if (issue.getSupporterId() != supporterId) {
            return false;
        }

        String status = normalize(issue.getStatus());

        return status.equals("asignado");
    }

    private Map<String, Object> buildSupervisorNotificationDto(Issue issue) {
        Map<String, Object> dto = baseDto(issue);

        dto.put("type", "NEW_ISSUE");
        dto.put("title", "Nuevo tiquete ingresado");
        dto.put("message", "Hay un nuevo tiquete pendiente de asignar.");

        return dto;
    }

    private Map<String, Object> buildSupporterNotificationDto(Issue issue) {
        Map<String, Object> dto = baseDto(issue);

        dto.put("type", "ASSIGNED_ISSUE");
        dto.put("title", "Nuevo tiquete asignado");
        dto.put("message", "Se te asignó un nuevo tiquete.");

        return dto;
    }

    private Map<String, Object> baseDto(Issue issue) {
        Map<String, Object> dto = new LinkedHashMap<>();

        dto.put("id", issue.getId());
        dto.put("reference", issue.getReference());
        dto.put("classification", issue.getClassification());
        dto.put("serviceName", issue.getServiceName());
        dto.put("status", issue.getStatus());
        dto.put("issueTimestamp",
                issue.getIssueTimestamp() != null
                        ? issue.getIssueTimestamp().toString()
                        : null);
        dto.put("contactEmail", issue.getContactEmail());
        dto.put("contactPhone", issue.getContactPhone());
        dto.put("supporterId", issue.getSupporterId());
        dto.put("supervisorId", issue.getSupervisorId());

        return dto;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }
}