package com.michele.bookcollection.service.strategy;

import com.michele.bookcollection.model.Libro;
import java.util.List;

public interface OrdinamentoStrategy {
    List<Libro> ordina(List<Libro> libri);
    String getNome(); // per visualizzare nel menu a tendina
}
