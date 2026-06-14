package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.data.SupervisorData;
import model.entities.Supervisor;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

        @WebServlet("/api/supervisors")
        public class SupervisorController extends HttpServlet {

        private final SupervisorData supervisorData = new SupervisorData();
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
                ArrayList < Supervisor > supervisors = supervisorData.getAll();
                        resp.setStatus(HttpServletResponse.SC_OK);
   
                        mapper.writeValue(resp.getWriter(), supervisors);
                } catch (Exception e) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                mapper.writeValue(resp.getWriter(), Map.of("error", "Error al obtener supervisores: " + e.getMessage()));
        }
        }
        }

