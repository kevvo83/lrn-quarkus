package com.kevlaw.app1.repositories;

import com.kevlaw.app1.exceptions.DuplicateEmployeeInsertException;
import com.kevlaw.app1.exceptions.RepositoryTimeoutException;
import com.kevlaw.app1.exceptions.ServerError;
import io.smallrye.mutiny.Uni;

import java.math.BigInteger;

public interface AsyncRepository<T> {

    Uni<Void> addSubject(T subject) throws DuplicateEmployeeInsertException, ServerError, RepositoryTimeoutException;
    Uni<T> getSubject(BigInteger id) throws IllegalArgumentException, RepositoryTimeoutException;

}
