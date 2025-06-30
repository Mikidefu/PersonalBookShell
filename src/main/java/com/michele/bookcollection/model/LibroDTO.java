package com.michele.bookcollection.model;

import java.util.List;

// Qui NON usiamo JavaFX Property: solo tipi “plain” (String, int, List<String>, enum)
public class LibroDTO implements LibroIF {

    private String titolo;
    private List<String> autori;
    private String isbn;
    private List<String> generi;
    private int valutazione;
    private static String statoLettura;

    public LibroDTO(String titolo,
                    List<String> autori,
                    String isbn,
                    List<String> generi,
                    int valutazione,
                    String statoLettura) {
        this.titolo = titolo;
        this.autori = autori;
        this.isbn = isbn;
        this.generi = generi;
        this.valutazione = valutazione;
        this.statoLettura = statoLettura;
    }

    public LibroDTO() {};

    public String getTitolo() {
        return titolo;
    }
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public List<String> getAutori() {
        return autori;
    }

    @Override
    public String getISBN() {
        return isbn;
    }

    public void setAutori(List<String> autori) {
        this.autori = autori;
    }


    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public List<String> getGeneri() {
        return generi;
    }
    public void setGeneri(List<String> generi) {
        this.generi = generi;
    }

    public int getValutazione() {
        return valutazione;
    }
    public void setValutazione(int valutazione) {
        this.valutazione = valutazione;
    }

    public String getStatoLetturaString() {
        return statoLettura;
    }

    public void setStatoLettura(String statoLettura) {
        this.statoLettura = statoLettura;
    }

    public StatoLettura getStatoLettura() {

        if (statoLettura == null) return StatoLettura.DA_LEGGERE;
        try {
            return StatoLettura.valueOf(statoLettura.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
