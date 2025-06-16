package com.michele.bookcollection.spark;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.*;

@WebSocket
public class BookWebSocketHandler {

    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    // Scheduler per inviare ping periodici
    private static final ScheduledExecutorService pingScheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        // Ogni 30 secondi, invia ping a tutte le sessioni aperte, per non spegnere il WebSocket
        pingScheduler.scheduleAtFixedRate(() -> {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getRemote().sendPing(ByteBuffer.wrap(new byte[0]));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessions.add(session);
        System.out.println("WebSocket connesso: " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        sessions.remove(session);
        System.out.println("WebSocket disconnesso: " + reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        // Al momento, ignoro i messaggi in entrata
    }

    public static void notifyAllClients(String jsonLibro) {
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getRemote().sendString(jsonLibro);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Per refreshare la lista pedissequamente all'aggiunta di un libro da app mobile.
    public static void broadcastRefresh() {
        sessions.forEach(s -> {
            try {
                s.getRemote().sendString("refresh");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
