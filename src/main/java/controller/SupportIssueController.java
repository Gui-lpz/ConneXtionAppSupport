package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.data.IssueData;
import model.entities.Issue;
import sync.IssueSyncManager;
import model.data.ClientStatusClient;

@WebServlet("/api/support/issues/*")
public class SupportIssueController extends HttpServlet {

    private final IssueData issueData = new IssueData();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String[] CLOSED_STATUSES = {"Resuelto", "Finished", "Terminado"};
    private static final String[] ALLOWED_CLASSIFICATIONS = {"Baja", "Media", "Alta"};
    private static final String[] ALLOWED_STATUSES = {"Ingresado", "En Progreso", "Resuelto"};
    private final ClientStatusClient clientStatusClient = new ClientStatusClient();

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
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
        PrintWriter out = resp.getWriter();

        String[] parts = pathParts(req);
        String route = parts.length >= 1 ? parts[0] : "";

        try {
            Integer supporterId = parseIntParam(req.getParameter("supporterId"));
            if (supporterId == null) {
                writeError(resp, out, HttpServletResponse.SC_BAD_REQUEST, "supporterId is required.");
                return;
            }

            if ("available".equals(route)) {
                ArrayList<Issue> list = issueData.getAvailableIssuesBySupporterId(supporterId);
                out.print(listToJson(list));
            } else if ("mine".equals(route)) {
                ArrayList<Issue> list = issueData.getIssuesBySupporterId(supporterId);
                out.print(listToJson(list));
            } else {
                writeError(resp, out, HttpServletResponse.SC_NOT_FOUND, "Unknown route.");
                return;
            }
            out.flush();

        } catch (Exception e) {
            writeError(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepare(resp);
        PrintWriter out = resp.getWriter();

        String[] parts = pathParts(req);
        if (parts.length < 2) {
            writeError(resp, out, HttpServletResponse.SC_NOT_FOUND, "Unknown route.");
            return;
        }

        Integer issueId = parseIntParam(parts[0]);
        String action = parts[1];

        if (issueId == null) {
            writeError(resp, out, HttpServletResponse.SC_BAD_REQUEST, "issueId is required.");
            return;
        }

        if ("assign".equals(action)) {
            handleAssign(req, resp, out, issueId);
        } else if ("update".equals(action)) {
            handleUpdate(req, resp, out, issueId);
        } else {
            writeError(resp, out, HttpServletResponse.SC_NOT_FOUND, "Unknown route.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }

    private void handleAssign(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, int issueId) {
        try {
            Map<?, ?> body = mapper.readValue(req.getInputStream(), Map.class);
            Integer supporterId = toInt(body.get("supporterId"));

            if (supporterId == null) {
                writeError(resp, out, HttpServletResponse.SC_BAD_REQUEST, "supporterId is required.");
                return;
            }

            Issue issue = issueData.findById(issueId);
            if (issue == null) {
                writeError(resp, out, HttpServletResponse.SC_NOT_FOUND, "Issue not found.");
                return;
            }

            if (isClosed(issue.getStatus())) {
                writeError(resp, out, HttpServletResponse.SC_CONFLICT, "Resolved issues cannot be assigned.");
                return;
            }

            if (issue.getSupporterId() != 0 && issue.getSupporterId() != supporterId) {
                writeError(resp, out, HttpServletResponse.SC_CONFLICT,
                        "This issue is already assigned to another supporter.");
                return;
            }

            if (!issueData.supporterHasService(supporterId, issue.getServiceId())) {
                writeError(resp, out, HttpServletResponse.SC_FORBIDDEN,
                        "This issue does not belong to the supporter's services.");
                return;
            }

            issueData.assignIssueToSupporter(issueId, supporterId);

            Issue updated = issueData.findById(issueId);

            try {
                if (updated != null) {
                    clientStatusClient.updateClientIssueStatus(
                            updated.getReference(),
                            updated.getStatus(),
                            updated.getResolutionComment()
                    );
                }
            } catch (Exception syncEx) {
                System.err.println("No se pudo sincronizar asignación con cliente: "
                        + syncEx.getMessage());
            }

            writeSuccess(resp, out, "Issue assigned successfully.", updated);

        } catch (Exception e) {
            writeError(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, int issueId) {
        try {
            Map<?, ?> body = mapper.readValue(req.getInputStream(), Map.class);
            Integer supporterId = toInt(body.get("supporterId"));
            String classification = (String) body.get("classification");
            String status = (String) body.get("status");
            String resolutionComment = (String) body.get("resolutionComment");

            if (supporterId == null) {
                writeError(resp, out, HttpServletResponse.SC_BAD_REQUEST, "supporterId is required.");
                return;
            }
            if (!inList(classification, ALLOWED_CLASSIFICATIONS)) {
                writeError(resp, out, HttpServletResponse.SC_BAD_REQUEST,
                        "classification must be Baja, Media or Alta.");
                return;
            }
            if (!inList(status, ALLOWED_STATUSES)) {
                writeError(resp, out, HttpServletResponse.SC_BAD_REQUEST,
                        "status must be Ingresado, En Progreso or Resuelto.");
                return;
            }

            Issue issue = issueData.findById(issueId);
            if (issue == null) {
                writeError(resp, out, HttpServletResponse.SC_NOT_FOUND, "Issue not found.");
                return;
            }
            if (issue.getSupporterId() == 0) {
                writeError(resp, out, HttpServletResponse.SC_CONFLICT,
                        "This issue is not assigned to any supporter.");
                return;
            }
            if (issue.getSupporterId() != supporterId) {
                writeError(resp, out, HttpServletResponse.SC_FORBIDDEN,
                        "This issue is assigned to another supporter.");
                return;
            }
            if (isClosed(issue.getStatus())) {
                writeError(resp, out, HttpServletResponse.SC_CONFLICT, "Resolved issues cannot be edited.");
                return;
            }

            issueData.updateAssignedIssue(issueId, classification, status, resolutionComment);

            Issue updated = issueData.findById(issueId);

            // Support DB update succeeded: push the change to the client backend
            // through the Gateway (async, daemon thread). Resolved issues stop tracking.
            IssueSyncManager.getInstance().triggerSync(updated);
            try {
                if (updated != null) {
                    clientStatusClient.updateClientIssueStatus(
                            updated.getReference(),
                            updated.getStatus(),
                            updated.getResolutionComment()
                    );
                }
            } catch (Exception syncEx) {
                System.err.println("No se pudo sincronizar asignación con cliente: "
                        + syncEx.getMessage());
            }

            writeSuccess(resp, out, "Issue assigned successfully.", updated);

        } catch (Exception e) {
            writeError(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    // ── helpers ──────────────────────────────────────────────
    private void prepare(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        setCors(resp);
    }

    private String[] pathParts(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            return new String[0];
        }
        return pathInfo.replaceAll("^/", "").split("/");
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

    private boolean isClosed(String status) {
        return inList(status, CLOSED_STATUSES);
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

    private void writeError(HttpServletResponse resp, PrintWriter out, int status, String message) {
        resp.setStatus(status);
        out.print("{\"error\":\"" + escapeJson(message) + "\"}");
        out.flush();
    }

    private void writeSuccess(HttpServletResponse resp, PrintWriter out, String message, Issue issue) {
        resp.setStatus(HttpServletResponse.SC_OK);
        out.print("{\"message\":\"" + escapeJson(message) + "\",\"issue\":" + issueToJson(issue) + "}");
        out.flush();
    }

    private String listToJson(ArrayList<Issue> list) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < list.size(); i++) {
            json.append(issueToJson(list.get(i)));
            if (i < list.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private String issueToJson(Issue issue) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(issue.getId()).append(",");
        json.append("\"reference\":\"").append(escapeJson(issue.getReference())).append("\",");
        json.append("\"classification\":\"").append(escapeJson(issue.getClassification())).append("\",");
        json.append("\"status\":\"").append(escapeJson(issue.getStatus())).append("\",");
        json.append("\"issueTimestamp\":\"").append(issue.getIssueTimestamp() != null ? issue.getIssueTimestamp().toString() : "").append("\",");
        json.append("\"resolutionComment\":\"").append(escapeJson(issue.getResolutionComment())).append("\",");
        json.append("\"serviceId\":").append(issue.getServiceId()).append(",");
        json.append("\"serviceName\":\"").append(escapeJson(issue.getServiceName())).append("\",");
        json.append("\"supporterId\":").append(issue.getSupporterId()).append(",");
        json.append("\"supervisorId\":").append(issue.getSupervisorId()).append(",");
        json.append("\"issueDescription\":\"").append(escapeJson(issue.getIssueDescription())).append("\"");
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
