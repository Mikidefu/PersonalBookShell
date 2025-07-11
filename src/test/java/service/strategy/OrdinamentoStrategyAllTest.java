package service.strategy;

import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;
import com.michele.bookcollection.service.strategy.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdinamentoStrategyAllTest {

    private static List<Libro> sample;

    @BeforeAll
    static void initSample() {
        sample = List.of(
                new Libro("Gamma", List.of("Alice","Bob"),   "1", List.of("Fantasy","Horror"), 3, StatoLettura.IN_LETTURA),
                new Libro("Alpha", List.of("Charlie"),       "2", List.of("SciFi"),              5, StatoLettura.LETTO),
                new Libro("Beta",  List.of("Bob","Charlie"), "3", List.of("Fantasy"),            1, StatoLettura.DA_LEGGERE)
        );
    }

    @Test
    void ordinamentoPerTitolo() {
        var asc  = new OrdinamentoPerTitolo(true).ordina(sample);
        var desc = new OrdinamentoPerTitolo(false).ordina(sample);

        assertEquals("Alpha",  asc.get(0).getTitolo());
        assertEquals("Gamma",  asc.get(2).getTitolo());

        assertEquals("Gamma",  desc.get(0).getTitolo());
        assertEquals("Alpha",  desc.get(2).getTitolo());
    }

    @Test
    void ordinamentoPerAutore() {
        // rispetto al primo autore in lista, ordine alfabetico
        var asc  = new OrdinamentoPerAutore(true).ordina(sample);
        var desc = new OrdinamentoPerAutore(false).ordina(sample);

        // i primi autori: "Alice","Bob","Charlie"
        assertEquals("Gamma", asc.get(0).getTitolo());     // Alice...
        assertEquals("Beta",  asc.get(1).getTitolo());     // Bob,Charlie
        assertEquals("Alpha", asc.get(2).getTitolo());     // Charlie

        assertEquals("Alpha", desc.get(0).getTitolo());
        assertEquals("Beta",  desc.get(1).getTitolo());
        assertEquals("Gamma", desc.get(2).getTitolo());
    }

    @Test
    void ordinamentoPerGenere() {
        // primi generi: Fantasy, Horror, SciFi
        var asc  = new OrdinamentoPerGenere(true).ordina(sample);
        var desc = new OrdinamentoPerGenere(false).ordina(sample);

        // ascending: Fantasy group (Gamma, Beta) poi SciFi (Alpha)
        assertEquals("Gamma", asc.get(0).getTitolo());
        assertEquals("Beta",  asc.get(1).getTitolo());
        assertEquals("Alpha", asc.get(2).getTitolo());

        // descending: SciFi (Alpha), poi Fantasy group reversed stably (Gamma, Beta)
        assertEquals("Alpha", desc.get(0).getTitolo());
        assertEquals("Gamma", desc.get(1).getTitolo());
        assertEquals("Beta",  desc.get(2).getTitolo());
    }


    @Test
    void ordinamentoPerValutazione() {
        var asc  = new OrdinamentoPerValutazione(true).ordina(sample);
        var desc = new OrdinamentoPerValutazione(false).ordina(sample);

        assertEquals(1, asc.get(0).getValutazione());
        assertEquals(5, asc.get(2).getValutazione());

        assertEquals(5, desc.get(0).getValutazione());
        assertEquals(1, desc.get(2).getValutazione());
    }

    @Test
    void ordinamentoPerStatoLettura() {
        var asc  = new OrdinamentoPerStatoLettura(true).ordina(sample);
        var desc = new OrdinamentoPerStatoLettura(false).ordina(sample);

        // enum ordine: DA_LEGGERE(0), IN_LETTURA(1), LETTO(2)
        assertEquals("Beta", asc.get(0).getTitolo());      // DA_LEGGERE
        assertEquals("Gamma",asc.get(1).getTitolo());      // IN_LETTURA
        assertEquals("Alpha",asc.get(2).getTitolo());      // LETTO

        assertEquals("Alpha", desc.get(0).getTitolo());
        assertEquals("Gamma", desc.get(1).getTitolo());
        assertEquals("Beta",  desc.get(2).getTitolo());
    }
}
