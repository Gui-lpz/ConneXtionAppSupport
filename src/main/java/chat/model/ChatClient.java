package chat.model;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*Cliente de socket para el chat de ConneXtion.
 
  Esta clase NO es Swing ni web, es la capa de transporte pura. La UI
  (frontend web o Servlet) la usa para conectarse al ChatServer y
  enviar/recibir mensajes.
 
  Cómo esto se puede integrar al proyecto: En ConneXtionHelpDesk_Cliente (app del
  cliente): Un servlet o endpoint REST instancia ChatClient con role = CLIENT.

  En ConneXtionAppSupport (app del soportista): Un servlet o endpoint REST
  instancia ChatClient con role = SUPPORTER. 

  El frontend hace polling o usa SSE (Server-Sent Events) al servlet para recibir mensajes nuevos en tiempo real.
 
  uso básico de esta implementación: ChatClient client = new ChatClient("localhost", 9500, 42,
  "CLIENT", "Ana Mora"); client.connect(); client.send("Hola, mi internet no
  funciona"); "Los mensajes recibidos llegan al MessageListener"

  client.disconnect();*/

public class ChatClient {

    private final String host;
    private final int port;
    private final int issueId;
    private final String role;
    private final String userName;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private MessageListener listener;
    private boolean connected = false;

    /*
      @param host Host del ChatServer (ej: "localhost")
      @param port Puerto del ChatServer (ej: 9500)
      @param issueId ID del tiquete de soporte
      @param role ChatMessage.ROLE_CLIENT o ChatMessage.ROLE_SUPPORTER
      @param userName Nombre completo del usuario
     */
    
    public ChatClient(String host, int port, int issueId, String role, String userName) {
        this.host = host;
        this.port = port;
        this.issueId = issueId;
        this.role = role;
        this.userName = userName;
    }

    // Callback que recibe los mensajes entrantes del otro participante. La UI implementa esta interfaz para actualizar la pantalla.
    public interface MessageListener {

        void onMessage(ChatMessage message);

        void onError(String error);

        void onConnected();

        void onDisconnected();
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    // Conecta al servidor, realiza el handshake y lanza el hilo de lectura. No bloquea el hilo llamante.
    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Enviar handshake: HELLO:<role>:<issueId>:<userName>
        out.println("HELLO:" + role + ":" + issueId + ":" + userName);

        // Leer respuesta del server
        String response = in.readLine();
        if (!"OK".equals(response)) {
            socket.close();
            throw new IOException("Handshake rechazado por el servidor: " + response);
        }

        connected = true;
        if (listener != null) {
            listener.onConnected();
        }

        // Lanzar hilo de lectura (no bloqueante)
        Thread readerThread = new Thread(this::readLoop, "ChatReader-" + issueId);
        readerThread.setDaemon(true);
        readerThread.start();
    }

    //  Envía un mensaje de texto al otro participante.
    public void send(String text) {
        if (!connected || out == null) {
            return;
        }
        ChatMessage msg = new ChatMessage(issueId, role, userName, text);
        out.println(msg.serialize());
    }

    //Cierra la conexión limpiamente.
    public void disconnect() {
        connected = false;
        if (out != null) {
            out.println("BYE");
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        if (listener != null) {
            listener.onDisconnected();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    //Loop de lectura que corre en su propio hilo. Deserializa cada línea recibida y notifica al listener.
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
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }
}
