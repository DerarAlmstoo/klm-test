package com.airfranceklm.fasttrack.assignment.model;



import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @Pattern(regexp = "^klm[0-9]{6}$", message = "employeeId must match ^klm[0-9]{6}$")
    private String employeeId;


    @NotBlank
    private String name;
}