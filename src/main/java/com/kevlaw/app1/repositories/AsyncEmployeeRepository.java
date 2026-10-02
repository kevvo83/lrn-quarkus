package com.kevlaw.app1.repositories;

import com.kevlaw.app1.exceptions.DuplicateEmployeeInsertException;
import com.kevlaw.app1.exceptions.ServerError;
import com.kevlaw.app1.exceptions.RepositoryTimeoutException;
import com.kevlaw.app1.models.Employee;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.PersistenceException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import static com.kevlaw.app1.repositories.RepositoryUtils.isConstraintViolation;

@ApplicationScoped
public class AsyncEmployeeRepository implements AsyncRepository<Employee> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncEmployeeRepository.class);

    @ConfigProperty(name="jpaTimeoutInSeconds")
    Integer jpaTimeoutInSeconds;

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Override
    public Uni<Void> addSubject(Employee employee) {

        Uni<Void> result = sessionFactory.withTransaction(session -> session.persist(employee));

        return result.
                ifNoItem().after(Duration.ofSeconds(jpaTimeoutInSeconds)).
                failWith(new RepositoryTimeoutException()).
                onFailure(EntityExistsException.class).transform(DuplicateEmployeeInsertException::new).
                onFailure(PersistenceException.class).transform(
                    (exp) -> isConstraintViolation(exp) ? new DuplicateEmployeeInsertException(exp) : new ServerError()
                );

    }

    @Override
    public Uni<Employee> getSubject(BigInteger id) {

        Uni<Employee> result = sessionFactory.withSession(session -> session.find(Employee.class, id));

        return result.ifNoItem().after(Duration.ofSeconds(jpaTimeoutInSeconds)).
                failWith(RepositoryTimeoutException::new).
                onFailure(IllegalArgumentException.class).transform(IllegalArgumentException::new);
    }

}
