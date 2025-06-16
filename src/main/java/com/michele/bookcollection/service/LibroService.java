package com.michele.bookcollection.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.LibroDTO;
import com.michele.bookcollection.model.StatoLettura;
import com.michele.bookcollection.repository.LibroRepository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LibroService {
    private final LibroRepository repo;

    public LibroService(LibroRepository repo) {
        this.repo = repo;
    }

    public void aggiungiLibro(Libro libro) {
        String isbnNorm = libro.getISBN().replaceAll("[\\s-]", "");
        boolean exists = repo.getTuttiLibri().stream()
                .anyMatch(l -> l.getISBN() != null
                        && l.getISBN().replaceAll("[\\s-]", "").equalsIgnoreCase(isbnNorm));
        if (exists) throw new IllegalArgumentException("Esiste già un libro con questo ISBN.");
        repo.salvaLibro(libro);
    }

    public List<Libro> getLibri() {
        return repo.getTuttiLibri();
    }

    public void modificaLibro(Libro libro) {
        repo.aggiornaLibro(libro);
    }

    public void rimuoviLibro(String isbn) {
        repo.eliminaLibro(isbn);
    }

    public List<Libro> filtra(String chiave, String valore) {
        return repo.getTuttiLibri().stream()
                .filter(libro -> {
                    switch (chiave.toLowerCase()) {
                        case "titolo": return libro.getTitolo().equalsIgnoreCase(valore);
                        case "autore": return libro.getAutori().stream().anyMatch(a -> a.equalsIgnoreCase(valore));
                        case "genere": return libro.getGeneri().stream().anyMatch(g -> g.equalsIgnoreCase(valore));
                        case "stato": return libro.getStatoLettura().name().equalsIgnoreCase(valore);
                        default: return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Libro> ordina(String campo, boolean crescente) {
        Comparator<Libro> cmp;
        switch (campo.toLowerCase()) {
            case "titolo": cmp = Comparator.comparing(Libro::getTitolo, String.CASE_INSENSITIVE_ORDER); break;
            case "autore": cmp = Comparator.comparing(l -> String.join(", ", l.getAutori()), String.CASE_INSENSITIVE_ORDER); break;
            case "valutazione": cmp = Comparator.comparingInt(Libro::getValutazione); break;
            default: throw new IllegalArgumentException("Campo di ordinamento non valido: " + campo);
        }
        if (!crescente) cmp = cmp.reversed();
        return repo.getTuttiLibri().stream().sorted(cmp).collect(Collectors.toList());
    }

    public Optional<Libro> cercaPerISBN(String isbn) {
        String isbnNorm = isbn.replaceAll("[\\s-]", "");
        return repo.getTuttiLibri().stream()
                .filter(l -> l.getISBN() != null && l.getISBN().replaceAll("[\\s-]", "").equalsIgnoreCase(isbnNorm))
                .findFirst();
    }

    public void esportaInJson(File file, List<Libro> libri) throws IOException {
        // 1) Trasformiamo ogni Libro in LibroDTO
        List<LibroDTO> dtos = libri.stream()
                .map(l -> new LibroDTO(
                        l.getTitolo(),
                        l.getAutori(),
                        l.getISBN(),
                        l.getGeneri(),
                        l.getValutazione(),
                        l.getStatoLettura().toString()
                ))
                .collect(Collectors.toList());

        // 2) Serializziamo la lista di DTO con Gson
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(dtos, writer);
        }
    }

    /**
     * Importa da JSON (file) una lista di libri.
     * Se un ISBN è già presente, salta quel libro.
     *
     * @param file Il file JSON da leggere.
     * @throws IOException Se la lettura/parsing fallisce.
     */
    public void importaDaJson(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // 1) Carica la lista di DTO da file
        Type typeListaDto = new TypeToken<List<LibroDTO>>() {}.getType();
        List<LibroDTO> listaDto;
        try (FileReader reader = new FileReader(file)) {
            listaDto = gson.fromJson(reader, typeListaDto);
        }

        // 2) Trasforma ogni DTO in un oggetto Libro e aggiungi se non duplicato
        for (LibroDTO dto : listaDto) {
            String isbnDto = dto.getIsbn();
            // Controllo duplicati basato su ISBN
            boolean giaPresente = repo.getTuttiLibri().stream()
                    .anyMatch(l -> isbnDto.equalsIgnoreCase(l.getISBN()));
            if (giaPresente) {
                // Salta: un libro con questo ISBN è già in libreria
                continue;
            }

            // Costruisci il nuovo Libro
            // Converti lo stato di lettura (string) a enum
            StatoLettura stato;
            try {
                stato = StatoLettura.valueOf(dto.getStatoLettura());
            } catch (Exception e) {
                // Se il valore non corrisponde a un enum valido, puoi impostare un default
                stato = StatoLettura.DA_LEGGERE;
            }

            // Crea l’oggetto Libro (usa il costruttore esistente)
            Libro nuovo = new Libro(
                    dto.getTitolo(),
                    dto.getAutori(),
                    dto.getIsbn(),
                    dto.getGeneri(),
                    dto.getValutazione(),
                    stato
            );

            repo.salvaLibro(nuovo);
        }
    }

    public void backupJsonAutomatico() throws IOException {
        // Creiamo la cartella “backup” se non esiste
        File backupDir = new File("backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        // Costruiamo un nome file con data/ora: es. backup_2025-06-03_14-30-00.json
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File dest = new File(backupDir, "backup_" + timestamp + ".json");

        // Chiamiamo internamente il metodo di esportazione
        // (magari esiste già in questo service: esportaInJson(File, List<Libro>))
        List<Libro> tutti = getLibri(); // recupera la lista attuale
        esportaInJson(dest, tutti);
    }

    /**
     * Stessa cosa, ma per CSV:
     */
    public void backupCsvAutomatico() throws IOException {
        File backupDir = new File("backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File dest = new File(backupDir, "backup_" + timestamp + ".csv");

        List<Libro> tutti = getLibri();
        esportaInCsv(dest, tutti); // serve implementare questo metodo simile a onEsportaCsv
    }

    /**
     * Esporta la lista di libri in CSV in un file.
     *
     * @param file  Il file di destinazione (con percorso)
     * @param libri La lista di libri da esportare
     * @throws IOException Se la scrittura fallisce
     */
    public void esportaInCsv(File file, List<Libro> libri) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // 1) Intestazione CSV
            writer.write("Titolo,Autori (;) ,ISBN,Generi (;),Valutazione,StatoLettura");
            writer.newLine();

            // 2) Una riga per ogni libro
            for (Libro l : libri) {
                String titolo    = escapeCSV(l.getTitolo());
                String autori    = escapeCSV(String.join(";", l.getAutori()));
                String isbn      = escapeCSV(l.getISBN());
                String generi    = escapeCSV(String.join(";", l.getGeneri()));
                String valutazione = String.valueOf(l.getValutazione());
                String stato     = l.getStatoLettura().toString();

                String line = String.join(",", titolo, autori, isbn, generi, valutazione, stato);
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        }
    }

    /**
     * Importa da un file CSV. Ogni riga corrisponde a un libro, con campi
     * separati da virgola e liste (autori/generi) separate da punto e virgola.
     * Se un libro possiede lo stesso ISBN di uno già esistente, lo salta.
     *
     * @param file Il file CSV da cui leggere
     * @throws IOException Se la lettura fallisce
     */
    public void importaDaCsv(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {

            // 1) Leggi e scarta la riga di intestazione
            String header = reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                // 2) split su virgola (usiamo split(",", -1) per non perdere campi vuoti)
                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    // Riga malformata: salta
                    continue;
                }

                // 3) Ripulisci (unescape) ogni campo
                String titolo = unescapeCSV(parts[0]);
                List<String> autori = List.of(unescapeCSV(parts[1]).split(";"));
                String isbn = unescapeCSV(parts[2]);
                List<String> generi = List.of(unescapeCSV(parts[3]).split(";"));
                int valutazione;
                try {
                    valutazione = Integer.parseInt(parts[4]);
                } catch (NumberFormatException e) {
                    valutazione = 0;
                }
                String statoStr = parts[5];
                StatoLettura stato;
                try {
                    stato = StatoLettura.valueOf(statoStr);
                } catch (Exception e) {
                    stato = StatoLettura.DA_LEGGERE;
                }

                // 4) Controlla duplicato in repo (basato su ISBN)
                boolean giaPresente = repo.getTuttiLibri().stream()
                        .anyMatch(l -> l.getISBN().equalsIgnoreCase(isbn));
                if (giaPresente) {
                    continue;
                }

                // 5) Crea e salva il nuovo Libro
                Libro nuovo = new Libro(
                        titolo,
                        autori,
                        isbn,
                        generi,
                        valutazione,
                        stato
                );
                repo.salvaLibro(nuovo);
            }
        }
    }

    /**
     * Metodo di utilità: se un campo CSV contiene virgole, punti e virgola o doppi apici,
     * lo racchiude fra doppi apici e raddoppia gli eventuali doppi apici interni.
     *
     * @param field La stringa da serializzare
     * @return La stringa “escape-ata” per CSV
     */
    public String escapeCSV(String field) {
        if (field == null) {
            return "";
        }
        // Raddoppia i doppi apici interni
        String clean = field.replace("\"", "\"\"");
        // Se contiene virgola, punto e virgola o doppi apici, racchiudilo in doppi apici
        if (clean.contains(",") || clean.contains(";") || clean.contains("\"")) {
            return "\"" + clean + "\"";
        }
        return clean;
    }

    /**
     * Se un campo CSV era racchiuso tra doppi apici, rimuove quelli esterni e
     * converte le doppie virgolette interne in singole virgolette.
     *
     * @param field La stringa letta dal CSV
     * @return La stringa “unescaped”
     */
    public String unescapeCSV(String field) {
        if (field == null) {
            return "";
        }
        String f = field;
        if (f.startsWith("\"") && f.endsWith("\"")) {
            f = f.substring(1, f.length() - 1).replace("\"\"", "\"");
        }
        return f;
    }

    public int getTotaleLibri() {
        return repo.getTuttiLibri().size();
    }

    /**
     * Restituisce una mappa <genere, conteggio> per tutti i libri.
     * Se un libro ha più generi, ognuno di essi incrementa il rispettivo contatore.
     */
    public Map<String, Long> getConteggioPerGenere() {
        return repo.getTuttiLibri().stream()
                .flatMap(l -> l.getGeneri().stream())
                .collect(Collectors.groupingBy(
                        g -> g,
                        Collectors.counting()
                ));
    }

    /**
     * Restituisce una mappa <statoLettura, conteggio> per tutti i libri.
     */
    public Map<StatoLettura, Long> getConteggioPerStato() {
        return repo.getTuttiLibri().stream()
                .collect(Collectors.groupingBy(
                        Libro::getStatoLettura,
                        Collectors.counting()
                ));
    }

    /**
     * Restituisce una mappa <valutazione (1-5), conteggio> per tutti i libri.
     */
    public Map<Integer, Long> getConteggioPerValutazione() {
        return repo.getTuttiLibri().stream()
                .collect(Collectors.groupingBy(
                        Libro::getValutazione,
                        Collectors.counting()
                ));
    }

    /**
     * Calcola la valutazione media di tutti i libri. Restituisce 0.0 se non ci sono libri.
     */
    public double getValutazioneMedia() {
        List<Libro> lista = repo.getTuttiLibri();
        if (lista.isEmpty()) {
            return 0.0;
        }
        double somma = lista.stream()
                .mapToInt(Libro::getValutazione)
                .sum();
        return somma / lista.size();
    }

    /**
     * Restituisce una mappa <autore, conteggio> basata su tutti i libri.
     * Se un libro ha più autori, ognuno di essi incrementa il proprio contatore.
     */
    public Map<String, Long> getConteggioPerAutore() {
        return repo.getTuttiLibri().stream()
                .flatMap(l -> l.getAutori().stream())
                .collect(Collectors.groupingBy(
                        a -> a,
                        Collectors.counting()
                ));
    }


}
