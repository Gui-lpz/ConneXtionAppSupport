package chat.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/* Representa un mensaje de chat entre cliente y soportista. Se serializa como
  JSON simple para enviarse por el socket.
 
  Protocolo de texto:
  MSG:<issueId>:<senderRole>:<senderName>:<timestamp>:<text>
 
  Ejemplo: MSG:42:CLIENT:Ana Mora:2026-06-15T10:30:00:Hola, mi internet no
  funciona

  FIX en deserialize(): el timestamp ISO (ej: "2026-06-20T19:52:21.659423")
  contiene ":" dentro de sí mismo (entre horas, minutos y segundos). El split()
  original por conteo de partes (limit=6) se confundía con esos ":" extra y
  mezclaba parte del timestamp con el texto del mensaje. Ahora se parsean
  los primeros 3 campos (issueId, role, name) por posición con indexOf,
  y el resto de la línea se separa timestamp/texto buscando el primer ":"
  que sigue al patrón de fecha ISO (10 dígitos + 'T' + hora), que es estable
  y no requiere cambiar el separador ni el protocolo existente. */
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

    /* Serializa el mensaje a una línea de texto para enviarlo por socket.
       Formato y separador sin cambios respecto al original. */
    public String serialize() {
        return PREFIX + SEPARATOR
                + issueId + SEPARATOR
                + senderRole + SEPARATOR
                + senderName + SEPARATOR
                + timestamp + SEPARATOR
                + text;
    }

    /**
     * Construye un ChatMessage desde una línea serializada. Retorna null si el
     * formato es inválido.
     *
     * FIX: en lugar de split(SEPARATOR, 6) que cuenta ":" a ciegas, parseamos
     * issueId, role y name por posición (nunca contienen ":"), y luego
     * separamos timestamp de texto usando el patrón fijo del timestamp
     * ISO_LOCAL_DATE_TIME, que siempre tiene esta forma:
     * yyyy-MM-ddTHH:mm:ss(.nnnnnnnnn)? Buscamos dónde termina ese patrón con
     * una regex anclada al inicio del resto de la línea, en vez de contar
     * separadores.
     */
    public static ChatMessage deserialize(String line) {
        if (line == null || !line.startsWith(PREFIX + SEPARATOR)) {
            return null;
        }

        try {
            // Quitar "MSG:" del inicio
            String rest = line.substring((PREFIX + SEPARATOR).length());

            // 1. issueId — hasta el siguiente ":"
            int idx1 = rest.indexOf(SEPARATOR);
            if (idx1 < 0) {
                return null;
            }
            String issueIdStr = rest.substring(0, idx1);
            rest = rest.substring(idx1 + 1);

            // 2. senderRole — hasta el siguiente ":"
            int idx2 = rest.indexOf(SEPARATOR);
            if (idx2 < 0) {
                return null;
            }
            String role = rest.substring(0, idx2);
            rest = rest.substring(idx2 + 1);

            // 3. senderName — hasta el siguiente ":"
            int idx3 = rest.indexOf(SEPARATOR);
            if (idx3 < 0) {
                return null;
            }
            String name = rest.substring(0, idx3);
            rest = rest.substring(idx3 + 1);

            // 4. timestamp — patrón fijo ISO_LOCAL_DATE_TIME:
            //    yyyy-MM-ddTHH:mm:ss  con nanosegundos opcionales (.NNNNNNNNN)
            //    Ejemplo: 2026-06-20T19:52:21.659423
            //    Contamos: 10 (fecha) + 1 (T) + 8 (HH:mm:ss) = 19 caracteres fijos,
            //    luego un "." opcional seguido de hasta 9 dígitos de nanosegundos.
            int fixedLen = 19; // "yyyy-MM-ddTHH:mm:ss"
            if (rest.length() < fixedLen) {
                return null;
            }

            int tsEnd = fixedLen;
            if (tsEnd < rest.length() && rest.charAt(tsEnd) == '.') {
                tsEnd++; // saltar el punto
                while (tsEnd < rest.length() && Character.isDigit(rest.charAt(tsEnd))) {
                    tsEnd++;
                }
            }

            // El siguiente carácter después del timestamp debe ser el separador ":"
            if (tsEnd >= rest.length() || rest.charAt(tsEnd) != ':') {
                return null;
            }

            String timestamp = rest.substring(0, tsEnd);
            String text = rest.substring(tsEnd + 1); // todo lo demás es el texto, sin tocar

            ChatMessage msg = new ChatMessage();
            msg.issueId = Integer.parseInt(issueIdStr);
            msg.senderRole = role;
            msg.senderName = name;
            msg.timestamp = timestamp;
            msg.text = text;
            return msg;

        } catch (Exception e) {
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
