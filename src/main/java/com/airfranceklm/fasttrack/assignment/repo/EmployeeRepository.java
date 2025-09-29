package com.airfranceklm.fasttrack.assignment.repo;



import com.airfranceklm.fasttrack.assignment.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeRepository extends JpaRepository<Employee, String> {}