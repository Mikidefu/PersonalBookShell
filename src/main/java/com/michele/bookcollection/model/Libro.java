package com.michele.bookcollection.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class Libro extends LibroAbstract {
    @Expose private final StringProperty titolo = new SimpleStringProperty();
    @Expose private final ListProperty<String> autori = new SimpleListProperty<>(FXCollections.observableArrayList());
    @Expose private final StringProperty isbn = new SimpleStringProperty();
    @Expose private final ListProperty<String> generi = new SimpleListProperty<>(FXCollections.observableArrayList());
    @Expose private final IntegerProperty valutazione = new SimpleIntegerProperty();
    @Expose private final ObjectProperty<StatoLettura> statoLettura = new SimpleObjectProperty<>();

    public Libro(String titolo, List<String> autori, String isbn, List<String> generi, int valutazione, StatoLettura statoLettura) {
        super();
        this.titolo.set(titolo);
        this.autori.setAll(autori);
        this.isbn.set(isbn);
        this.generi.setAll(generi);
        this.valutazione.set(valutazione);
        this.statoLettura.set(statoLettura);
    }

    @JsonIgnore public StringProperty titoloProperty() { return titolo; }
    @Override public String getTitolo() { return titolo.get(); }
    public void setTitolo(String titolo) { this.titolo.set(titolo); }

    @JsonIgnore public ListProperty<String> autoriProperty() { return autori; }
    @Override public List<String> getAutori() { return autori.get(); }
    public void setAutori(List<String> autori) { this.autori.setAll(autori); }

    @JsonIgnore public StringProperty isbnProperty() { return isbn; }
    @Override public String getISBN() { return isbn.get(); }
    public void setISBN(String isbn) { this.isbn.set(isbn); }

    @JsonIgnore public ListProperty<String> generiProperty() { return generi; }
    @Override public List<String> getGeneri() { return generi.get(); }
    public void setGeneri(List<String> generi) { this.generi.setAll(generi); }

    @JsonIgnore public IntegerProperty valutazioneProperty() { return valutazione; }
    @Override public int getValutazione() { return valutazione.get(); }
    public void setValutazione(int valutazione) { this.valutazione.set(valutazione); }

    @JsonIgnore public ObjectProperty<StatoLettura> statoLetturaProperty() { return statoLettura; }
    @Override public StatoLettura getStatoLettura() { return statoLettura.get(); }
    public void setStatoLettura(StatoLettura statoLettura) { this.statoLettura.set(statoLettura); }
}
