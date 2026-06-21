package controller;

import chat.model.ChatClient;
import chat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servlet de chat — lado SOPORTE (ConneXtionAppSupport)
 *
 * El ChatServer corre embebido en este mismo proceso (AppSupport, puerto 9500).
 * Aun así, este servlet DEBE conectarse via ChatClient (socket + handshake
 * HELLO), porque llamar ChatServer.getOrCreateSession() directo en memoria
 * NUNCA registra un PrintWriter en la sesión — solo el handshake hace eso. Sin
 * handshake, supporterWriter queda null y route() nunca le entrega mensajes al
 * soporte.
 *
 * issueId se mantiene como int, sin cambios.
 *
 * GET /api/chat?issueId=42&supporterName=Carlos POST /api/chat Body: {
 * "issueId": 42, "senderName": "Carlos", "text": "..." }
 */
@WebServlet("/api/chat")
public class ChatServerSupport extends HttpServlet {

    // ChatServer corre en este mismo proceso (AppSupport), puerto 9500
    private static final String CHAT_HOST = "localhost";
    private static final int CHAT_PORT = 9500;

    private final ObjectMapper mapper = new ObjectMapper();

    // Un ChatClient activo por issueId
    private static final Map<Integer, ChatClient> clients = new ConcurrentHashMap<>();
    private static final Map<Integer, List<ChatMessage>> history = new ConcurrentHashMap<>();

    // ── GET: historial ───────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String issueIdParam = req.getParameter("issueId");
        String supporterName = req.getParameter("supporterName");

        if (issueIdParam == null) {
            sendError(resp, 400, "Parámetro 'issueId' requerido.");
            return;
        }

        try {
            int issueId = Integer.parseInt(issueIdParam);
            String name = supporterName != null ? supporterName : "Soportista";

            ensureConnected(issueId, name);

            List<ChatMessage> sessionHistory
                    = history.computeIfAbsent(issueId, k -> new CopyOnWriteArrayList<>());

            resp.setStatus(200);
            mapper.writeValue(resp.getWriter(), serialize(sessionHistory));

        } catch (NumberFormatException e) {
            sendError(resp, 400, "issueId debe ser un número entero.");
        }
    }

    // ── POST: enviar mensaje ──────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");

        try {
            Map<?, ?> body = mapper.readValue(req.getInputStream(), Map.class);

            int issueId = Integer.parseInt(body.get("issueId").toString());
            String senderName = (String) body.get("senderName");
            String text = (String) body.get("text");

            if (text == null || text.isBlank()) {
                sendError(resp, 400, "El campo 'text' es requerido.");
                return;
            }

            String name = senderName != null ? senderName : "Soportista";
            ensureConnected(issueId, name);

            ChatClient client = clients.get(issueId);
            if (client == null || !client.isConnected()) {
                sendError(resp, 503, "No se pudo conectar al servidor de chat.");
                return;
            }

            // Guardar en historial local antes de enviar
            ChatMessage msg = new ChatMessage(issueId, ChatMessage.ROLE_SUPPORTER, name, text.trim());
            history.computeIfAbsent(issueId, k -> new CopyOnWriteArrayList<>()).add(msg);

            client.send(text.trim());

            Map<String, Object> ok = new HashMap<>();
            ok.put("ok", true);
            ok.put("timestamp", msg.getTimestamp());
            resp.setStatus(200);
            mapper.writeValue(resp.getWriter(), ok);

        } catch (Exception e) {
            sendError(resp, 500, "Error al procesar el mensaje: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    /**
     * Conecta via socket al ChatServer (mismo proceso, puerto 9500) y hace el
     * handshake HELLO. Esto es lo que faltaba: sin esto, supporterWriter queda
     * null en la ChatSession y los mensajes del cliente nunca llegan.
     */
    private synchronized void ensureConnected(int issueId, String userName) {
        ChatClient existing = clients.get(issueId);
        if (existing != null && existing.isConnected()) {
            return;
        }

        ChatClient client = new ChatClient(
                CHAT_HOST, CHAT_PORT, issueId,
                ChatMessage.ROLE_SUPPORTER, userName);

        client.setMessageListener(new ChatClient.MessageListener() {
            @Override
            public void onMessage(ChatMessage msg) {
                // Mensaje recibido del cliente → guardar en historial
                history.computeIfAbsent(issueId, k -> new CopyOnWriteArrayList<>()).add(msg);
            }

            @Override
            public void onError(String error) {
                System.err.println("[Chat Soporte issueId=" + issueId + "] Error: " + error);
            }

            @Override
            public void onConnected() {
                System.out.println("[Chat Soporte issueId=" + issueId + "] Conectado al ChatServer.");
            }

            @Override
            public void onDisconnected() {
                System.out.println("[Chat Soporte issueId=" + issueId + "] Desconectado.");
                clients.remove(issueId);
            }
        });

        try {
            client.connect();
            clients.put(issueId, client);
        } catch (IOException e) {
            System.err.println("[Chat Soporte] No se pudo conectar al ChatServer en "
                    + CHAT_HOST + ":" + CHAT_PORT + " — " + e.getMessage());
        }
    }

    private List<Map<String, Object>> serialize(List<ChatMessage> msgs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatMessage m : msgs) {
            Map<String, Object> map = new HashMap<>();
            map.put("issueId", m.getIssueId());
            map.put("senderRole", m.getSenderRole());
            map.put("senderName", m.getSenderName());
            map.put("timestamp", m.getTimestamp());
            map.put("text", m.getText());
            result.add(map);
        }
        return result;
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCors(resp);
        resp.setStatus(204);
    }

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendError(HttpServletResponse resp, int status, String msg) throws IOException {
        resp.setStatus(status);
        Map<String, String> err = new HashMap<>();
        err.put("error", msg);
        mapper.writeValue(resp.getWriter(), err);
    }
}
