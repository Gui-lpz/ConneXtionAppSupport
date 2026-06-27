package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
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

@WebServlet("/api/supporter/register")
public class SupporterRegisterController extends HttpServlet {

    private static final int MAX_SERVICES = 2;

    private final SupporterData supporterData = new SupporterData();
    private final ServiceData serviceData = new ServiceData();
    private final ObjectMapper mapper = new ObjectMapper();

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

    // ── GET: devuelve los servicios disponibles ───────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        setCors(resp);

        try {
            ArrayList<Service> services = serviceData.getAll();

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < services.size(); i++) {
                Service s = services.get(i);
                json.append("{\"id\":").append(s.getId())
                        .append(",\"name\":\"").append(escapeJson(s.getName())).append("\"}");
                if (i < services.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(json.toString());

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of("error", e.getMessage()));
        }
    }

    // ── POST: registra el nuevo soportista ────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        setCors(resp);

        try {
            Map<?, ?> body = mapper.readValue(req.getInputStream(), Map.class);

            // ── 1. Leer campos ────────────────────────────────────────────────
            Integer supervisorId = toInt(body.get("supervisorId"));
            String name = (String) body.get("name");
            String firstSurname = (String) body.get("firstSurname");
            String secondSurname = (String) body.get("secondSurname");
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            List<?> serviceIds = (List<?>) body.get("serviceIds");

            // ── 2. Validar campos obligatorios ────────────────────────────────
            if (supervisorId == null || isBlank(name) || isBlank(firstSurname)
                    || isBlank(email) || isBlank(password)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(),
                        Map.of("error", "Los campos supervisorId, name, firstSurname, email y password son obligatorios."));
                return;
            }

            // ── 3. Validar áreas de servicio (OPCIONAL) ────────────────────────
            // Si el supervisor no marca la casilla de "área específica", serviceIds
            // viene vacío o null → el soportista trabaja en TODAS las áreas (1-4).
            List<Integer> parsedServiceIds = new ArrayList<>();

            boolean noAreasProvided = (serviceIds == null || serviceIds.isEmpty());

            if (!noAreasProvided) {
                if (serviceIds.size() > MAX_SERVICES) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    mapper.writeValue(resp.getWriter(),
                            Map.of("error", "Un soportista puede tener como máximo " + MAX_SERVICES + " áreas de servicio."));
                    return;
                }

                // Verificar que los IDs enviados existen en la BD y no están duplicados
                for (Object sid : serviceIds) {
                    Integer id = toInt(sid);
                    if (id == null) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        mapper.writeValue(resp.getWriter(), Map.of("error", "serviceIds contiene un valor inválido."));
                        return;
                    }
                    if (parsedServiceIds.contains(id)) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        mapper.writeValue(resp.getWriter(), Map.of("error", "No podés asignar el mismo servicio dos veces."));
                        return;
                    }
                    Service service = serviceData.findById(id);
                    if (service == null) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        mapper.writeValue(resp.getWriter(),
                                Map.of("error", "El servicio con id " + id + " no existe."));
                        return;
                    }
                    parsedServiceIds.add(id);
                }
            } else {
                // No se marcó la casilla de área específica → asignar TODAS las áreas existentes
                ArrayList<Service> allServices = serviceData.getAll();
                for (Service s : allServices) {
                    parsedServiceIds.add(s.getId());
                }
            }

            // ── 4. Verificar que el email no esté ya registrado ───────────────
            if (supporterData.existsByEmail(email.trim())) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                mapper.writeValue(resp.getWriter(),
                        Map.of("error", "Ya existe un soportista registrado con ese correo."));
                return;
            }

            // ── 5. Crear el soportista ────────────────────────────────────────
            Supporter supporter = new Supporter(
                    0,
                    name.trim(),
                    firstSurname.trim(),
                    secondSurname != null ? secondSurname.trim() : "",
                    email.trim(),
                    password,
                    0, // serviceId simple — se maneja via SupporterService
                    supervisorId
            );

            // Inserta en Supporter y en SupporterService (máx 2 filas)
            int newId = supporterData.addWithServices(supporter, parsedServiceIds);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(resp.getWriter(), Map.of(
                    "message", "Soportista registrado correctamente.",
                    "supporterId", newId,
                    "serviceIds", parsedServiceIds
            ));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Integer toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String && !((String) value).isBlank()) {
            try {
                return Integer.valueOf(((String) value).trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
