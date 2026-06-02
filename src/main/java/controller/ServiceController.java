package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import model.data.IssueData;
import model.entities.Issue;

@WebServlet("/api/issues/incoming")
public class ServiceController extends HttpServlet {

    private final IssueData issueData = new IssueData();
    private final ObjectMapper mapper = new ObjectMapper();

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

        try {
            Map<?, ?> body = mapper.readValue(req.getInputStream(), Map.class);
            String reference = (String) body.get("reference");
            int serviceId = ((Number) body.get("serviceId")).intValue();

            if (reference == null || reference.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(), Map.of("error", "El campo reference es obligatorio."));
                return;
            }

            Issue issue = new Issue();
            issue.setReference(reference);
            issue.setServiceId(serviceId);

            issueData.add(issue);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(resp.getWriter(),
                    Map.of("message", "Issue registrado en soporte.", "reference", reference));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of("error", e.getMessage()));
        }
    }
}