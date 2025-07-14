package com.michele.bookcollection.service.strategy;

import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ordina per stato di lettura (ordine enum: DA_LEGGERE → IN_LETTURA → LETTO).
 */
public class OrdinamentoPerStatoLettura implements OrdinamentoStrategy {
    private final boolean crescente;

    /**
     * @param crescente true se voglio in ordine alfabetico A→Z, false per Z→A
     */
    public OrdinamentoPerStatoLettura(boolean crescente) {
        this.crescente = crescente;
    }

    @Override
    public List<Libro> ordina(List<Libro> libri) {
        Comparator<Libro> cmp = Comparator.comparing(
                l -> l.getStatoLettura().ordinal()
        );
        if (!crescente) cmp = cmp.reversed();
        return libri.stream().sorted(cmp).collect(Collectors.toList());
    }

    @Override
    public String getNome() {
        return "Stato lettura " + (crescente ? "↑" : "↓");
    }
}
