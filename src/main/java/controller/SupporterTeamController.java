package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.data.ServiceData;
import model.data.SupporterData;
import model.entities.Service;
import model.entities.Supporter;

@WebServlet("/api/supporter/team")
public class SupporterTeamController extends HttpServlet {

    private final SupporterData supporterData = new SupporterData();
    private final ServiceData serviceData = new ServiceData();
    private final ObjectMapper mapper = new ObjectMapper();

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
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

        try {
            String supervisorIdParam = req.getParameter("supervisorId");

            if (supervisorIdParam == null || supervisorIdParam.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(), Map.of("error", "El parámetro supervisorId es obligatorio."));
                return;
            }

            int supervisorId;
            try {
                supervisorId = Integer.parseInt(supervisorIdParam.trim());
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(), Map.of("error", "supervisorId debe ser numérico."));
                return;
            }

            ArrayList<Supporter> team = supporterData.getBySupervisorId(supervisorId);

            List<Map<String, Object>> result = new ArrayList<>();
            for (Supporter sp : team) {
                ArrayList<Integer> serviceIds = supporterData.getServiceIds(sp.getId());

                List<Map<String, Object>> services = new ArrayList<>();
                for (Integer sid : serviceIds) {
                    Service svc = serviceData.findById(sid);
                    if (svc != null) {
                        Map<String, Object> s = new HashMap<>();
                        s.put("id", svc.getId());
                        s.put("name", svc.getName());
                        services.add(s);
                    }
                }

                Map<String, Object> entry = new HashMap<>();
                entry.put("id", sp.getId());
                entry.put("fullName", sp.getFullName());
                entry.put("email", sp.getEmail());
                entry.put("services", services);
                result.add(entry);
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), result);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of("error", "Error interno: " + e.getMessage()));
        }
    }
}
