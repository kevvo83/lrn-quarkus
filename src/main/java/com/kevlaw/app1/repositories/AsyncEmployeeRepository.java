package com.kevlaw.app1.repositories;

import com.kevlaw.app1.exceptions.DuplicateEmployeeInsertException;
import com.kevlaw.app1.exceptions.ServerError;
import com.kevlaw.app1.exceptions.RepositoryTimeoutException;
import com.kevlaw.app1.models.Employee;
import com.kevlaw.app1.models.PagingResponse;
import io.smallrye.mutiny.Multi;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;

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

    public Uni<PagingResponse> getAllSubjects(String encodedCursor, int limit) {

        final int _limit = Math.max(limit, 1);

        final Long cursorId;

        return decodeCursor(encodedCursor).
            onItem().
            transformToUni(_cursorId -> {
                return sessionFactory.withSession(
                    session -> {
                        var query = _cursorId == null
                                ? session.createQuery("from Employee e order by e.id desc", Employee.class)
                                : session.createQuery(
                                    "from Employee e where e.id < :cursorId order by e.id desc",
                                    Employee.class
                                ).setParameter("cursorId", _cursorId);

                        return query.setMaxResults(_limit).getResultList().map(ArrayList::new);
                    }
                );
            }).
            onItem().transform(results -> {
                String encCursor = results.isEmpty() ? null : encodeCursor(results.getLast().id);
                return PagingResponse.fromResults(results, encCursor);
            }).
            ifNoItem().after(Duration.ofSeconds(jpaTimeoutInSeconds)).
            failWith(RepositoryTimeoutException::new);
    }

    private Uni<Long> decodeCursor(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        String decoded = new String(
                Base64.getUrlDecoder().decode(encodedCursor),
                StandardCharsets.UTF_8
        );
        long id = Long.parseLong(decoded);
        if (id <= 0) {
            throw new IllegalArgumentException("Cursor ID must be positive");
        }
        return Uni.createFrom().item(id);
    }

    private String encodeCursor(long id) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(Long.toString(id).getBytes(StandardCharsets.UTF_8));
    }

}
