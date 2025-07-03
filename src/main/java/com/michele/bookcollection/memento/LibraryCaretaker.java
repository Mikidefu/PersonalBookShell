package com.michele.bookcollection.memento;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce la lista dei mementi e l’undo/redo.
 */
public class LibraryCaretaker {
    private final LibraryOriginator originator;
    private final List<LibraryMemento> history = new ArrayList<>();
    private int currentIndex = -1;

    public LibraryCaretaker(LibraryOriginator originator) {
        this.originator = originator;
    }

    /** Salva un nuovo snapshot alla fine della history (cancella eventuali “redo” ancora possibili). */
    public void save() {
        // se avevamo fatto degli undo e poi salviamo un nuovo stato,
        // eliminiamo tutti i possibili redo che erano rimasti
        while (history.size() > currentIndex + 1) {
            history.remove(history.size() - 1);
        }
        history.add(originator.save());
        currentIndex = history.size() - 1;
    }

    /** Ritorna true se c’è almeno un momento precedente a cui tornare. */
    public boolean canUndo() {
        return currentIndex > 0;
    }

    /** Ritorna true se c’è almeno uno snapshot futuro (redo) pronto. */
    public boolean canRedo() {
        return currentIndex < history.size() - 1;
    }

    /** Torna allo snapshot precedente (lancia IllegalStateException se canUndo()==false). */
    public void undo() {
        if (!canUndo()) {
            throw new IllegalStateException("Niente da undo");
        }
        currentIndex--;
        originator.restore(history.get(currentIndex));
    }

    /** Torna allo snapshot successivo (lancia IllegalStateException se canRedo()==false). */
    public void redo() {
        if (!canRedo()) {
            throw new IllegalStateException("Niente da redo");
        }
        currentIndex++;
        originator.restore(history.get(currentIndex));
    }
}
