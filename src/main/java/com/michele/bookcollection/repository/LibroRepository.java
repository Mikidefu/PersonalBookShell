package com.michele.bookcollection.repository;

import com.michele.bookcollection.model.Libro;

import java.util.List;

public interface LibroRepository {
    void salvaLibro(Libro libro);
    List<Libro> getTuttiLibri();
    void aggiornaLibro(Libro libro);
    void eliminaLibro(String isbn);
}
