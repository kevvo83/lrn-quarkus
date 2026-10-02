package com.kevlaw.app1.repositories;

import com.kevlaw.app1.exceptions.DuplicateEmployeeInsertException;
import com.kevlaw.app1.exceptions.ServerError;
import com.kevlaw.app1.models.Employee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

import static com.kevlaw.app1.repositories.RepositoryUtils.isConstraintViolation;

@ApplicationScoped
public class SyncEmployeeRepository implements SyncRepository<Employee> {

    private static final Logger logger = LoggerFactory.getLogger(SyncEmployeeRepository.class);

    @Inject
    EntityManager entityManager;

    public void addSubject(Employee employee) throws DuplicateEmployeeInsertException, ServerError {
        try {
            entityManager.persist(employee);
        }
        catch (EntityExistsException exception) {
            logger.error("Employee details provided already exist in the system, cannot be added to the database");
            logger.error(Arrays.toString(exception.getStackTrace()));
            throw new DuplicateEmployeeInsertException(exception);
        }
        catch (PersistenceException exception) {
            if (isConstraintViolation(exception)) {
                throw new DuplicateEmployeeInsertException(exception);
            } else {
                throw new ServerError();
            }
        }
    }

//    Using the entityManager's find() function - which is eager - need to compare with lazy functions
    public Optional<Employee> getSubject(BigInteger id) {
        try {
            return Optional.ofNullable(entityManager.find(Employee.class, id));
        }
        catch (IllegalArgumentException exp) {
            logger.error("");
            return Optional.empty();
        }
    }
}
