package com.michele.bookcollection.gui;

import com.michele.bookcollection.model.StatoLettura;
import com.michele.bookcollection.service.LibroService;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Map;

public class StatisticheViewController {

    @FXML private Label lblTotale;
    @FXML private Label lblValMedia;

    @FXML private TableView<GenereCount> tblGenere;
    @FXML private TableColumn<GenereCount, String> colGenere;
    @FXML private TableColumn<GenereCount, Long> colCountGenere;

    @FXML private TableView<StatoCount> tblStato;
    @FXML private TableColumn<StatoCount, String> colStato;
    @FXML private TableColumn<StatoCount, Long> colCountStato;

    // NUOVI campi per Autore
    @FXML private TableView<AutoreCount> tblAutore;
    @FXML private TableColumn<AutoreCount, String> colAutoreStat;
    @FXML private TableColumn<AutoreCount, Long> colCountAutore;

    @FXML private BarChart<String, Number> barChartValutazioni;

    private LibroService libroService;

    public void setLibroService(LibroService service) {
        this.libroService = service;
    }

    @FXML
    public void initialize() {
        // colonne per Genere
        colGenere.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getGenere())
        );
        colCountGenere.setCellValueFactory(cell ->
                new SimpleLongProperty(cell.getValue().getCount()).asObject()
        );

        // colonne per Stato
        colStato.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStato().toString())
        );
        colCountStato.setCellValueFactory(cell ->
                new SimpleLongProperty(cell.getValue().getCount()).asObject()
        );

        // colonne per Autore
        colAutoreStat.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAutore())
        );
        colCountAutore.setCellValueFactory(cell ->
                new SimpleLongProperty(cell.getValue().getCount()).asObject()
        );
    }

    /**
     * Popola tutte le parti di UI con i dati di riepilogo,
     * includendo ora anche "Libri per Autore".
     */
    public void loadStatistics() {
        // 1) Totale libri e valutazione media
        int totale = libroService.getTotaleLibri();
        double valMedia = libroService.getValutazioneMedia();
        lblTotale.setText("Totale libri: " + totale);
        lblValMedia.setText(String.format("Valutazione media: %.2f", valMedia));

        // 2) Libri per Genere
        Map<String, Long> mapGenere = libroService.getConteggioPerGenere();
        ObservableList<GenereCount> listaGeneri = FXCollections.observableArrayList();
        mapGenere.forEach((g, cnt) -> listaGeneri.add(new GenereCount(g, cnt)));
        tblGenere.setItems(listaGeneri);

        // 3) Libri per Stato
        Map<StatoLettura, Long> mapStato = libroService.getConteggioPerStato();
        ObservableList<StatoCount> listaStati = FXCollections.observableArrayList();
        mapStato.forEach((st, cnt) -> listaStati.add(new StatoCount(st, cnt)));
        tblStato.setItems(listaStati);

        // 4) NUOVA TABELLA: Libri per Autore
        Map<String, Long> mapAutore = libroService.getConteggioPerAutore();
        ObservableList<AutoreCount> listaAutori = FXCollections.observableArrayList();
        mapAutore.forEach((a, cnt) -> listaAutori.add(new AutoreCount(a, cnt)));
        tblAutore.setItems(listaAutori);

        // 5) Distribuzione valutazioni (BarChart)
        Map<Integer, Long> mapVal = libroService.getConteggioPerValutazione();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int v = 1; v <= 5; v++) {
            long count = mapVal.getOrDefault(v, 0L);
            series.getData().add(new XYChart.Data<>(String.valueOf(v), count));
        }
        barChartValutazioni.getData().clear();
        barChartValutazioni.getData().add(series);
    }

    // Classi helper per riempire le TableView
    public static class GenereCount {
        private final String genere;
        private final long count;
        public GenereCount(String genere, long count) {
            this.genere = genere;
            this.count = count;
        }
        public String getGenere() { return genere; }
        public long getCount() { return count; }
    }

    public static class StatoCount {
        private final StatoLettura stato;
        private final long count;
        public StatoCount(StatoLettura stato, long count) {
            this.stato = stato;
            this.count = count;
        }
        public StatoLettura getStato() { return stato; }
        public long getCount() { return count; }
    }

    // NUOVA classe helper per Autore
    public static class AutoreCount {
        private final String autore;
        private final long count;
        public AutoreCount(String autore, long count) {
            this.autore = autore;
            this.count = count;
        }
        public String getAutore() { return autore; }
        public long getCount() { return count; }
    }
}
