package com.equip.equiprental.member.repository;

import com.equip.equiprental.member.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
