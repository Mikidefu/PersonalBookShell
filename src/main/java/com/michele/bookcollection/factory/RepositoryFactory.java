package com.michele.bookcollection.factory;

import com.michele.bookcollection.repository.LibroRepository;

/**
 * Creator astratto definisce il Factory Method e un'operazione che utilizza la repository.
 */
public abstract class RepositoryFactory {
    /**
     * Factory Method: restituisce un'istanza di LibroRepository concreto
     */
    protected abstract LibroRepository createRepository();

    /**
     * Esempio di operazione che utilizza la repository
     */
    public LibroRepository operation() {
        // Creazione della repository tramite Factory Method
        System.out.println("Sto creando la repository con il Factory Method");
        LibroRepository repo = createRepository();
        System.out.println("Repository creata: " + repo.getClass().getSimpleName());
        return repo;
    }
}
