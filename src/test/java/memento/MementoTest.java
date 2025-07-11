package memento;

import com.michele.bookcollection.memento.LibraryCaretaker;
import com.michele.bookcollection.memento.LibraryOriginator;
import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;
import com.michele.bookcollection.repository.LibroRepository;
import com.michele.bookcollection.service.LibroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MementoTest {

    private LibraryOriginator originator;
    private LibraryCaretaker caretaker;

    @BeforeEach
    void setUp() {
        // 1) Creo una repository “in memory” per non toccare il DB
        LibroRepository inMemoryRepo = new LibroRepository() {
            private final List<Libro> store = new ArrayList<>();

            @Override
            public void salvaLibro(Libro libro) {
                store.add(libro);
            }

            @Override
            public List<Libro> getTuttiLibri() {
                // restituisco sempre una copia difensiva
                return new ArrayList<>(store);
            }

            @Override
            public void aggiornaLibro(Libro libro) {
                // rimpiazzo per ISBN
                for (int i = 0; i < store.size(); i++) {
                    if (store.get(i).getISBN().equals(libro.getISBN())) {
                        store.set(i, libro);
                        return;
                    }
                }
            }

            @Override
            public void eliminaLibro(String isbn) {
                store.removeIf(l -> l.getISBN().equals(isbn));
            }
        };

        // 2) Istanzio il service con la repo in‑memory
        LibroService service = new LibroService(inMemoryRepo);

        // 3) Ora il tuo Originator prende il service
        originator = new LibraryOriginator(service);
        caretaker  = new LibraryCaretaker(originator);
    }

    @Test
    void undoRedo_base() {
        // stato iniziale vuoto
        originator.setState(List.of());
        caretaker.save();

        // aggiungo un libro
        Libro l = new Libro("X", List.of(), "1", List.of(), 1, StatoLettura.DA_LEGGERE);
        originator.setState(List.of(l));
        caretaker.save();

        assertTrue(caretaker.canUndo());
        caretaker.undo();
        assertEquals(0, originator.getState().size());

        assertTrue(caretaker.canRedo());
        caretaker.redo();
        assertEquals(1, originator.getState().size());
    }
}
