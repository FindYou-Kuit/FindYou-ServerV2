package com.kuit.findyou.domain.information.service;

import com.kuit.findyou.domain.information.dto.GetAnimalDepartmentsResponse;
import com.kuit.findyou.domain.information.model.AnimalDepartment;
import com.kuit.findyou.domain.information.repository.AnimalDepartmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalDepartmentServiceTest {

    @InjectMocks
    private AnimalDepartmentServiceImpl service;

    @Mock
    private AnimalDepartmentRepository repository;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    // ===== Helper =====
    private static AnimalDepartment dep(long id, String org, String dept, String phone) {
        return AnimalDepartment.builder()
                .id(id)
                .organization(org)
                .department(dept)
                .phoneNumber(phone)
                .build();
    }

    @DisplayName("필터 없음 + size보다 1개 더 가져오면(isLast=false) / 커서는 마지막 요소 id")
    @Test
    void should_ReturnNonLastPage_When_NoFilter_And_MoreThanSize() {
        // given
        int size = 3;
        long lastId = 10L;

        // repository는 department ASC 정렬로 size+1개 반환 (커서 페이징 패턴)
        var rows = List.of(
                dep(9, "서울특별시 송파구청", "가부서", "1577-0001"),
                dep(8, "서울특별시 송파구청", "나부서", "1577-0002"),
                dep(7, "서울특별시 송파구청", "다부서", "1577-0003"),
                dep(6, "서울특별시 송파구청", "라부서", "1577-0004") // size+1
        );
        when(repository.findAllByIdGreaterThanOrderByDepartmentAsc(eq(lastId), any(Pageable.class)))
                .thenReturn(rows);

        // when
        GetAnimalDepartmentsResponse resp = service.getDepartments(lastId, size, null, null);

        // then
        verify(repository).findAllByIdGreaterThanOrderByDepartmentAsc(eq(lastId), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(size + 1);

        assertThat(resp.isLast()).isFalse();
        assertThat(resp.lastId()).isEqualTo(7L);         // 잘린 마지막 요소의 id
        assertThat(resp.departments()).hasSize(size);
        assertThat(resp.departments().get(0).department()).isEqualTo("가부서");
    }

    @DisplayName("시도/시군구 필터 적용 + size 이하이면(isLast=true) / 커서는 마지막 요소 id")
    @Test
    void should_ReturnLastPage_When_FilterApplied_And_LessOrEqualSize() {
        // given
        int size = 3;
        long lastId = 10L;
        String sido = "서울특별시";
        String sigungu = "송파구";
        String keyword = "서울특별시 송파구"; // 서비스에서 조립해서 contains 검색한다고 가정

        var rows = List.of(
                dep(9, "서울특별시 송파구청", "관광체육과", "1577-0955"),
                dep(8, "서울특별시 송파구청", "경제진흥과", "1577-0956")
        );
        when(repository.findAllByOrganizationContainingAndIdGreaterThanOrderByDepartmentAsc(
                eq(keyword), eq(lastId), any(Pageable.class))
        ).thenReturn(rows);

        // when
        GetAnimalDepartmentsResponse resp = service.getDepartments(lastId, size, sido, sigungu);

        // then
        verify(repository).findAllByOrganizationContainingAndIdGreaterThanOrderByDepartmentAsc(eq(keyword), eq(lastId), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageSize()).isEqualTo(size + 1);

        assertThat(resp.isLast()).isTrue();      // size 이하
        assertThat(resp.lastId()).isEqualTo(8L);
        assertThat(resp.departments()).hasSize(2);
        assertThat(resp.departments().get(0).department()).isEqualTo("관광체육과");
    }
    @DisplayName("lastId가 0(또는 null)로 오면 0 기준으로 첫 페이지 조회")
    @Test
    void should_QueryFromZero_When_LastIdIsZeroOrNull() {
        // given
        int size = 20;

        when(repository.findAllByIdGreaterThanOrderByDepartmentAsc(eq(0L), any(Pageable.class)))
                .thenReturn(List.of()); // 첫 페이지 결과 없음 가정

        // when
        GetAnimalDepartmentsResponse resp = service.getDepartments(0L, size, null, null);

        // then
        verify(repository).findAllByIdGreaterThanOrderByDepartmentAsc(eq(0L), any(Pageable.class));
        assertThat(resp.isLast()).isTrue();
        assertThat(resp.lastId()).isEqualTo(-1L);
        assertThat(resp.departments()).isEmpty();

        // (null로도 한 번 더 확인하고 싶으면:)
        resp = service.getDepartments(null, size, null, null);
        verify(repository, times(2)).findAllByIdGreaterThanOrderByDepartmentAsc(eq(0L), any(Pageable.class));
    }
}
