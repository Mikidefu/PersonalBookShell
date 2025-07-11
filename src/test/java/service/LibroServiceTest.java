package service;

import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;
import com.michele.bookcollection.repository.LibroRepository;
import com.michele.bookcollection.service.LibroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibroServiceTest {

    private LibroRepository repo;
    private LibroService service;

    @BeforeEach
    void setUp() {
        // creiamo un mock repository
        repo = mock(LibroRepository.class);
        service = new LibroService(repo);
    }

    @Test
    void aggiungiLibro_doppioISBN_lanciaEccezione() {
        Libro l = new Libro("A",List.of("A"), "123", List.of("g"), 3, StatoLettura.DA_LEGGERE);
        // quando chiedo tutti i libri ritorno uno con stesso ISBN
        when(repo.getTuttiLibri()).thenReturn(List.of(l));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.aggiungiLibro(new Libro("B",List.of("B"),"123",List.of("g"),1,StatoLettura.DA_LEGGERE))
        );
        assertEquals("Esiste già un libro con questo ISBN.", ex.getMessage());
    }

    @Test
    void filtra_perTitolo() {
        Libro l1 = new Libro("JavaFX", List.of("M"), "1", List.of("gui"), 5, StatoLettura.LETTO);
        Libro l2 = new Libro("Spring", List.of("M"), "2", List.of("web"), 4, StatoLettura.LETTO);
        when(repo.getTuttiLibri()).thenReturn(List.of(l1, l2));
        List<Libro> result = service.filtra("titolo","JavaFX");
        assertEquals(1, result.size());
        assertSame(l1, result.get(0));
    }

}
