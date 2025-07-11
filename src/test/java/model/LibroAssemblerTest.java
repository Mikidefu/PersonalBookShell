package model;

import com.michele.bookcollection.assembler.LibroAssembler;
import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.LibroDTO;
import com.michele.bookcollection.model.StatoLettura;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibroAssemblerTest {

    @Test
    void createDTO_e_createDomain_roundTrip() {
        Libro orig = new Libro("T", List.of("A1","A2"), "123", List.of("G1"), 4, StatoLettura.IN_LETTURA);
        LibroDTO dto = LibroAssembler.createDTO(orig);
        Libro ricreato = LibroAssembler.createDomain(dto);

        assertEquals(orig.getTitolo(), ricreato.getTitolo());
        assertEquals(orig.getAutori(), ricreato.getAutori());
        assertEquals(orig.getISBN(), ricreato.getISBN());
        assertEquals(orig.getGeneri(), ricreato.getGeneri());
        assertEquals(orig.getValutazione(), ricreato.getValutazione());
        assertEquals(orig.getStatoLettura(), ricreato.getStatoLettura());
    }

    @Test
    void updateDomain_modificaSoloCampi() {
        Libro orig = new Libro("Old", List.of("A"), "111", List.of("G"), 1, StatoLettura.DA_LEGGERE);
        LibroDTO dto = new LibroDTO("New", List.of("B"), "111", List.of("H"), 5, "LETTO");
        LibroAssembler.updateDomain(dto, orig);
        assertEquals("New", orig.getTitolo());
        assertEquals(List.of("B"), orig.getAutori());
        assertEquals(5, orig.getValutazione());
        assertEquals(StatoLettura.LETTO, orig.getStatoLettura());
    }
}
