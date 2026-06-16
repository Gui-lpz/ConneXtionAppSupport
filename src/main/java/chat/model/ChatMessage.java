package chat.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/* Representa un mensaje de chat entre cliente y soportista. Se serializa como
  JSON simple para enviarse por el socket.
 
  Protocolo de texto:
  MSG:<issueId>:<senderRole>:<senderName>:<timestamp>:<text>
 
  Ejemplo: MSG:42:CLIENT:Ana Mora:2026-06-15T10:30:00:Hola, mi internet no
  funciona */
public class ChatMessage {

    public static final String PREFIX = "MSG";
    public static final String SEPARATOR = ":";

    // Roles posibles del remitente
    public static final String ROLE_CLIENT = "CLIENT";
    public static final String ROLE_SUPPORTER = "SUPPORTER";

    private int issueId;      // ID del tiquete/solicitud al que pertenece este chat
    private String senderRole;   // CLIENT o SUPPORTER
    private String senderName;   // Nombre completo del remitente
    private String timestamp;    // ISO-8601 local
    private String text;         // Contenido del mensaje

    public ChatMessage() {
    }

    public ChatMessage(int issueId, String senderRole, String senderName, String text) {
        this.issueId = issueId;
        this.senderRole = senderRole;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /* Serializa el mensaje a una línea de texto para enviarlo por socket. El
    texto del mensaje puede contener espacios pero no el separador " : ". Si
    se necesitara  soportar " : " en los mensajes.*/
    
    public String serialize() {
        return PREFIX + SEPARATOR
                + issueId + SEPARATOR
                + senderRole + SEPARATOR
                + senderName + SEPARATOR
                + timestamp + SEPARATOR
                + text;
    }

    /* Construye un ChatMessage desde una línea serializada. Retorna null si el
      formato es inválido. */
    public static ChatMessage deserialize(String line) {
        if (line == null || !line.startsWith(PREFIX + SEPARATOR)) {
            return null;
        }

        // Dividir en máximo 6 partes (el texto puede contener " : ")
        String[] parts = line.split(SEPARATOR, 6);
        if (parts.length < 6) {
            return null;
        }

        try {
            ChatMessage msg = new ChatMessage();
            msg.issueId = Integer.parseInt(parts[1]);
            msg.senderRole = parts[2];
            msg.senderName = parts[3];
            msg.timestamp = parts[4];
            msg.text = parts[5];
            return msg;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int getIssueId() {
        return issueId;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public void setSenderRole(String role) {
        this.senderRole = role;
    }

    public void setSenderName(String name) {
        this.senderName = name;
    }

    public void setTimestamp(String ts) {
        this.timestamp = ts;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + senderName + " (" + senderRole + "): " + text;
    }
}
