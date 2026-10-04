package com.kevlaw.app1.dtos;
import com.kevlaw.app1.models.Employee;
import jakarta.annotation.Nullable;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeDto {
    public String id;
    public String firstName;
    public String lastName;
    public String middleName;

    public EmployeeDto(
        Long id,
        @Nonnull String firstName,
        @Nonnull String lastName,
        @Nullable String middleName
    )
    {
        this.id = (id != null) ? id.toString() : null;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
    }

    public static EmployeeDto fromEmployee(Employee employee) {
        return new EmployeeDto(
            employee.id, employee.firstName, employee.lastName, employee.middleName
        );
    }

}