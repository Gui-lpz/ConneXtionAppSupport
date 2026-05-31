package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import model.data.SupervisorData;
import model.data.SupporterData;
import model.entities.Supervisor;
import model.entities.Supporter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * POST /api/login Body: { "email": "...", "password": "...", "role":
 * "SUPPORTER" | "SUPERVISOR" } Si no se envía role, intenta Supervisor primero,
 * luego Supporter.
 */
@WebServlet("/api/login")
public class LoginController extends HttpServlet {

    private final SupporterData supporterData = new SupporterData();
    private final SupervisorData supervisorData = new SupervisorData();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        try {
            Map<?, ?> body = mapper.readValue(req.getInputStream(), Map.class);
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            String roleHint = body.get("role") != null
                    ? ((String) body.get("role")).toUpperCase() : null;

            if (email == null || password == null) {
                resp.setStatus(400);
                mapper.writeValue(resp.getWriter(), error("Campos 'email' y 'password' requeridos."));
                return;
            }

            if ("SUPERVISOR".equals(roleHint)) {
                Supervisor sv = supervisorData.login(email, password);
                if (sv != null) {
                    okSupervisor(req, resp, sv);
                    return;
                }

            } else if ("SUPPORTER".equals(roleHint)) {
                Supporter sp = supporterData.login(email, password);
                if (sp != null) {
                    okSupporter(req, resp, sp);
                    return;
                }

            } else {
                // Sin hint: probar ambos
                Supervisor sv = supervisorData.login(email, password);
                if (sv != null) {
                    okSupervisor(req, resp, sv);
                    return;
                }

                Supporter sp = supporterData.login(email, password);
                if (sp != null) {
                    okSupporter(req, resp, sp);
                    return;
                }
            }

            resp.setStatus(401);
            mapper.writeValue(resp.getWriter(), error("Credenciales incorrectas."));

        } catch (Exception e) {
            resp.setStatus(500);
            mapper.writeValue(resp.getWriter(), error("Error interno: " + e.getMessage()));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(204);
    }

    // ── helpers ──────────────────────────────────────────────
    private void okSupervisor(HttpServletRequest req, HttpServletResponse resp, Supervisor sv)
            throws Exception {
        saveSession(req, sv.getId(), sv.getFullName(), "SUPERVISOR");
        Map<String, Object> d = new HashMap<>();
        d.put("id", sv.getId());
        d.put("fullName", sv.getFullName());
        d.put("email", sv.getEmail());
        d.put("role", "SUPERVISOR");
        d.put("serviceId", sv.getServiceId());
        resp.setStatus(200);
        mapper.writeValue(resp.getWriter(), d);
    }

    private void okSupporter(HttpServletRequest req, HttpServletResponse resp, Supporter sp)
            throws Exception {
        saveSession(req, sp.getId(), sp.getFullName(), "SUPPORTER");
        Map<String, Object> d = new HashMap<>();
        d.put("id", sp.getId());
        d.put("fullName", sp.getFullName());
        d.put("email", sp.getEmail());
        d.put("role", "SUPPORTER");
        d.put("serviceId", sp.getServiceId());
        resp.setStatus(200);
        mapper.writeValue(resp.getWriter(), d);
    }

    private void saveSession(HttpServletRequest req, int id, String name, String role) {
        HttpSession s = req.getSession(true);
        s.setAttribute("userId", id);
        s.setAttribute("userName", name);
        s.setAttribute("role", role);
    }

    private Map<String, String> error(String msg) {
        Map<String, String> m = new HashMap<>();
        m.put("error", msg);
        return m;
    }
}
