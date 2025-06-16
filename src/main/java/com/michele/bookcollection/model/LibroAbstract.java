package com.michele.bookcollection.model;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class LibroAbstract implements LibroIF {
    protected String titolo;
    protected List<String> autori;
    protected String isbn;
    protected List<String> generi;
    protected int valutazione;
    protected StatoLettura statoLettura;

    @Override public String getTitolo() { return titolo; }
    @Override public List<String> getAutori() { return autori; }
    @Override public String getISBN() { return isbn; }
    @Override public List<String> getGeneri() { return generi; }
    @Override public int getValutazione() { return valutazione; }
    @Override public StatoLettura getStatoLettura() { return statoLettura; }

    public void setTitolo(String titolo) { this.titolo = titolo; }
    public void setAutori(List<String> autori) { this.autori = autori; }
    public void setISBN(String isbn) { this.isbn = isbn; }
    public void setGeneri(List<String> generi) { this.generi = generi; }
    public void setValutazione(int valutazione) { this.valutazione = valutazione; }
    public void setStatoLettura(StatoLettura statoLettura) { this.statoLettura = statoLettura; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LibroAbstract)) return false;
        LibroAbstract that = (LibroAbstract) o;
        return Objects.equals(isbn, that.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isbn);
    }

    @Override
    public String toString() {
        return "LibroAbstract{" +
                "titolo='" + titolo + '\'' +
                ", autori=" + autori.toString() +
                ", isbn='" + isbn + '\'' +
                ", generi=" + generi.toString() +
                ", valutazione=" + valutazione +
                ", statoLettura=" + statoLettura.toString() +
                '}';
    }
}

