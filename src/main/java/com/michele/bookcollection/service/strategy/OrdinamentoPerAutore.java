package com.michele.bookcollection.service.strategy;

import com.michele.bookcollection.model.Libro;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class OrdinamentoPerAutore implements OrdinamentoStrategy {
    private final boolean crescente;

    /**
     * @param crescente true se vogliamo in ordine alfabetico A→Z, false per Z→A
     */
    public OrdinamentoPerAutore(boolean crescente) {
        this.crescente = crescente;
    }

    @Override
    public List<Libro> ordina(List<Libro> libri) {
        Comparator<Libro> cmp = Comparator.comparing(
                l -> l.getAutori().isEmpty() ? "" : l.getAutori().get(0),
                String.CASE_INSENSITIVE_ORDER
        );
        if (!crescente) cmp = cmp.reversed();
        return libri.stream().sorted(cmp).collect(Collectors.toList());
    }

    @Override
    public String getNome() {
        return "Autore " + (crescente ? "↑" : "↓");
    }
}
