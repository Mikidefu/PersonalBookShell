package com.michele.bookcollection.repository;

import com.michele.bookcollection.db.ConnectionManagerSingleton;
import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroRepositoryPostgres implements LibroRepository {

    @Override
    public void salvaLibro(Libro libro) {
        String sql = "INSERT INTO libri (titolo, autori, isbn, generi, valutazione, stato_lettura) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = ConnectionManagerSingleton.INSTANCE.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, libro.getTitolo());
            Array autoriArray = c.createArrayOf("text", libro.getAutori().toArray(new String[0]));
            ps.setArray(2, autoriArray);
            ps.setString(3, libro.getISBN());
            Array generiArray = c.createArrayOf("text", libro.getGeneri().toArray(new String[0]));
            ps.setArray(4, generiArray);
            ps.setInt(5, libro.getValutazione());
            ps.setString(6, libro.getStatoLettura().name());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Libro> getTuttiLibri() {
        List<Libro> list = new ArrayList<>();
        String sql = "SELECT * FROM libri";
        try (Connection c = ConnectionManagerSingleton.INSTANCE.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Array autoriSql = rs.getArray("autori");
                List<String> autori = List.of((String[]) autoriSql.getArray());
                Array generiSql = rs.getArray("generi");
                List<String> generi = List.of((String[]) generiSql.getArray());

                Libro l = new Libro(
                        rs.getString("titolo"),
                        autori,
                        rs.getString("isbn"),
                        generi,
                        rs.getInt("valutazione"),
                        StatoLettura.valueOf(rs.getString("stato_lettura"))
                );
                list.add(l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void aggiornaLibro(Libro libro) {
        String sql = "UPDATE libri SET titolo = ?, autori = ?, generi = ?, valutazione = ?, stato_lettura = ? WHERE isbn = ?";
        try (Connection c = ConnectionManagerSingleton.INSTANCE.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, libro.getTitolo());
            Array autoriArray = c.createArrayOf("text", libro.getAutori().toArray(new String[0]));
            ps.setArray(2, autoriArray);
            Array generiArray = c.createArrayOf("text", libro.getGeneri().toArray(new String[0]));
            ps.setArray(3, generiArray);
            ps.setInt(4, libro.getValutazione());
            ps.setString(5, libro.getStatoLettura().name());
            ps.setString(6, libro.getISBN());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void eliminaLibro(String isbn) {
        String sql = "DELETE FROM libri WHERE isbn = ?";
        try (Connection c = ConnectionManagerSingleton.INSTANCE.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, isbn);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
