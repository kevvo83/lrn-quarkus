package com.kevlaw.app1;

import com.kevlaw.app1.dtos.EmployeeDto;
import com.kevlaw.app1.exceptions.DuplicateEmployeeInsertException;
import com.kevlaw.app1.exceptions.RepositoryTimeoutException;
import com.kevlaw.app1.exceptions.ServerError;
import com.kevlaw.app1.models.Employee;
import com.kevlaw.app1.models.PagingResponse;
import com.kevlaw.app1.repositories.AsyncEmployeeRepository;
import com.kevlaw.app1.repositories.SyncEmployeeRepository;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeController {

    private static final Logger logger
            = LoggerFactory.getLogger(EmployeeController.class);

    @Inject
    SyncEmployeeRepository syncEmployeeRepository;

    @Inject
    AsyncEmployeeRepository asyncEmployeeRepository;

    @POST
    @Path("addEmployee")
    @Transactional
    public Response addEmployeeDetails(EmployeeDto employeeDto) {
        Employee employeeToBeAdded = new Employee(
            employeeDto.firstName,
            employeeDto.middleName,
            employeeDto.lastName
        );
        try {
            syncEmployeeRepository.addSubject(employeeToBeAdded);

            EmployeeDto createdEntity = new EmployeeDto(
                    employeeToBeAdded.id, employeeToBeAdded.firstName,
                    employeeToBeAdded.lastName, employeeToBeAdded.middleName
            );

            return Response.ok(createdEntity).build();
        }
        catch (DuplicateEmployeeInsertException exp) {
            return Response.
                    status(Response.Status.CONFLICT).
                    entity(
                        Map.of(
                            "error", "Duplicate record detected, could not add new record as configured",
                            "body", employeeDto
                        )
                    ).
                    build();
        }
        catch (ServerError exp) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("queryEmployee")
    public Response getEmployeeById(@QueryParam("id") String id) {

        if (id == null) return Response.status(Response.Status.BAD_REQUEST).build();

        Optional<Employee> result = syncEmployeeRepository.getSubject(new BigInteger(id.trim(), 10));

        if (result.isPresent()) {
            Employee emp = result.get();
            EmployeeDto empResult = new EmployeeDto(emp.id, emp.firstName, emp.lastName, emp.middleName);
            return Response.ok(empResult).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("asyncAddEmployee")
    public Uni<Response> asyncAddEmployee(EmployeeDto employeeDto) {

        Employee createdEntity = Employee.fromDto(employeeDto);

        return asyncEmployeeRepository.
                addSubject(createdEntity).
                onItem().transform(ignored -> Response.ok(createdEntity).build()).
                onFailure(DuplicateEmployeeInsertException.class).
                recoverWithItem(
                        Response.status(Response.Status.CONFLICT).entity(
                            Map.of(
                                "error", "Duplicate record detected, could not add new record as configured",
                                "body", EmployeeDto.fromEmployee(createdEntity)
                            )
                        ).build()
                ).
                onFailure(ServerError.class).
                recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build()).
                onFailure(RepositoryTimeoutException.class).
                recoverWithItem(Response.status(Response.Status.REQUEST_TIMEOUT).build());
    }

    @GET
    @Path("asyncQueryEmployee")
    public Uni<Response> asyncGetEmployeeById(@QueryParam("id") String id) {

        return Uni.
                createFrom().item(id).
                onItem().transform(String::trim).
                onItem().transform(value -> new BigInteger(value, 10)).
                onItem().transformToUni(value -> asyncEmployeeRepository.getSubject(value)).
                onItem().ifNotNull().transform(employee -> Response.ok(EmployeeDto.fromEmployee(employee)).build()).
                onItem().ifNull().continueWith(
                        Response.status(Response.Status.NOT_FOUND).entity(
                            Map.of(
                                "error", String.format("Employee of ID %s not found", id)
                            )
                        ).build()
                ).
                onFailure(RepositoryTimeoutException.class).recoverWithItem(
                        Response.status(Response.Status.REQUEST_TIMEOUT).entity(
                            Map.of(
                                "error", "Timeout exception"
                            )
                        ).build()
                ).
                onFailure(IllegalArgumentException.class).recoverWithItem(
                        Response.status(Response.Status.BAD_REQUEST).entity(
                            Map.of(
                                "error", "Bad Request, improperly passed parameters",
                                "body", id
                            )
                        ).build()
                ).
                onFailure(NumberFormatException.class).recoverWithItem(
                        Response.status(Response.Status.BAD_REQUEST).entity(
                            Map.of(
                                "error", "Bad Request, improperly passed parameters",
                                "body", id
                            )
                        ).build()
                );
    }

    @GET
    @Path("/healthz")
    public Uni<Response> healthz() {
        return Uni.createFrom().item(Response.ok().build());
    }

    @GET
    @Path("/getAllEmployees")
    public Uni<Response> getAllEmployees(@QueryParam("cursorId") String cursorId, @QueryParam("limit") int limit) {
        return asyncEmployeeRepository.getAllSubjects(cursorId, limit).
                onItem().transform(res -> {
                    return res.getEmployee().isEmpty() ? Response.status(Response.Status.NOT_FOUND).build() :
                            Response.ok(res).build();
                });
    }
}
