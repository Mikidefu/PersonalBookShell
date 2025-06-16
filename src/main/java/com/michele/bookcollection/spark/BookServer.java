package com.michele.bookcollection.spark;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.LibroDTO;
import com.michele.bookcollection.model.StatoLettura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookServer {

    private static final String DB_URL      = "jdbc:postgresql://localhost:5432/DatabaseLibri";
    private static final String DB_USER     = "postgres";
    private static final String DB_PASSWORD = "Miky1234";

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        port(8080);

        // 🔌 Registra WebSocket
        webSocket("/ws", BookWebSocketHandler.class);
        init();

        // CORS completo per tutti i metodi
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });
        options("/*", (req, res) -> "OK");

        get("/ping", (req, res) -> "pong");

        // POST /books - crea libro
        post("/books", (request, response) -> {
            System.out.println("POST /books ricevuto, body: " + request.body());
            response.type("application/json");

            // 1. Parsing JSON in DTO
            LibroDTO dto;
            try {
                dto = gson.fromJson(request.body(), LibroDTO.class);
            } catch (JsonSyntaxException e) {
                response.status(400);
                return gson.toJson(new ApiResponse("bad_request", "JSON non valido"));
            }

            // 2. Validazione minima
            if (dto.getTitolo() == null || dto.getIsbn() == null) {
                response.status(400);
                return gson.toJson(new ApiResponse("bad_request", "Titolo o ISBN mancanti"));
            }

            // 3. Conversione in dominio
            Libro libro = fromDTO(dto);

            // 4. Controlli
            if (libro.getValutazione() < 0) libro.setValutazione(0);
            if (libro.getStatoLettura() == null) libro.setStatoLettura(StatoLettura.DA_LEGGERE);
            if (libro.getAutori() == null) libro.setAutori(new ArrayList<>());
            if (libro.getGeneri() == null) libro.setGeneri(new ArrayList<>());

            // 5. Salvataggio
            boolean saved = saveToDatabase(libro);
            if (saved) {
                // 6. Notifica WebSocket con DTO
                BookWebSocketHandler.notifyAllClients(gson.toJson(toDTO(libro)));
                response.status(201);
                return gson.toJson(new ApiResponse("saved", null));
            } else {
                response.status(500);
                return gson.toJson(new ApiResponse("error", "Errore interno salvataggio"));
            }
        });

        // GET /books - restituisce lista di tutti i libri come DTO
        get("/books", (req, res) -> {
            res.type("application/json");
            List<Libro> libri = getAllBooks();
            List<LibroDTO> dtos = libri.stream()
                    .map(BookServer::toDTO)
                    .collect(Collectors.toList());
            return gson.toJson(dtos);
        });

        // GET /books/:isbn - restituisce un libro per isbn come DTO
        get("/books/:isbn", (req, res) -> {
            res.type("application/json");
            Libro libro = getBookByISBN(req.params("isbn"));
            if (libro == null) {
                res.status(404);
                return gson.toJson(new ApiResponse("not_found", "Libro non trovato"));
            }
            return gson.toJson(toDTO(libro));
        });

        // DELETE /books/:isbn - cancella un libro per isbn
        delete("/books/:isbn", (req, res) -> {
            res.type("application/json");
            String isbn = req.params("isbn");
            boolean deleted = deleteBookByISBN(isbn);
            if (deleted) {
                return gson.toJson(new ApiResponse("deleted", null));
            } else {
                res.status(404);
                return gson.toJson(new ApiResponse("not_found", "Libro non trovato o errore cancellazione"));
            }
        });
        webSocket("/ws", BookWebSocketHandler.class);
        init();
    }

    // ————————————————
    // Metodi di persistenza
    // ————————————————

    private static boolean saveToDatabase(Libro libro) {
        String sql = "INSERT INTO libri (isbn, titolo, autori, generi, valutazione, stato_lettura) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (isbn) DO UPDATE SET titolo = EXCLUDED.titolo, autori = EXCLUDED.autori, " +
                "generi = EXCLUDED.generi, valutazione = EXCLUDED.valutazione, stato_lettura = EXCLUDED.stato_lettura";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libro.getISBN());
            stmt.setString(2, libro.getTitolo());
            stmt.setArray(3, conn.createArrayOf("text", libro.getAutori().toArray()));
            stmt.setArray(4, conn.createArrayOf("text", libro.getGeneri().toArray()));
            stmt.setInt(5, libro.getValutazione());
            stmt.setString(6, libro.getStatoLettura().name());

            stmt.executeUpdate();
            System.out.println("Salvataggio su DB completato per isbn: " + libro.getISBN());
            BookWebSocketHandler.broadcastRefresh();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static List<Libro> getAllBooks() {
        List<Libro> libri = new ArrayList<>();
        String sql = "SELECT isbn, titolo, autori, generi, valutazione, stato_lettura FROM libri";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                List<String> autori = rs.getArray("autori") != null
                        ? List.of((String[]) rs.getArray("autori").getArray())
                        : new ArrayList<>();
                List<String> generi = rs.getArray("generi") != null
                        ? List.of((String[]) rs.getArray("generi").getArray())
                        : new ArrayList<>();
                libri.add(new Libro(
                        rs.getString("titolo"),
                        autori,
                        rs.getString("isbn"),
                        generi,
                        rs.getInt("valutazione"),
                        StatoLettura.valueOf(rs.getString("stato_lettura"))
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return libri;
    }

    private static Libro getBookByISBN(String isbn) {
        String sql = "SELECT isbn, titolo, autori, generi, valutazione, stato_lettura FROM libri WHERE isbn = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    List<String> autori = rs.getArray("autori") != null
                            ? List.of((String[]) rs.getArray("autori").getArray())
                            : new ArrayList<>();
                    List<String> generi = rs.getArray("generi") != null
                            ? List.of((String[]) rs.getArray("generi").getArray())
                            : new ArrayList<>();
                    return new Libro(
                            rs.getString("titolo"),
                            autori,
                            rs.getString("isbn"),
                            generi,
                            rs.getInt("valutazione"),
                            StatoLettura.valueOf(rs.getString("stato_lettura"))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean deleteBookByISBN(String isbn) {
        String sql = "DELETE FROM libri WHERE isbn = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ————————————————
    // Conversioni dominio ↔ DTO
    // ————————————————

    private static LibroDTO toDTO(Libro libro) {
        if (libro == null) return null;
        return new LibroDTO(
                libro.getTitolo(),
                libro.getAutori(),
                libro.getISBN(),
                libro.getGeneri(),
                libro.getValutazione(),
                libro.getStatoLettura().name()
        );
    }

    private static Libro fromDTO(LibroDTO dto) {
        if (dto == null) return null;
        return new Libro(
                dto.getTitolo(),
                dto.getAutori(),
                dto.getIsbn(),
                dto.getGeneri(),
                dto.getValutazione(),
                LibroDTO.getStatoLetturaEnum(dto.getStatoLettura())
        );
    }

    // Classe per risposta JSON semplice
    static class ApiResponse {
        String status;
        String error;
        ApiResponse(String status, String error) {
            this.status = status;
            this.error = error;
        }
    }
}
