package com.michele.bookcollection.model;

import java.util.List;

public interface LibroIF {
    String getTitolo();
    List<String> getAutori();
    String getISBN();
    List<String> getGeneri();
    int getValutazione();
    StatoLettura getStatoLettura();
}
