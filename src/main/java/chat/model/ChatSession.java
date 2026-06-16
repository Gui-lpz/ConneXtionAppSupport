package chat.model;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/* Representa una sesión de chat activa asociada a un tiquete específico.
 
  Contiene los dos extremos de la conversación: - El PrintWriter del cliente
  (para enviarle mensajes) - El PrintWriter del soportista (para enviarle
  mensajes)
 
  El ChatServer mantiene un Map<Integer, ChatSession> donde la clave es el
  issueId. Cuando llega un MSG para el issueId X, el server busca la sesión X y
  lo reenvía al otro extremo.*/
public class ChatSession {

    private final int issueId;

    // PrintWriter del cliente conectado (null si aún no conectó)
    private PrintWriter clientWriter;

    // PrintWriter del soportista conectado (null si aún no conectó)
    private PrintWriter supporterWriter;

    // Historial de mensajes de esta sesión (en memoria por el momento)
    private final List<ChatMessage> history = new ArrayList<>();

    public ChatSession(int issueId) {
        this.issueId = issueId;
    }

    /* Envía un mensaje al otro extremo de la conversación. Si el destinatario
      no está conectado, el mensaje se pierde (en una versión más completa se
      podría persistir y entregar luego).*/
    public void route(ChatMessage msg) {
        history.add(msg);

        String line = msg.serialize();

        if (ChatMessage.ROLE_CLIENT.equals(msg.getSenderRole())) {
            // SI el cliente envió mensaje, se reenvia al soportista
            if (supporterWriter != null) {
                supporterWriter.println(line);
            }
        } else {
            // Si el soportista envió mensaje, se reenvia al cliente
            if (clientWriter != null) {
                clientWriter.println(line);
            }
        }
    }

    // Verifica si ambos participantes están conectados
    public boolean isFull() {
        return clientWriter != null && supporterWriter != null;
    }

    public int getIssueId() {
        return issueId;
    }

    public PrintWriter getClientWriter() {
        return clientWriter;
    }

    public PrintWriter getSupporterWriter() {
        return supporterWriter;
    }

    public List<ChatMessage> getHistory() {
        return history;
    }

    public void setClientWriter(PrintWriter w) {
        this.clientWriter = w;
    }

    public void setSupporterWriter(PrintWriter w) {
        this.supporterWriter = w;
    }
}
