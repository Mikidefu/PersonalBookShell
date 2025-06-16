package com.michele.bookcollection.gui;

import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;
import com.michele.bookcollection.service.LibroService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class LibroFormController {

    @FXML private TextField titoloField;
    @FXML private TextField isbnField;
    @FXML private FlowPane autoriPane;
    @FXML private TextField nuovoAutoreField;
    @FXML private FlowPane generiPane;
    @FXML private TextField nuovoGenereField;
    @FXML private Spinner<Integer> valutazioneSpinner;
    @FXML private ComboBox<StatoLettura> statoLetturaComboBox;

    private LibroService libroService;
    private Libro libroEsistente;
    private Stage dialogStage;

    private List<String> autoriLista = new ArrayList<>();
    private List<String> generiLista = new ArrayList<>();

    public void setLibroService(LibroService service) {
        this.libroService = service;
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setLibro(Libro libro) {
        this.libroEsistente = libro;
        titoloField.setText(libro.getTitolo());
        isbnField.setText(libro.getISBN());
        autoriLista.addAll(libro.getAutori());
        generiLista.addAll(libro.getGeneri());
        autoriLista.forEach(this::creaTagAutore);
        generiLista.forEach(this::creaTagGenere);
        isbnField.setDisable(true);
        valutazioneSpinner.getValueFactory().setValue(libro.getValutazione());
        statoLetturaComboBox.setValue(libro.getStatoLettura());
    }

    @FXML
    public void initialize() {
        valutazioneSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3)
        );
        statoLetturaComboBox.setItems(
                FXCollections.observableArrayList(StatoLettura.values())
        );
    }

    @FXML
    private void onAggiungiAutore() {
        String a = nuovoAutoreField.getText().trim();
        if (!a.isEmpty() && !autoriLista.contains(a)) {
            autoriLista.add(a);
            creaTagAutore(a);
            nuovoAutoreField.clear();
        }
    }

    private void creaTagAutore(String a) {
        Label tag = new Label(a);
        tag.getStyleClass().add("tag-label");
        Button close = new Button("✕");
        close.getStyleClass().add("tag-close");
        HBox pill = new HBox(tag, close);
        pill.getStyleClass().add("tag-pill");
        close.setOnAction(e -> {
            autoriLista.remove(a);
            autoriPane.getChildren().remove(pill);
        });
        autoriPane.getChildren().add(pill);
    }

    @FXML
    private void onAggiungiGenere() {
        String g = nuovoGenereField.getText().trim();
        if (!g.isEmpty() && !generiLista.contains(g)) {
            generiLista.add(g);
            creaTagGenere(g);
            nuovoGenereField.clear();
        }
    }

    private void creaTagGenere(String g) {
        Label tag = new Label(g);
        tag.getStyleClass().add("tag-label");
        Button close = new Button("✕");
        close.getStyleClass().add("tag-close");
        HBox pill = new HBox(tag, close);
        pill.getStyleClass().add("tag-pill");
        close.setOnAction(e -> {
            generiLista.remove(g);
            generiPane.getChildren().remove(pill);
        });
        generiPane.getChildren().add(pill);
    }

    @FXML
    private void handleSalva() {
        if (titoloField.getText().isEmpty()) {
            showAlert(AlertType.ERROR, "Il titolo è obbligatorio.");
            return;
        }
        if (isbnField.getText().isEmpty()) {
            showAlert(AlertType.ERROR, "L'ISBN è obbligatorio.");
            return;
        }
        if (autoriLista.isEmpty()) {
            showAlert(AlertType.ERROR, "Inserisci almeno un autore.");
            return;
        }
        if (generiLista.isEmpty()) {
            showAlert(AlertType.ERROR, "Inserisci almeno un genere.");
            return;
        }
        if (statoLetturaComboBox.getValue() == null) {
            showAlert(AlertType.ERROR, "Seleziona lo stato di lettura.");
            return;
        }

        try {
            if (libroEsistente == null) {
                libroService.aggiungiLibro(new Libro(
                        titoloField.getText(), autoriLista,
                        isbnField.getText(), generiLista,
                        valutazioneSpinner.getValue(),
                        statoLetturaComboBox.getValue()
                ));
            } else {
                libroEsistente.setTitolo(titoloField.getText());
                libroEsistente.setISBN(isbnField.getText());
                libroEsistente.setAutori(autoriLista);
                libroEsistente.setGeneri(generiLista);
                libroEsistente.setValutazione(valutazioneSpinner.getValue());
                libroEsistente.setStatoLettura(statoLetturaComboBox.getValue());
                libroService.modificaLibro(libroEsistente);
            }
            dialogStage.close();
        } catch (IllegalArgumentException e) {
            showAlert(AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void handleAnnulla() {
        dialogStage.close();
    }

    private void showAlert(AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(type == AlertType.ERROR ? "Errore" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
