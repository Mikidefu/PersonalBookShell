package com.michele.bookcollection.service.strategy;

import com.michele.bookcollection.model.Libro;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OrdinamentoPerTitolo implements OrdinamentoStrategy {
    private final boolean crescente;

    /**
     * @param crescente true se vogliamo in ordine alfabetico A→Z, false per Z→A
     */
    public OrdinamentoPerTitolo(boolean crescente) {
        this.crescente = crescente;
    }

    @Override
    public List<Libro> ordina(List<Libro> libri) {
        Comparator<Libro> cmp = Comparator.comparing(
                Libro::getTitolo,
                String.CASE_INSENSITIVE_ORDER
        );
        if (!crescente) {
            cmp = cmp.reversed();
        }
        return libri.stream()
                .sorted(cmp)
                .collect(Collectors.toList());
    }

    @Override
    public String getNome() {
        return "Titolo " + (crescente ? "↑" : "↓");
    }
}
