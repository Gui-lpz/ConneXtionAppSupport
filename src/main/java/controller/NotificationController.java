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
//  Funciona como Endpoint de notificaciones para la aplicación de soporte.

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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        setCors(resp);
        String role = req.getParameter("role");
        if (role == null || role.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(),
                    Map.of("error", "El parámetro 'role' es obligatorio (SUPERVISOR o SUPPORTER)."));
            return;
        }
        try {
            switch (role.toUpperCase()) {
                case "SUPERVISOR" ->
                    handleSupervisorNotifications(req, resp);
                case "SUPPORTER" ->
                    handleSupporterNotifications(req, resp);
                default -> {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    mapper.writeValue(resp.getWriter(),
                            Map.of("error", "Rol no reconocido: " + role));
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of("error", e.getMessage()));
        }
    }

    private void handleSupervisorNotifications(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String supervisorIdParam = request.getParameter("supervisorId");
        if (supervisorIdParam == null || supervisorIdParam.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "El parámetro 'supervisorId' es obligatorio."));
            return;
        }
        int supervisorId = Integer.parseInt(supervisorIdParam);
        Supervisor supervisor = supervisorData.findById(supervisorId);
        if (supervisor == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Supervisor no encontrado con id: " + supervisorId));
            return;
        }
        ArrayList<Issue> pendingIssues = issueData.getPendingByServiceId(supervisor.getServiceId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Issue issue : pendingIssues) {
            result.add(buildSupervisorNotificationDto(issue));
        }
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(response.getWriter(), result);
    }

    private void handleSupporterNotifications(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String supporterIdParam = request.getParameter("supporterId");
        if (supporterIdParam == null || supporterIdParam.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "El parámetro 'supporterId' es obligatorio."));
            return;
        }
        int supporterId = Integer.parseInt(supporterIdParam);
        Supporter supporter = supporterData.findById(supporterId);
        if (supporter == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Soportista no encontrado con id: " + supporterId));
            return;
        }
        ArrayList<Issue> assignedIssues = issueData.getBySupporterId(supporterId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Issue issue : assignedIssues) {
            result.add(buildSupporterIssueDto(issue));
        }
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(response.getWriter(), result);
    }

    private Map<String, Object> buildSupervisorNotificationDto(Issue issue) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", issue.getId());
        dto.put("reference", issue.getReference());
        dto.put("classification", issue.getClassification());
        dto.put("serviceName", issue.getServiceName());
        dto.put("status", issue.getStatus());
        dto.put("issueTimestamp",
                issue.getIssueTimestamp() != null
                ? issue.getIssueTimestamp().toString() : null);
        dto.put("contactEmail", issue.getContactEmail());
        dto.put("contactPhone", issue.getContactPhone());
        return dto;
    }

    private Map<String, Object> buildSupporterIssueDto(Issue issue) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", issue.getId());
        dto.put("reference", issue.getReference());
        dto.put("classification", issue.getClassification());
        dto.put("serviceName", issue.getServiceName());
        dto.put("status", issue.getStatus());
        dto.put("issueTimestamp",
                issue.getIssueTimestamp() != null
                ? issue.getIssueTimestamp().toString() : null);
        dto.put("resolutionComment",
                issue.isResolved() ? issue.getResolutionComment() : null);
        dto.put("isAssigned", issue.isAssigned());
        dto.put("isInProgress", issue.isInProgress());
        dto.put("isResolved", issue.isResolved());
        return dto;
    }
}
