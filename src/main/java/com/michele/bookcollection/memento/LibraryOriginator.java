package com.michele.bookcollection.memento;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.service.LibroService;

public class LibraryOriginator {
    private final LibroService libroService;
    private List<Libro> currentState;

    public LibraryOriginator(LibroService service) {
        this.libroService = service;
        this.currentState = new ArrayList<>();
    }

    public LibraryMemento save() {
        return new LibraryMemento(currentState);
    }

    public void restore(LibraryMemento m) {
        List<Libro> previousState = m.getState();

        // 1) rimuovo dal DB i libri non più presenti
        List<String> isbnsPrevious = previousState.stream()
                .map(Libro::getISBN).toList();
        libroService.getLibri().stream()
                .filter(l -> !isbnsPrevious.contains(l.getISBN()))
                .forEach(l -> libroService.rimuoviLibro(l.getISBN()));

        // 2) aggiungo al DB i libri tornati “nuovi”
        List<String> isbnsInDb = libroService.getLibri().stream()
                .map(Libro::getISBN).toList();
        previousState.stream()
                .filter(l -> !isbnsInDb.contains(l.getISBN()))
                .forEach(l -> libroService.aggiungiLibro(l));

        // 3) aggiorno stato in memoria
        this.currentState = previousState;
    }

    // usato dal Caretaker per tenere aggiornato lo snapshot in memoria
    public void setState(List<Libro> corrente) {
        this.currentState = corrente;
    }

    public List<Libro> getState() {
        return currentState;
    }
}
