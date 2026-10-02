package com.kevlaw.app1.repositories;

import com.kevlaw.app1.exceptions.DuplicateEmployeeInsertException;
import com.kevlaw.app1.exceptions.ServerError;
import java.math.BigInteger;
import java.util.Optional;

public interface SyncRepository<T> {

    void addSubject(T subject) throws DuplicateEmployeeInsertException, ServerError;
    Optional<T> getSubject(BigInteger id);

    // Shared behaviour across implementing classes

}
