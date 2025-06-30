package com.michele.bookcollection.factory;

import com.michele.bookcollection.repository.LibroRepository;
import com.michele.bookcollection.repository.LibroRepositoryPostgres;

/**
 * Concrete Creator che implementa il Factory Method
 */
public class PostgresRepositoryFactory extends RepositoryFactory {

    @Override
    protected LibroRepository createRepository() {
        // Costruisce e restituisce la repository Postgres
        return new LibroRepositoryPostgres();
    }

}
