package com.michele.bookcollection.builder;

import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;
import java.util.List;

public class ConcreteLibroBuilder implements BuilderIF {
    private String titolo;
    private List<String> autori;
    private String isbn;
    private List<String> generi;
    private int valutazione;
    private StatoLettura statoLettura;

    @Override
    public void reset() {
        this.titolo = null;
        this.autori = null;
        this.isbn = null;
        this.generi = null;
        this.valutazione = 0;
        this.statoLettura = StatoLettura.DA_LEGGERE;
    }

    @Override
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    @Override
    public void setAutori(List<String> autori) {
        this.autori = autori;
    }

    @Override
    public void setISBN(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public void setGeneri(List<String> generi) {
        this.generi = generi;
    }

    @Override
    public void setValutazione(int valutazione) {
        this.valutazione = valutazione;
    }

    @Override
    public void setStatoLettura(StatoLettura stato) {
        this.statoLettura = stato;
    }

    @Override
    public Libro getLibro() {
        Libro libro = new Libro(titolo, autori, isbn, generi, valutazione, statoLettura);
        reset();
        System.out.println("Ho creato/modificato un libro tramite Builder");
        return libro;
    }
}
