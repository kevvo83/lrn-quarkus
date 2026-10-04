package com.kevlaw.app1;

import com.kevlaw.app1.dtos.EmployeeDto;
import com.kevlaw.app1.exceptions.DuplicateEmployeeInsertException;
import com.kevlaw.app1.exceptions.RepositoryTimeoutException;
import com.kevlaw.app1.models.Employee;
import com.kevlaw.app1.repositories.AsyncEmployeeRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/employeesCompletableStage")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeControllerUsingCompletionStage {

    private static final Logger logger
            = LoggerFactory.getLogger(EmployeeController.class);

    @Inject
    AsyncEmployeeRepository asyncEmployeeRepository;

    @GET
    @Path("queryEmployee")
    public CompletionStage<Response> getEmployeeById(@QueryParam("id") String id) {

        return CompletableFuture.
                completedFuture(id).
                thenApply(ip -> {
                    if (ip == null) throw new IllegalArgumentException(String.format("Cannot process provided input argument %s", ip));
                    return new BigInteger(ip.trim(), 10);
                }).
                thenCompose(ip2 -> asyncEmployeeRepository.getSubject(ip2).subscribeAsCompletionStage()).
                thenApply(res -> {
                    if (res == null) return Response.status(Response.Status.NOT_FOUND).entity(
                        Map.of("error", String.format("Employee of id %s is not found", id))
                    ).build();
                    else return Response.ok(EmployeeDto.fromEmployee(res)).build();
                }).
                exceptionally(exception -> {
                    Throwable cause = exception.getCause();
                    if (cause instanceof RepositoryTimeoutException) {
                        return Response.status(Response.Status.REQUEST_TIMEOUT).entity(
                                Map.of("error", "Request timeout exception")
                        ).build();
                    }
                    else if (cause instanceof IllegalArgumentException) {
                        return Response.status(Response.Status.BAD_REQUEST).entity(
                                Map.of(
                                        "error", "Bad Request, improperly passed parameters",
                                        "body", id
                                )
                        ).build();
                    }
                    else {
                        return Response.serverError().build();
                    }
                });
    }

    @POST
    @Path("addEmployee")
    public CompletionStage<Response> addEmployee(EmployeeDto employee){

        return CompletableFuture.
            completedFuture(Employee.fromDto(employee)).
            thenCompose(emp -> {
                return asyncEmployeeRepository.addSubject(emp).subscribeAsCompletionStage();
            }).thenApply(ignore -> Response.ok(employee).build()).
            exceptionally(exception -> {
                Throwable cause = exception.getCause();
                if (cause instanceof DuplicateEmployeeInsertException) return Response.status(Response.Status.CONFLICT).build();
                else {
                    return Response.serverError().build();
                }
            });

    }
}
