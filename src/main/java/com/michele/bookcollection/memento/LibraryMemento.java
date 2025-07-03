package com.michele.bookcollection.memento;

import java.util.List;
import com.michele.bookcollection.model.Libro;

/**
 * Incapsula uno snapshot immutabile della lista di libri.
 */
public class LibraryMemento {
    private final List<Libro> state;

    public LibraryMemento(List<Libro> state) {
        // Salva una deep copy della lista usando il copy constructor di Libro
        this.state = state.stream()
                .map(Libro::new)  // usa il costruttore di copia
                .toList();
    }

    public List<Libro> getState() {
        // Restituisce un’altra copia per garantire immutabilità
        return state.stream()
                .map(Libro::new)
                .toList();
    }
}
