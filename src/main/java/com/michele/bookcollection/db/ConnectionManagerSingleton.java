package com.michele.bookcollection.db;

import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionCreator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton per gestire un’unica fonte di connessioni al DB.
 */
public enum ConnectionManagerSingleton {
    INSTANCE;

    // Parametri di connessione: personalizzali o leggili da properties
    private static final String URL      = "jdbc:postgresql://localhost:5432/DatabaseLibri";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "Miky1234";

    private ConnectionManagerSingleton() {
    }

    /**
     * Restituisce una nuova Connection.
     * @throws SQLException se non è possibile aprire la connessione.
     */
    public synchronized Connection getConnection() throws SQLException {
        System.out.println("Chiamata Connessione Singleton");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
