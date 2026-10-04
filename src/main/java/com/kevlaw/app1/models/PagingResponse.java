package com.kevlaw.app1.models;

import com.kevlaw.app1.dtos.EmployeeDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class PagingResponse {
    ArrayList<EmployeeDto> employee;
    String cursor;

    public static PagingResponse fromResults(ArrayList<Employee> results, String cursorId) {
        return new PagingResponse(
            results.stream().map(EmployeeDto::fromEmployee).collect(Collectors.toCollection(ArrayList::new)),
            cursorId
        );
    }

}
