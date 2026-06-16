package chat.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* Servidor de chat para ConneXtion.
 
  PROTOCOLO DE HANDSHAKE (primeras líneas al conectar):
    Cliente envía  HELLO:<role>:<issueId>:<userName>
    Servidor responde con OK  si todo sale bien si no responde con ERROR:<motivo>  si algo falla
 
  PROTOCOLO DE MENSAJES (después del handshake):
    Cliente envía →  MSG:<issueId>:<role>:<name>:<timestamp>:<text>
    Servidor lo enruta al otro extremo de la sesión.
 
  DESCONEXIÓN:
    El cliente envía → BYE
    O cierra el socket directamente.
 
  SESIONES:
    El server mantiene un Map<issueId, ChatSession>.
    La primera persona en conectarse para un issueId crea la sesión.
    La segunda persona se une a la sesión existente.
    Cuando ambos se desconectan, la sesión se elimina.*/
public class ChatServer {

    // Puerto donde escucha el servidor de chat. Debe coincidir con el cliente.
    public static final int PORT = 8080;

    /* Sesiones activas: clave = issueId, valor = ChatSession.
      ConcurrentHashMap para acceso seguro desde múltiples hilos.*/
    private static final Map<Integer, ChatSession> sessions = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("ConneXtion ChatServer escuchando en puerto " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Cada conexión entrante obtiene su propio hilo
                new ClientHandler(clientSocket).start();
            }
        }
    }

    // Métodos de gestión de sesiones (llamados desde los hilos) 

    /*Obtiene o crea la sesión para el issueId dado.
      Sincronizado para evitar condiciones de carrera al crear.*/
    static synchronized ChatSession getOrCreateSession(int issueId) {
        return sessions.computeIfAbsent(issueId, ChatSession::new);
    }

    // Elimina la sesión si ambos participantes se han desconectado.
    static synchronized void cleanupSession(int issueId) {
        ChatSession session = sessions.get(issueId);
        if (session != null && !session.isFull()) {
            // Solo eliminar si quedó vacía (ambos escritores son null)
            if (session.getClientWriter() == null && session.getSupporterWriter() == null) {
                sessions.remove(issueId);
                System.out.println("Sesión " + issueId + " eliminada.");
            }
        }
    }

    //Hilo por cliente 

    /* Maneja la conexión de un único participante (cliente o soportista).
      Sigue el mismo patrón que el ChatServer de ejemplo:
        1. Handshake para identificar rol e issueId.
        2. Loop de lectura que enruta mensajes a la sesión.
        3. Limpieza al desconectar.*/
    private static class ClientHandler extends Thread {

        private final Socket socket;
        private PrintWriter out;
        private int issueId;
        private String role;
        private String userName;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                //1. Handshake 
                // Esperar: HELLO:<role>:<issueId>:<userName>
                String hello = in.readLine();
                if (!doHandshake(hello)) {
                    out.println("ERROR:Handshake inválido. Formato esperado: HELLO:<role>:<issueId>:<userName>");
                    return;
                }

                out.println("OK");
                System.out.println("[" + issueId + "] " + role + " '" + userName + "' conectado.");

                // 2. Loop de mensajes
                String line;
                while ((line = in.readLine()) != null) {
                    if ("BYE".equals(line)) {
                        break;
                    }

                    ChatMessage msg = ChatMessage.deserialize(line);
                    if (msg != null) {
                        ChatSession session = sessions.get(issueId);
                        if (session != null) {
                            session.route(msg);
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Error en hilo de chat: " + e.getMessage());
            } finally {
                disconnect();
            }
        }

        /* Parsea el mensaje de handshake y registra al participante en su sesión.
          Retorna true si fue exitoso. */
        private boolean doHandshake(String hello) {
            if (hello == null || !hello.startsWith("HELLO:")) {
                return false;
            }

            String[] parts = hello.split(":", 4);
            if (parts.length < 4) {
                return false;
            }

            role = parts[1]; // CLIENT o SUPPORTER
            userName = parts[3];

            try {
                issueId = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return false;
            }

            if (!ChatMessage.ROLE_CLIENT.equals(role)
                    && !ChatMessage.ROLE_SUPPORTER.equals(role)) {
                return false;
            }

            // Registrar en la sesión
            ChatSession session = getOrCreateSession(issueId);

            if (ChatMessage.ROLE_CLIENT.equals(role)) {
                session.setClientWriter(out);
            } else {
                session.setSupporterWriter(out);
            }

            return true;
        }

        // Al desconectarse, elimina el PrintWriter de la sesión y cierra el socket.
        private void disconnect() {
            if (issueId > 0) {
                ChatSession session = sessions.get(issueId);
                if (session != null) {
                    if (ChatMessage.ROLE_CLIENT.equals(role)) {
                        session.setClientWriter(null);
                    } else if (ChatMessage.ROLE_SUPPORTER.equals(role)) {
                        session.setSupporterWriter(null);
                    }
                    cleanupSession(issueId);
                }
            }
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            System.out.println("[" + issueId + "] " + role + " '" + userName + "' desconectado.");
        }
    }
}
