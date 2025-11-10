package com.equip.equiprental.member;

import com.equip.equiprental.member.domain.Department;
import com.equip.equiprental.member.dto.DepartmentDto;
import com.equip.equiprental.member.repository.DepartmentRepository;
import com.equip.equiprental.member.service.DepartmentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentServiceImpl 단위 테스트")
public class DepartmentServiceImplTest {
    @Mock DepartmentRepository departmentRepository;

    @InjectMocks
    DepartmentServiceImpl departmentService;

    @Nested
    @DisplayName("getDepartmentList 메서드 테스트")
    class getDepartmentList {
        @Test
        void getDepartmentList_shouldReturnDtoList() {
            // given
            Department dept = new Department(1L, "개발팀");
            when(departmentRepository.findAll()).thenReturn(List.of(dept));

            // when
            List<DepartmentDto> result = departmentService.getDepartmentList();

            // then
            assertThat(result).hasSize(1); // 반환된 리스트 크기 확인
            DepartmentDto dto = result.get(0);
            assertThat(dto.getDepartmentId()).isEqualTo(dept.getDepartmentId()); // ID 매칭
            assertThat(dto.getDepartmentName()).isEqualTo(dept.getDepartmentName()); // 이름 매칭
        }
    }
}
