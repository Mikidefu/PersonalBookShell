package com.michele.bookcollection.gui;

import com.michele.bookcollection.builder.ConcreteLibroBuilder;
import com.michele.bookcollection.builder.BuilderIF;
import com.michele.bookcollection.builder.LibroDirector;
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

    // Builder & Director
    private final BuilderIF builder = new ConcreteLibroBuilder();
    private final LibroDirector director = new LibroDirector(builder);

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
        // validazioni
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
                // usa il director per costruire il nuovo libro
                Libro nuovo = director.build(
                        titoloField.getText(),
                        new ArrayList<>(autoriLista),
                        isbnField.getText(),
                        new ArrayList<>(generiLista),
                        valutazioneSpinner.getValue(),
                        statoLetturaComboBox.getValue()
                );
                libroService.aggiungiLibro(nuovo);

            } else {
                // modifica l'esistente: ricostruisci via builder e assegna
                Libro modificato = director.build(
                        titoloField.getText(),
                        new ArrayList<>(autoriLista),
                        libroEsistente.getISBN(), // ISBN non modificabile
                        new ArrayList<>(generiLista),
                        valutazioneSpinner.getValue(),
                        statoLetturaComboBox.getValue()
                );
                libroService.modificaLibro(modificato);
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
