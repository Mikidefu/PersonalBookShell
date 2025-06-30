package com.michele.bookcollection.assembler;

import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.LibroDTO;

public class LibroAssembler {

    /** Costruisce un DTO a partire dal Domain Object Libro */
    public static LibroDTO createDTO(Libro libro) {
        LibroDTO dto = new LibroDTO();
        dto.setTitolo(libro.getTitolo());
        dto.setAutori(libro.getAutori());
        dto.setIsbn(libro.getISBN());
        dto.setGeneri(libro.getGeneri());
        dto.setValutazione(libro.getValutazione());
        dto.setStatoLettura(libro.getStatoLettura().name());
        return dto;
    }

    /** Crea un nuovo Domain Object Libro da un DTO */
    public static Libro createDomain(LibroDTO dto) {
        return new Libro(
                dto.getTitolo(),
                dto.getAutori(),
                dto.getISBN(),
                dto.getGeneri(),
                dto.getValutazione(),
                // converto la stringa in enum, default DA_LEGGERE se invalida
                dto.getStatoLettura()
        );
    }

    /** Aggiorna un Domain Object esistente usando i dati del DTO */
    public static void updateDomain(LibroDTO dto, Libro libro) {
        libro.setTitolo(dto.getTitolo());
        libro.setAutori(dto.getAutori());
        libro.setISBN(dto.getISBN());
        libro.setGeneri(dto.getGeneri());
        libro.setValutazione(dto.getValutazione());
        libro.setStatoLettura(
                dto.getStatoLettura()
        );
    }
}
