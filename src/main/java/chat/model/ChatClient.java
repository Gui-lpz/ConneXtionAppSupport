package chat.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*Cliente de socket para el chat de ConneXtion.
 
  Esta clase NO es Swing ni web, es la capa de transporte pura.
  El servlet (ChatServerClient o ChatServerSupport) la usa para
  conectarse al ChatServer y enviar/recibir mensajes.
 
 Ciertas correcciones respecto a la versión que se subió anterior al github:
   - isConnected() verifica el estado real del socket, no solo el flag.
   - connect() tiene timeout de 5 segundos para no bloquearse indefinidamente.
   - send() detecta errores del PrintWriter y marca la conexión como caída.
   - disconnect() es seguro para llamar múltiples veces (idempotente).
   - readLoop() limpia el socket correctamente al terminar. */
public class ChatClient {

    private static final int CONNECT_TIMEOUT_MS = 5000; // 5 segundos

    private final String host;
    private final int port;
    private final int issueId;
    private final String role;
    private final String userName;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private MessageListener listener;
    private volatile boolean connected = false;

    public ChatClient(String host, int port, int issueId, String role, String userName) {
        this.host = host;
        this.port = port;
        this.issueId = issueId;
        this.role = role;
        this.userName = userName;
    }

    //Listener 
    public interface MessageListener {

        void onMessage(ChatMessage message);

        void onError(String error);

        void onConnected();

        void onDisconnected();
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    // Conexión 

    /* Conecta al ChatServer, realiza el handshake y lanza el hilo de lectura.
      No bloquea el hilo llamante más allá del handshake.
      Tiene timeout de 5 segundos — si el server no responde, lanza IOException. */
    public void connect() throws IOException {
        // FIX: timeout para no quedarse bloqueado si el server no responde
        socket = new Socket();
        socket.connect(
                new java.net.InetSocketAddress(host, port),
                CONNECT_TIMEOUT_MS
        );
        socket.setSoTimeout(0); // Sin timeout para el readLoop (lectura continua)

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Handshake: HELLO:<role>:<issueId>:<userName>
        out.println("HELLO:" + role + ":" + issueId + ":" + userName);

        // FIX: leer con timeout para no bloquearse en el handshake
        socket.setSoTimeout(CONNECT_TIMEOUT_MS);
        String response = in.readLine();
        socket.setSoTimeout(0); // Volver a sin timeout para el readLoop

        if (!"OK".equals(response)) {
            closeSocket();
            throw new IOException("Handshake rechazado: " + response);
        }

        connected = true;
        if (listener != null) {
            listener.onConnected();
        }

        Thread readerThread = new Thread(this::readLoop, "ChatReader-" + issueId + "-" + role);
        readerThread.setDaemon(true);
        readerThread.start();
    }

    // Envío
    // Envía un mensaje de texto al otro participante.
    public void send(String text) {
        if (!isConnected()) {
            return;
        }

        ChatMessage msg = new ChatMessage(issueId, role, userName, text);
        out.println(msg.serialize());

        // FIX: PrintWriter traga las excepciones — checkError() detecta si falló
        if (out.checkError()) {
            System.err.println("[ChatClient issueId=" + issueId + "] Error al enviar mensaje.");
            connected = false;
            if (listener != null) {
                listener.onError("Error al enviar mensaje.");
            }
        }
    }

    //  Desconexión
    public void disconnect() {
        if (!connected) {
            return;
        }
        connected = false;

        try {
            if (out != null) {
                out.println("BYE");
            }
        } catch (Exception ignored) {
        }

        closeSocket();

        if (listener != null) {
            listener.onDisconnected();
        }
    }

    //Estado 
    /*Verifica el estado real del socket además del flag connected.
      La versión original solo verificaba el flag, que podía quedar desactualizado
      si el socket caía de forma inesperada.*/
    public boolean isConnected() {
        return connected
                && socket != null
                && !socket.isClosed()
                && socket.isConnected();
    }

    // Helpers 
    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    // Loop de lectura
    // Corre en su propio hilo daemon. Deserializa cada línea recibida y notifica al listener.
    private void readLoop() {
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                ChatMessage msg = ChatMessage.deserialize(line);
                if (msg != null && listener != null) {
                    listener.onMessage(msg);
                }
            }
        } catch (IOException e) {
            if (connected && listener != null) {
                listener.onError("Conexión perdida: " + e.getMessage());
            }
        } finally {
            connected = false;
            closeSocket(); // FIX: cerrar socket al terminar el readLoop
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }
}
