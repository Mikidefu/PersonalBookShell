package com.michele.bookcollection.spark;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.michele.bookcollection.db.ConnectionManagerSingleton;
import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.LibroDTO;
import com.michele.bookcollection.model.StatoLettura;
import com.michele.bookcollection.assembler.LibroAssembler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookServer {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        port(8080);

        // Registrazione WebSocket
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
            response.type("application/json");

            // 1) Deserializzo in DTO
            LibroDTO dto;
            try {
                dto = gson.fromJson(request.body(), LibroDTO.class);
            } catch (JsonSyntaxException e) {
                response.status(400);
                return gson.toJson(new ApiResponse("bad_request", "JSON non valido"));
            }

            // 2) Validazioni
            if (dto.getTitolo() == null || dto.getISBN() == null) {
                response.status(400);
                return gson.toJson(new ApiResponse("bad_request", "Titolo o ISBN mancanti"));
            }

            // 3) Creo l'oggetto dominio dal DTO
            Libro libro = LibroAssembler.createDomain(dto);

            // 4) Default e correzioni
            if (libro.getValutazione() < 0) libro.setValutazione(0);
            if (libro.getStatoLettura() == null) libro.setStatoLettura(StatoLettura.DA_LEGGERE);
            if (libro.getAutori() == null) libro.setAutori(new ArrayList<>());
            if (libro.getGeneri() == null) libro.setGeneri(new ArrayList<>());

            // 5) Salvataggio (INSERT o UPDATE via ON CONFLICT)
            boolean saved = saveToDatabase(libro);
            if (saved) {
                // 6) Notifico tutti i client via WS inviando **DTO** aggiornato
                LibroDTO outDto = LibroAssembler.createDTO(libro);
                BookWebSocketHandler.notifyAllClients(gson.toJson(outDto));

                response.status(201);
                return gson.toJson(new ApiResponse("saved", null));
            } else {
                response.status(500);
                return gson.toJson(new ApiResponse("error", "Errore interno salvataggio"));
            }
        });

        get("/books", (req, res) -> {
            res.type("application/json");
            List<Libro> libri = getAllBooks();
            // map Domain → DTO
            List<LibroDTO> dtos = libri.stream()
                    .map(LibroAssembler::createDTO)
                    .collect(Collectors.toList());
            return gson.toJson(dtos);
        });

        get("/books/:isbn", (req, res) -> {
            res.type("application/json");
            Libro libro = getBookByISBN(req.params("isbn"));
            if (libro == null) {
                res.status(404);
                return gson.toJson(new ApiResponse("not_found", "Libro non trovato"));
            }
            LibroDTO dto = LibroAssembler.createDTO(libro);
            return gson.toJson(dto);
        });

        delete("/books/:isbn", (req, res) -> {
            res.type("application/json");
            boolean deleted = deleteBookByISBN(req.params("isbn"));
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

        try (Connection conn = ConnectionManagerSingleton.INSTANCE.getConnection();
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
        try (Connection conn = ConnectionManagerSingleton.INSTANCE.getConnection();
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
        try (Connection conn = ConnectionManagerSingleton.INSTANCE.getConnection();
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
        try (Connection conn = ConnectionManagerSingleton.INSTANCE.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
