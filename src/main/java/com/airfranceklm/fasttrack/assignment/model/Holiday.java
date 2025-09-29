package com.airfranceklm.fasttrack.assignment.model;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;


import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "holidays")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holiday {
    @Id
    @UuidGenerator
    private UUID holidayId;


    @NotBlank
    private String holidayLabel;


    @NotBlank
    @Pattern(regexp = "^klm[0-9]{6}$", message = "employeeId must match ^klm[0-9]{6}$")
    private String employeeId;


    @NotNull
    private OffsetDateTime startOfHoliday;


    @NotNull
    private OffsetDateTime endOfHoliday;


    @Enumerated(EnumType.STRING)
    @NotNull
    private Status status;
}