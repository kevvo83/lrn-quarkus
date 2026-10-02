package com.kevlaw.app1.models;

import com.kevlaw.app1.dtos.EmployeeDto;
import jakarta.annotation.Nullable;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
    uniqueConstraints = @UniqueConstraint(
        name="employee_first_name_last_name_uniq",
        columnNames = {"firstName", "lastName"}
    )
)
public class Employee {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Nonnull
    public String firstName;
    @Nonnull
    public String lastName;

    @Nullable
    public String middleName;

    public Employee(
            @Nonnull String firstName,
            @Nullable String middleName,
            @Nonnull String lastName
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
    }

    public static Employee fromDto(EmployeeDto dto) {
        return new Employee(
            dto.firstName,
            dto.middleName,
            dto.lastName
        );
    }
}
