package com.michele.bookcollection.controller;

import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;
import com.michele.bookcollection.service.LibroService;

import java.util.List;

public class LibroController {
    private final LibroService service;

    public LibroController(LibroService service) {
        this.service = service;
    }

    /**
     * Gestisce l'aggiunta di un libro con più autori e generi
     */
    public void gestisciAggiuntaLibro(String titolo, List<String> autori, String isbn, List<String> generi, int valutazione, StatoLettura statoLettura) {
        Libro libro = new Libro(titolo, autori, isbn, generi, valutazione, statoLettura);
        service.aggiungiLibro(libro);
    }

    public List<Libro> mostraLibri() {
        return service.getLibri();
    }
}
