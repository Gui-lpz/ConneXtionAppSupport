package chat.listener;

import chat.model.ChatServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/* Arranca y detiene el ChatServer automáticamente con Tomcat.
 
  Al deployer el war, Tomcat llama contextInitialized() y el ChatServer
  empieza a escuchar en el puerto 9500 en su propio hilo daemon.
  Al undeployar, contextDestroyed() detiene el hilo limpiamente.*/

@WebListener
public class ChatServerLauncher implements ServletContextListener {

    private Thread chatThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        chatThread = new Thread(() -> {
            try {
                ChatServer.main(new String[]{});
            } catch (Exception e) {
                System.err.println("ChatServer falló al iniciar: " + e.getMessage());
            }
        }, "ChatServer-Main");

        // Daemon: si Tomcat se detiene, este hilo muere con él
        chatThread.setDaemon(true);
        chatThread.start();

        System.out.println("ChatServer iniciado en puerto " + ChatServer.PORT);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (chatThread != null && chatThread.isAlive()) {
            chatThread.interrupt();
        }
        System.out.println("ChatServer detenido.");
    }
}