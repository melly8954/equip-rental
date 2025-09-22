package com.equip.equiprental.member.service;

import com.equip.equiprental.member.domain.Department;
import com.equip.equiprental.member.dto.DepartmentDto;
import com.equip.equiprental.member.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService{
    private final DepartmentRepository departmentRepository;

    @Override
    public List<DepartmentDto> getDepartmentList() {
        List<Department> list = departmentRepository.findAll();


        return list.stream()
                .map(department -> DepartmentDto.builder()
                        .departmentId(department.getDepartmentId())
                        .departmentName(department.getDepartmentName())
                        .build())
                .toList();
    }
}
