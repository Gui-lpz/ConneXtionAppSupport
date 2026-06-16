package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.data.IssueData;
import model.entities.Issue;

@WebServlet("/api/issues")
public class IssueController extends HttpServlet {

    private final IssueData issueData = new IssueData();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*"); // Permite peticiones desde el frontend
        String role = request.getParameter("role");
        String userIdStr = request.getParameter("userId");

        PrintWriter out = response.getWriter();
        try {
            ArrayList<Issue> issuesList;

            // Lógica de filtrado basada en el rol del usuario
            if ("SUPERVISOR".equalsIgnoreCase(role)) {
                // El supervisor ve TODOS los tiquetes
                issuesList = issueData.getAll();
            } else if ("SUPPORT".equalsIgnoreCase(role) && userIdStr != null) {
                // El soportista solo ve LOS SUYOS usando tu método existente
                int userId = Integer.parseInt(userIdStr);
                issuesList = issueData.getBySupporterId(userId);
            } else {
                // Por seguridad, si no es ninguno, devolvemos lista vacía
                issuesList = new ArrayList<>();
            }

            // Convertimos a JSON y enviamos
            out.print(convertListToJson(issuesList));
            out.flush();
            /*try {
            ArrayList<Issue> issuesList = issueData.getAll();
            String jsonResponse = convertListToJson(issuesList);
            out.print(jsonResponse);
            out.flush();*/
        } catch (SQLException | ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error al cargar los reportes: " + e.getMessage() + "\"}");
        }
    }

    private String convertListToJson(ArrayList<Issue> list) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < list.size(); i++) {
            Issue issue = list.get(i);
            json.append("{");
            json.append("\"id\":").append(issue.getId()).append(",");
            json.append("\"reference\":\"").append(escapeJson(issue.getReference())).append("\",");
            json.append("\"description\":\"").append(escapeJson(issue.getDescription())).append("\",");
            json.append("\"classification\":\"").append(escapeJson(issue.getClassification())).append("\",");
            json.append("\"status\":\"").append(escapeJson(issue.getStatus())).append("\",");
            json.append("\"issueTimestamp\":\"")
                    .append(issue.getIssueTimestamp() != null ? issue.getIssueTimestamp().toString() : "")
                    .append("\",");
            json.append("\"resolutionComment\":\"").append(escapeJson(issue.getResolutionComment())).append("\",");
            json.append("\"contactAddress\":\"").append(escapeJson(issue.getContactAddress())).append("\",");
            json.append("\"contactPhone\":\"").append(escapeJson(issue.getContactPhone())).append("\",");
            json.append("\"contactEmail\":\"").append(escapeJson(issue.getContactEmail())).append("\",");
            json.append("\"serviceId\":").append(issue.getServiceId()).append(",");
            json.append("\"serviceName\":\"").append(escapeJson(issue.getServiceName())).append("\",");
            json.append("\"supporterId\":").append(issue.getSupporterId()).append(",");
            json.append("\"supervisorId\":").append(issue.getSupervisorId()).append(",");
            json.append("\"issueDescription\":\"").append(escapeJson(issue.getIssueDescription())).append("\"");
            json.append("}");

            if (i < list.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
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
