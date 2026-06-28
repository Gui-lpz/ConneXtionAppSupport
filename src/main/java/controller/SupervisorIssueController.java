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
import model.data.SupporterData;
import model.entities.Issue;
import model.entities.Supporter;


@WebServlet("/api/supervisor/*")
public class SupervisorIssueController extends HttpServlet {

    private final IssueData issueData = new IssueData();
    private final SupporterData supporterData = new SupporterData();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String[] CLOSED_STATUSES = {"Resuelto", "Finished", "Terminado"};

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCors(resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepare(resp);
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();
        try {
            if ("/issues/available".equals(path)) {
                handleAvailable(req, resp);
            } else if ("/supporters/by-service".equals(path)) {
                handleSupportersByService(req, resp);
            } else {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown route.");
            }
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepare(resp);
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();
        try {
            if ("/issues/assign".equals(path)) {
                handleAssign(req, resp);
            } else {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown route.");
            }
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // GET /api/supervisor/issues/available[?serviceId=]
    private void handleAvailable(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer serviceId = parseIntParam(req.getParameter("serviceId"));
        ArrayList<Issue> issues = issueData.getSupervisorAvailableIssues(serviceId);

        List<Map<String, Object>> out = new ArrayList<>();
        for (Issue i : issues) {
            out.add(availableIssueToMap(i));
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(resp.getWriter(), out);
    }

    // GET /api/supervisor/supporters/by-service?serviceId=
    private void handleSupportersByService(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer serviceId = parseIntParam(req.getParameter("serviceId"));
        if (serviceId == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "serviceId is required.");
            return;
        }
        ArrayList<Supporter> supporters = supporterData.getByServiceId(serviceId);

        List<Map<String, Object>> out = new ArrayList<>();
        for (Supporter s : supporters) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("fullName", s.getFullName());
            m.put("name", s.getName());
            m.put("firstSurname", s.getFirstSurname());
            m.put("secondSurname", s.getSecondSurname());
            out.add(m);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(resp.getWriter(), out);
    }

    // POST /api/supervisor/issues/assign
    private void handleAssign(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Map<?, ?> body = mapper.readValue(req.getInputStream(), Map.class);
        Integer issueId = toInt(body.get("issueId"));
        Integer supporterId = toInt(body.get("supporterId"));
        Integer supervisorId = toInt(body.get("supervisorId")); // opcional
        if (supervisorId != null && supervisorId == 0) {
            supervisorId = null; // 0 se interpreta como "sin supervisor"
        }

        if (issueId == null || supporterId == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "issueId and supporterId are required.");
            return;
        }

        Issue issue = issueData.findById(issueId);
        if (issue == null) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Issue not found.");
            return;
        }
        if (isClosed(issue.getStatus())) {
            writeError(resp, HttpServletResponse.SC_CONFLICT, "Resolved issues cannot be assigned.");
            return;
        }
        // debe seguir disponible pues supporterId=null
        if (issue.getSupporterId() != 0) {
            writeError(resp, HttpServletResponse.SC_CONFLICT, "This issue is already assigned to a supporter.");
            return;
        }
        if (supporterData.findById(supporterId) == null) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Supporter not found.");
            return;
        }
        // para que el soportista solo pueda trabajar el servicio del tiquete
        if (!issueData.supporterHasService(supporterId, issue.getServiceId())) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN,
                    "The supporter does not work with this issue's service.");
            return;
        }

        issueData.assignIssueBySupervisor(issueId, supporterId, supervisorId);

        Issue updated = issueData.findById(issueId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Issue assigned successfully.");
        out.put("issue", assignedIssueToMap(updated));
        resp.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(resp.getWriter(), out);
    }

    // ── helpers ──────────────────────────────────────────────
    private Map<String, Object> availableIssueToMap(Issue i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("reference", i.getReference());
        m.put("issueDescription", i.getIssueDescription());
        m.put("serviceId", i.getServiceId());
        m.put("serviceName", i.getServiceName());
        m.put("issueTimestamp", i.getIssueTimestamp() != null ? i.getIssueTimestamp().toString() : null);
        return m;
    }

    private Map<String, Object> assignedIssueToMap(Issue i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("reference", i.getReference());
        m.put("issueDescription", i.getIssueDescription());
        m.put("serviceId", i.getServiceId());
        m.put("serviceName", i.getServiceName());
        m.put("supporterId", i.getSupporterId());
        m.put("supervisorId", i.getSupervisorId());
        m.put("status", i.getStatus());
        return m;
    }

    private void prepare(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        setCors(resp);
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        mapper.writeValue(resp.getWriter(), Map.of("error", message));
    }

    private boolean isClosed(String status) {
        if (status == null) {
            return false;
        }
        for (String s : CLOSED_STATUSES) {
            if (s.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    private Integer parseIntParam(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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
}
