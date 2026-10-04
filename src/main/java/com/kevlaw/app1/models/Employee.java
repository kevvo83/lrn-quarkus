package com.kevlaw.app1.models;

import com.kevlaw.app1.dtos.EmployeeDto;
import jakarta.annotation.Nullable;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
    uniqueConstraints = @UniqueConstraint(
        name="employee_first_name_last_name_uniq",
        columnNames = {"first_name", "last_name"}
    )
)
public class Employee {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    @Nonnull
    @Column(name = "first_name", nullable = false)
    public String firstName;

    @Nonnull
    @Column(name = "last_name", nullable = false)
    public String lastName;

    @Nullable
    @Column(name = "middle_name")
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
