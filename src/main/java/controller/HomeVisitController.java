package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import model.data.HomeVisitData;
import model.entities.HomeVisit;

@WebServlet("/api/home-visits")
public class HomeVisitController extends HttpServlet {

    private final HomeVisitData homeVisitData = new HomeVisitData();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = response.getWriter();

        try {
            String issueIdParam = request.getParameter("issueId");
            String supporterIdParam = request.getParameter("supporterId");

            ArrayList<HomeVisit> visits;

            if (issueIdParam != null && !issueIdParam.isEmpty()) {
                visits = homeVisitData.getByIssueId(Integer.parseInt(issueIdParam));
            } else if (supporterIdParam != null && !supporterIdParam.isEmpty()) {
                visits = homeVisitData.getBySupporterId(Integer.parseInt(supporterIdParam));
            } else {
                visits = homeVisitData.getAll();
            }

            out.print(convertListToJson(visits));
            out.flush();

        } catch (SQLException | ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Error al cargar las visitas: " + escapeJson(e.getMessage()) + "\"}");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Parámetro numérico inválido\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = response.getWriter();

        try {
            int issueId = Integer.parseInt(request.getParameter("issueId"));
            int supporterId = Integer.parseInt(request.getParameter("supporterId"));
            String visitDateTimeText = request.getParameter("visitDateTime");
            String address = request.getParameter("address");
            String contactPhone = request.getParameter("contactPhone");
            String observations = request.getParameter("observations");

            String status = request.getParameter("status");
            if (status == null || status.trim().isEmpty()) {
                status = "Programada";
            }

            LocalDateTime visitDateTime = LocalDateTime.parse(visitDateTimeText);

            HomeVisit visit = new HomeVisit(
                    0,
                    issueId,
                    supporterId,
                    visitDateTime,
                    address,
                    contactPhone,
                    status,
                    observations
            );

            homeVisitData.add(visit);

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print("{\"message\":\"Visita a domicilio programada correctamente\"}");

        } catch (SQLException | ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Error al registrar la visita: " + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Datos inválidos para programar la visita: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = response.getWriter();

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String action = request.getParameter("action");

            if ("complete".equalsIgnoreCase(action)) {
                String observations = request.getParameter("observations");
                homeVisitData.completeVisit(id, observations);
                out.print("{\"message\":\"Visita completada correctamente\"}");
                return;
            }

            if ("cancel".equalsIgnoreCase(action)) {
                String observations = request.getParameter("observations");
                homeVisitData.cancelVisit(id, observations);
                out.print("{\"message\":\"Visita cancelada correctamente\"}");
                return;
            }

            if ("status".equalsIgnoreCase(action)) {
                String status = request.getParameter("status");
                homeVisitData.updateStatus(id, status);
                out.print("{\"message\":\"Estado de la visita actualizado correctamente\"}");
                return;
            }

            int issueId = Integer.parseInt(request.getParameter("issueId"));
            int supporterId = Integer.parseInt(request.getParameter("supporterId"));
            LocalDateTime visitDateTime = LocalDateTime.parse(request.getParameter("visitDateTime"));
            String address = request.getParameter("address");
            String contactPhone = request.getParameter("contactPhone");
            String status = request.getParameter("status");
            String observations = request.getParameter("observations");

            HomeVisit visit = new HomeVisit(
                    id,
                    issueId,
                    supporterId,
                    visitDateTime,
                    address,
                    contactPhone,
                    status,
                    observations
            );

            homeVisitData.update(visit);
            out.print("{\"message\":\"Visita actualizada correctamente\"}");

        } catch (SQLException | ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Error al actualizar la visita: " + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Datos inválidos para actualizar la visita: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);
        PrintWriter out = response.getWriter();

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            homeVisitData.delete(id);

            out.print("{\"message\":\"Visita eliminada correctamente\"}");

        } catch (SQLException | ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Error al eliminar la visita: " + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Debe indicar un ID válido\"}");
        }
    }

    private String convertListToJson(ArrayList<HomeVisit> list) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < list.size(); i++) {
            HomeVisit visit = list.get(i);

            json.append("{");
            json.append("\"id\":").append(visit.getId()).append(",");
            json.append("\"issueId\":").append(visit.getIssueId()).append(",");
            json.append("\"supporterId\":").append(visit.getSupporterId()).append(",");
            json.append("\"visitDateTime\":\"")
                    .append(visit.getVisitDateTime() != null ? visit.getVisitDateTime().toString() : "")
                    .append("\",");
            json.append("\"address\":\"").append(escapeJson(visit.getAddress())).append("\",");
            json.append("\"contactPhone\":\"").append(escapeJson(visit.getContactPhone())).append("\",");
            json.append("\"status\":\"").append(escapeJson(visit.getStatus())).append("\",");
            json.append("\"observations\":\"").append(escapeJson(visit.getObservations())).append("\"");
            json.append("}");

            if (i < list.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }

    private void setJsonResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        setCorsHeaders(response);
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
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