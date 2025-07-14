package com.michele.bookcollection.gui;

import com.michele.bookcollection.factory.PostgresRepositoryFactory;
import com.michele.bookcollection.factory.RepositoryFactory;
import com.michele.bookcollection.memento.LibraryCaretaker;
import com.michele.bookcollection.memento.LibraryOriginator;
import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import com.michele.bookcollection.service.LibroService;
import com.michele.bookcollection.service.strategy.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.websocket.*;


import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MainViewController {

    @FXML private BorderPane rootPane;
    @FXML private TableView<Libro> tabellaLibri;
    @FXML private TableColumn<Libro, String> colTitolo;
    @FXML private TableColumn<Libro, String> colAutore;
    @FXML private TableColumn<Libro, String> colISBN;
    @FXML private TableColumn<Libro, String> colGeneri;
    @FXML private TableColumn<Libro, Integer> colValutazione;
    @FXML private TableColumn<Libro, String> colStatoLettura;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterGenere;
    @FXML private ComboBox<String> filterStatoLettura;
    @FXML private Slider filterValutazioneMin;
    @FXML private ComboBox<OrdinamentoStrategy> sortStrategyCombo;
    @FXML private Button undoButton;
    @FXML private Button redoButton;




    //PER BACKUP SCHEDULATO
    private ScheduledExecutorService scheduler;

    private Session webSocketSession;
    private static final String WS_URI = "ws://localhost:8080/ws";


    private LibroService libroService;

    private LibraryOriginator originator;
    private LibraryCaretaker caretaker;
    private final IntegerProperty historyVersion = new SimpleIntegerProperty(0);


    private ObservableList<Libro> libriData;
    private FilteredList<Libro> filteredData;
    private SortedList<Libro> sortedData;

    @FXML
    public void initialize() {
        RepositoryFactory factory = new PostgresRepositoryFactory();
        libroService = new LibroService(factory.operation());

        originator = new LibraryOriginator(libroService);
        caretaker = new LibraryCaretaker(originator);

        // 1) Carico i dati in un ObservableList
        libriData = FXCollections.observableArrayList(libroService.getLibri());

        // 2) Creo il FilteredList (predicate iniziale = sempre true)
        filteredData = new FilteredList<>(libriData, libro -> true);

        // 3) Creo il SortedList basato sul FilteredList
        sortedData = new SortedList<>(filteredData);

        // 4) Lego il comparatore della TableView a quello del SortedList
        sortedData.comparatorProperty().bind(tabellaLibri.comparatorProperty());

        // 5) Associo il SortedList alla TableView
        tabellaLibri.setItems(sortedData);


        // ** primo snapshot **
        originator.setState(List.copyOf(libriData));
        caretaker.save();
        undoButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !caretaker.canUndo(),
                        historyVersion
                )
        );
        redoButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !caretaker.canRedo(),
                        historyVersion
                )
        );


        // ---------- Configurazione colonne ----------
        colTitolo.setCellValueFactory(c -> c.getValue().titoloProperty());
        colAutore.setCellValueFactory(c -> new SimpleStringProperty(
                String.join(", ", c.getValue().getAutori())
        ));
        colISBN.setCellValueFactory(c -> c.getValue().isbnProperty());
        colGeneri.setCellValueFactory(c -> new SimpleStringProperty(
                String.join(", ", c.getValue().getGeneri())
        ));
        colValutazione.setCellValueFactory(c -> c.getValue().valutazioneProperty().asObject());
        colStatoLettura.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatoLettura().toString()
        ));

        // Comparatori custom
        colTitolo.setComparator(String.CASE_INSENSITIVE_ORDER);
        colISBN.setComparator(String.CASE_INSENSITIVE_ORDER);
        colGeneri.setComparator((g1, g2) -> {
            String p1 = g1.split(",")[0].trim().toLowerCase();
            String p2 = g2.split(",")[0].trim().toLowerCase();
            return p1.compareTo(p2);
        });
        colValutazione.setComparator(Integer::compare);
        colStatoLettura.setComparator((s1, s2) -> {
            var o1 = StatoLettura.valueOf(s1);
            var o2 = StatoLettura.valueOf(s2);
            return Integer.compare(o1.ordinal(), o2.ordinal());
        });

        // ---------- Setup filtri dinamici ----------
        aggiornaComboBoxGeneri();
        filterStatoLettura.setItems(FXCollections.observableArrayList(
                "Tutti",
                StatoLettura.LETTO.toString(),
                StatoLettura.IN_LETTURA.toString(),
                StatoLettura.DA_LEGGERE.toString()
        ));
        filterStatoLettura.getSelectionModel().selectFirst();

        filterValutazioneMin.setMin(0);
        filterValutazioneMin.setMax(5);
        filterValutazioneMin.setValue(0);
        filterValutazioneMin.setMajorTickUnit(1);
        filterValutazioneMin.setSnapToTicks(true);
        filterValutazioneMin.setShowTickLabels(true);
        filterValutazioneMin.setShowTickMarks(false);

        // ad ogni cambio di filtro → aggiorno la tabella
        searchField.textProperty().addListener((obs,o,n)-> aggiornaFiltro());
        filterGenere.valueProperty().addListener((obs,o,n)-> aggiornaFiltro());
        filterStatoLettura.valueProperty().addListener((obs,o,n)-> aggiornaFiltro());
        filterValutazioneMin.valueProperty().addListener((obs,o,n)-> {
            filterValutazioneMin.setValue(n.intValue());
            aggiornaFiltro();
        });

        // ---------- Backup automatico e websocket ----------
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try { libroService.backupJsonAutomatico(); }
            catch (IOException e) { System.err.println("Backup fallito: "+e.getMessage()); }
        }, 15, 15, TimeUnit.MINUTES);
        inizializzaWebSocket();

        // ——— SETUP STRATEGY DI ORDINAMENTO —————
        // 1) Preparo le varie strategie disponibili
        List<OrdinamentoStrategy> strategies = List.of(
                new OrdinamentoPerTitolo(true),
                new OrdinamentoPerTitolo(false),
                new OrdinamentoPerGenere(true),
                new OrdinamentoPerGenere(false),
                new OrdinamentoPerAutore(true),
                new OrdinamentoPerAutore(false),
                new OrdinamentoPerValutazione(true),
                new OrdinamentoPerValutazione(false),
                new OrdinamentoPerStatoLettura(true),
                new OrdinamentoPerStatoLettura(false)
        );
        sortStrategyCombo.setItems(FXCollections.observableArrayList(strategies));

        // 2) Visualizzo nel ComboBox il nome di ogni strategy
        Callback<ListView<OrdinamentoStrategy>, ListCell<OrdinamentoStrategy>> factory2 = lv -> new ListCell<>() {
            @Override
            protected void updateItem(OrdinamentoStrategy s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s==null ? null : s.getNome());
            }
        };
        sortStrategyCombo.setCellFactory(factory2);
        sortStrategyCombo.setButtonCell(factory2.call(null));

        // 3) Seleziono di default la prima
        sortStrategyCombo.getSelectionModel().selectFirst();

        // 4) Quando cambia la strategy, richiamo il service e aggiorno la lista
        sortStrategyCombo.valueProperty().addListener((obs, oldS, newS) -> {
            if (newS!=null) {
                List<Libro> ordinati = libroService.ordina(newS);
                libriData.setAll(ordinati);
                aggiornaFiltro();
            }
        });

        Platform.runLater(() -> {
            rootPane.getScene().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                    onUndo();
                    event.consume();
                } else if (event.isControlDown() && event.getCode() == KeyCode.Y) {
                    onRedo();
                    event.consume();
                }
            });
        });

    }

    @FXML
    private void onUndo() {
        if (caretaker.canUndo()) {
            caretaker.undo();
            List<Libro> previous = originator.getState();
            libriData.setAll(previous);
            aggiornaComboBoxGeneri();
            aggiornaFiltro();
            historyVersion.set(historyVersion.get() + 1);
        } else {
            showInfo("Niente da annullare");
        }
    }

    @FXML
    private void onRedo() {
        if (caretaker.canRedo()) {
            caretaker.redo();
            List<Libro> next = originator.getState();
            libriData.setAll(next);
            aggiornaComboBoxGeneri();
            aggiornaFiltro();
            historyVersion.set(historyVersion.get() + 1);
        } else {
            showInfo("Niente da reintegrare");
        }
    }


    // ------------------------------------------
    // METODI DI MODIFICA/ELIMINA/AGGIUNGI
    // ------------------------------------------

    @FXML
    private void onModifica() {
        Libro selezionato = tabellaLibri.getSelectionModel().getSelectedItem();
        if (selezionato == null) {
            showWarning("Seleziona un libro da modificare.");
            return;
        }


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/LibroForm.fxml"));
            Parent root = loader.load();

            LibroFormController controller = loader.getController();
            Stage stage = setupDialog(root, "Modifica Libro");
            controller.setDialogStage(stage);
            controller.setLibroService(libroService);
            controller.setLibro(selezionato);

            stage.showAndWait();
            refreshLibri();
            originator.setState(List.copyOf(libriData));
            caretaker.save();
            historyVersion.set(historyVersion.get() + 1);

        } catch (IOException e) {
            e.printStackTrace();
            showWarning("Errore nell'apertura della finestra di modifica.");
        }


    }

    @FXML
    private void onElimina() {
        Libro selezionato = tabellaLibri.getSelectionModel().getSelectedItem();
        if (selezionato == null) {
            showWarning("Seleziona un libro da eliminare.");
            return;
        }


        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Conferma eliminazione");
        conferma.setContentText("Vuoi davvero eliminare '" + selezionato.getTitolo() + "'?");
        if (conferma.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
            libroService.rimuoviLibro(selezionato.getISBN());
            refreshLibri();
            originator.setState(List.copyOf(libriData));
            caretaker.save();
            historyVersion.set(historyVersion.get() + 1);
        }
    }

    @FXML
    private void apriFormLibro() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/LibroForm.fxml"));
            Parent root = loader.load();

            LibroFormController controller = loader.getController();
            Stage stage = setupDialog(root, "Nuovo Libro");
            controller.setDialogStage(stage);
            controller.setLibroService(libroService);

            stage.showAndWait();
            refreshLibri();
            originator.setState(List.copyOf(libriData));
            caretaker.save();
            historyVersion.set(historyVersion.get() + 1);

        } catch (IOException e) {
            e.printStackTrace();
            showWarning("Errore nell'apertura della finestra di inserimento.");
        }
    }

    private Stage setupDialog(Parent root, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        Stage mainStage = (Stage) tabellaLibri.getScene().getWindow();
        stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        stage.initOwner(mainStage);
        return stage;
    }

    // ------------------------------------------
    // FILTRO DINAMICO E REFRESH
    // ------------------------------------------

    private void aggiornaFiltro() {
        String testo = (searchField.getText() != null) ? searchField.getText().toLowerCase() : "";
        String genereSelezionato = filterGenere.getValue();
        String statoSelezionato = filterStatoLettura.getValue();
        int valMin = (int) filterValutazioneMin.getValue();

        filteredData.setPredicate(libro -> {
            boolean matchTesto = testo.isEmpty() ||
                    libro.getTitolo().toLowerCase().contains(testo) ||
                    libro.getAutori().stream().anyMatch(a -> a.toLowerCase().contains(testo));

            boolean matchGenere = true;
            if (genereSelezionato != null && !genereSelezionato.equals("Tutti")) {
                matchGenere = libro.getGeneri() != null && libro.getGeneri().contains(genereSelezionato);
            }

            boolean matchStato = true;
            if (statoSelezionato != null && !statoSelezionato.equals("Tutti")) {
                matchStato = libro.getStatoLettura() != null
                        && libro.getStatoLettura().toString().equalsIgnoreCase(statoSelezionato);
            }

            boolean matchValutazione = libro.getValutazione() >= valMin;

            return matchTesto && matchGenere && matchStato && matchValutazione;
        });
    }

    private void refreshLibri() {
        libriData.clear();
        libriData.setAll(libroService.getLibri());
        aggiornaComboBoxGeneri();
        aggiornaFiltro();
    }

    private void aggiornaComboBoxGeneri() {
        List<String> generi = libriData.stream()
                .flatMap(l -> l.getGeneri().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        generi.add(0, "Tutti");
        filterGenere.setItems(FXCollections.observableArrayList(generi));
        filterGenere.getSelectionModel().selectFirst();
    }

    // ------------------------------------------
    // IMPORT/EXPORT JSON
    // ------------------------------------------

    @FXML
    private void onEsportaJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva libreria come JSON");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));

        File file = fileChooser.showSaveDialog(tabellaLibri.getScene().getWindow());
        if (file != null) {
            try {
                libroService.esportaInJson(file, List.copyOf(libriData));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Esportazione JSON completata");
                alert.setHeaderText(null);
                alert.setContentText("La libreria è stata esportata in " + file.getName());
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText("Esportazione JSON fallita");
                alert.setContentText("Impossibile salvare in JSON:\n" + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onImportaJson() {


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Apri file JSON per importare");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));

        File file = fileChooser.showOpenDialog(tabellaLibri.getScene().getWindow());
        if (file != null) {
            try {
                libroService.importaDaJson(file);
                refreshLibri();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Importazione JSON completata");
                alert.setHeaderText(null);
                alert.setContentText("I libri sono stati importati da JSON.");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore di importazione");
                alert.setHeaderText("Import JSON fallito");
                alert.setContentText("Dettagli: " + e.getMessage());
                alert.showAndWait();
            }
        }
        // ** salvo snapshot**
        originator.setState(List.copyOf(libriData));
        caretaker.save();
        historyVersion.set(historyVersion.get() + 1);
    }

    // ------------------------------------------
    // IMPORT/EXPORT CSV
    // ------------------------------------------

    @FXML
    private void onEsportaCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva libreria come CSV");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        File file = fileChooser.showSaveDialog(tabellaLibri.getScene().getWindow());
        if (file != null) {
            try {
                // Chiamo direttamente il service, passando tutti i libri attuali
                libroService.esportaInCsv(file, List.copyOf(libriData));

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Esportazione CSV completata");
                alert.setHeaderText(null);
                alert.setContentText("File CSV salvato in " + file.getName());
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText("Esportazione CSV fallita");
                alert.setContentText("Impossibile salvare in CSV:\n" + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onImportaCsv() {


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Apri file CSV per importare");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        File file = fileChooser.showOpenDialog(tabellaLibri.getScene().getWindow());
        if (file != null) {
            try {
                // Chiamo il metodo service che legge il CSV e salva in repository
                libroService.importaDaCsv(file);

                // Poi refresho la tabella dai dati del repository
                refreshLibri();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Importazione CSV completata");
                alert.setHeaderText(null);
                alert.setContentText("I libri sono stati importati da CSV.");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore di importazione");
                alert.setHeaderText("Import CSV fallito");
                alert.setContentText("Dettagli: " + e.getMessage());
                alert.showAndWait();
            }
        }

        // ** salvo snapshot**
        originator.setState(List.copyOf(libriData));
        caretaker.save();
        historyVersion.set(historyVersion.get() + 1);
    }

    // ------------------------------------------
    // METODI BACKUP MANUALE
    // ------------------------------------------

    @FXML
    private void onBackupManualeJson() {
        try {
            libroService.backupJsonAutomatico();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Backup JSON completato");
            alert.setHeaderText(null);
            alert.setContentText("Backup manuale JSON effettuato (cartella: backup/).");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore Backup JSON");
            alert.setHeaderText("Impossibile effettuare backup JSON");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onBackupManualeCsv() {
        try {
            libroService.backupCsvAutomatico();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Backup CSV completato");
            alert.setHeaderText(null);
            alert.setContentText("Backup manuale CSV effettuato (cartella: backup/).");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore Backup CSV");
            alert.setHeaderText("Impossibile effettuare backup CSV");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    //      -----------
    //      STATISTICHE
    //      -----------

    @FXML
    private void onApriStatistiche() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gui/StatsView.fxml")
            );
            Parent root = loader.load();

            StatisticheViewController controller = loader.getController();
            controller.setLibroService(libroService);
            controller.loadStatistics();

            Stage stage = new Stage();
            stage.setTitle("Statistiche generali");
            // 1) Fisso le dimensioni
            Scene scenaStatistiche = new Scene(root, 800, 600);
            stage.setScene(scenaStatistiche);

            // 2) Imposto una dimensione minima
            stage.setMinWidth(600);
            stage.setMinHeight(400);

            // 3) (Volendo) Disabilito il ridimensionamento
            // stage.setResizable(false);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showWarning("Impossibile aprire la finestra Statistiche.");
        }
    }

    //      -----------
    //      WEBSOCKET
    //      -----------


    private void inizializzaWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    webSocketSession = session;
                    session.addMessageHandler(String.class, message -> {
                        if ("refresh".equalsIgnoreCase(message.trim())) {
                            Platform.runLater(() -> refreshLibri());
                        }
                    });
                }

                @Override
                public void onError(Session session, Throwable thr) {
                    System.err.println("Errore WebSocket: " + thr.getMessage());
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    System.out.println("WebSocket chiuso: " + closeReason);
                }
            }, URI.create(WS_URI));
            System.out.println("WebSocket connesso a " + WS_URI);
        } catch (Exception e) {
            System.err.println("Errore nella connessione WebSocket: " + e.getMessage());
        }
    }



    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attenzione");
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onExit() {
        if (scheduler != null) scheduler.shutdownNow();
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try { webSocketSession.close(); } catch(IOException e){ e.printStackTrace(); }
        }
        ((Stage) tabellaLibri.getScene().getWindow()).close();
    }

    /**
     * Quando l’app chiude, shut down dello scheduler:
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
