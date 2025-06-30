package com.michele.bookcollection.builder;

import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;
import java.util.List;

public class LibroDirector {
    private final BuilderIF builder;

    public LibroDirector(BuilderIF builder) {
        this.builder = builder;
    }

    public Libro build(String titolo,
                       List<String> autori,
                       String isbn,
                       List<String> generi,
                       int valutazione,
                       StatoLettura stato) {
        builder.reset();
        builder.setTitolo(titolo);
        builder.setAutori(autori);
        builder.setISBN(isbn);
        builder.setGeneri(generi);
        builder.setValutazione(valutazione);
        builder.setStatoLettura(stato);
        return builder.getLibro();
    }
}
