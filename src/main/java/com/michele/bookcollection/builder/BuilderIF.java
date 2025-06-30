package com.michele.bookcollection.builder;

import com.michele.bookcollection.model.Libro;
import com.michele.bookcollection.model.StatoLettura;
import java.util.List;

public interface BuilderIF {
    void reset();
    void setTitolo(String titolo);
    void setAutori(List<String> autori);
    void setISBN(String isbn);
    void setGeneri(List<String> generi);
    void setValutazione(int valutazione);
    void setStatoLettura(StatoLettura stato);
    Libro getLibro();
}
