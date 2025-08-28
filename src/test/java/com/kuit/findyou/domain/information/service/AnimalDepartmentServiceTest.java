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
import java.util.stream.IntStream;

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
    private static List<AnimalDepartment> mkList(long startIdInclusive, int count, String orgPrefix) {
        return IntStream.range(0, count)
                .mapToObj(i -> dep(startIdInclusive + i, orgPrefix, "부서" + (i + 1), "02-000-" + (i + 1)))
                .toList();
    }

    @DisplayName("district 없음 + size보다 1개 더 조회(isLast=false) / 커서는 마지막 요소 id")
    @Test
    void should_ReturnNonLastPage_When_NoDistrict_And_MoreThanSize() {
        // given
        final int size = 3;
        final Long lastId = 0L;
        // size+1(4개) 반환 & 마지막 페이지 아님
        var rows = mkList(1, size + 1, "서울특별시 광진구");
        when(repository.findAllByIdGreaterThanOrderByIdAsc(anyLong(), any(Pageable.class))).thenReturn(rows);

        // when
        GetAnimalDepartmentsResponse resp = service.getDepartments(lastId, size, null);

        // then
        verify(repository).findAllByIdGreaterThanOrderByIdAsc(eq(0L), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(size + 1);

        assertThat(resp.departments()).hasSize(size);
        assertThat(resp.isLast()).isFalse();
        assertThat(resp.lastId()).isEqualTo(3L);
    }

    @DisplayName("district 없음 + size 이하로 조회(isLast=true) / 커서는 마지막 요소 id")
    @Test
    void should_ReturnLastPage_When_NoDistrict_And_LessOrEqualSize() { // given
        final int size = 3;
        final Long lastId = 0L;
        var rows = mkList(1, size - 1, "서울특별시 광진구"); // 2개 → 마지막 페이지
        when(repository.findAllByIdGreaterThanOrderByIdAsc(anyLong(), any(Pageable.class))).thenReturn(rows);

        // when
        GetAnimalDepartmentsResponse resp = service.getDepartments(lastId, size, null);

        // then
        verify(repository).findAllByIdGreaterThanOrderByIdAsc(eq(0L), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(size + 1);

        assertThat(resp.departments()).hasSize(size - 1);
        assertThat(resp.isLast()).isTrue();
        assertThat(resp.lastId()).isNull();
    }

    @DisplayName("정확 일치 우선 -> organization = district 정확 매칭이면 AND/substring 호출 없이 반환")
    @Test
    void should_UseExactMatch_First() {
        // given
        final int size = 3;
        final Long lastId = 0L;
        final String district = "서울특별시 송파구";

        var exactRows = List.of(
                dep(1L, district, "관광체육과", "02-1111-1111"),
                dep(2L, district, "동물보호과", "02-1111-2222"),
                dep(3L, district, "복지정책과", "02-1111-3333"),
                dep(4L, district, "기타", "02-1111-4444") // size+1
        );
        when(repository.findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), any(Pageable.class)))
                .thenReturn(exactRows);

        // when
        GetAnimalDepartmentsResponse resp = service.getDepartments(lastId, size, district);

        // then
        verify(repository).findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), pageableCaptor.capture());
        verify(repository, never()).findAllByOrganizationContainingAndOrganizationContainingAndIdGreaterThanOrderByIdAsc(any(), any(), anyLong(), any()); //호출되지 않아야 함
        verify(repository, never()).findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(any(), anyLong(), any()); //호출되지 않아야 함

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageSize()).isEqualTo(size + 1);

        assertThat(resp.departments()).hasSize(size);
        assertThat(resp.isLast()).isFalse();
        assertThat(resp.lastId()).isEqualTo(3L);
    }

    @DisplayName("정확 일치 실패 시 AND 토큰 사용: '서울특별시' AND '송파구' 동시 포함 행만 반환")
    @Test
    void should_FallbackToAndTokens_When_ExactMatchEmpty() {
        // given
        final int size = 3;
        final Long lastId = 0L;
        final String district = "서울특별시 송파구";

        when(repository.findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), any(Pageable.class)))
                .thenReturn(List.of()); //정확히 일치히는 경우가 없음

        var andRows = List.of(
                dep(1L, "서울특별시 송파구", "관광체육과", "02-1111-1111"),
                dep(2L, "송파구 서울특별시", "동물보호과", "02-1111-2222"),
                dep(3L, "서울특별시 송파구", "복지정책과", "02-1111-3333"),
                dep(4L, "서울특별시 송파구", "기타", "02-1111-4444") // size+1
        );
        when(repository.findAllByOrganizationContainingAndOrganizationContainingAndIdGreaterThanOrderByIdAsc(eq("서울특별시"), eq("송파구"), eq(0L), any(Pageable.class)))
                .thenReturn(andRows);

        // when
        GetAnimalDepartmentsResponse resp = service.getDepartments(lastId, size, district);

        // then
        verify(repository).findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), any(Pageable.class));
        verify(repository).findAllByOrganizationContainingAndOrganizationContainingAndIdGreaterThanOrderByIdAsc(eq("서울특별시"), eq("송파구"), eq(0L), pageableCaptor.capture());
        verify(repository, never()).findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(any(), anyLong(), any());

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageSize()).isEqualTo(size + 1);

        assertThat(resp.departments()).hasSize(size);
        assertThat(resp.isLast()).isFalse();
        assertThat(resp.lastId()).isEqualTo(3L);
    }

    @DisplayName("정확 일치 실패 + 토큰 2개 미만이면 substring로 폴백")
    @Test
    void should_FallbackToSubstring_When_ExactEmpty_And_TokensLessThanTwo() {
        // given
        final int size = 2;
        final Long lastId = 0L;
        final String district = "세종특별자치시"; //시도와 시군구가 나눠져있지 않은 경우임

        when(repository.findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), any(Pageable.class)))
                .thenReturn(List.of()); //없음

        var subRows = List.of(
                dep(1L, "세종특별자치시", "동물보호과", "044-111-1111"),
                dep(2L, "세종특별자치시", "복지과", "044-111-2222")
        );
        when(repository.findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), any(Pageable.class)))
                .thenReturn(subRows);

        // when
        GetAnimalDepartmentsResponse resp = service.getDepartments(lastId, size, district);

        // then
        verify(repository).findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), any(Pageable.class));
        verify(repository, never()).findAllByOrganizationContainingAndOrganizationContainingAndIdGreaterThanOrderByIdAsc(any(), any(), anyLong(), any());
        verify(repository).findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), pageableCaptor.capture());

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageSize()).isEqualTo(size + 1);

        assertThat(resp.departments()).hasSize(size);
        assertThat(resp.isLast()).isTrue();   //반환수가 size와 같지만 size+1을 요청했는데 더 없으니 마지막
        assertThat(resp.lastId()).isNull();
    }

    @DisplayName("정확 일치 실패 + AND 토큰도 실패 시 substring로 폴백")
    @Test
    void should_FallbackToSubstring_When_ExactEmpty_And_AndTokensEmptyToo() {
        // given
        final int size = 2;
        final Long lastId = 0L;
        final String district = "서울특별시 강동구";

        when(repository.findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), any(Pageable.class)))
                .thenReturn(List.of());

        when(repository.findAllByOrganizationContainingAndOrganizationContainingAndIdGreaterThanOrderByIdAsc(eq("서울특별시"), eq("강동구"), eq(0L), any(Pageable.class)))
                .thenReturn(List.of());

        var subRows = List.of(
                dep(10L, "서울특별시 강동구", "동물복지과", "02-1234-5678"),
                dep(11L, "서울특별시 강동구", "보호과", "02-1234-5679"),
                dep(12L, "서울특별시 강동구", "기타", "02-1234-5680") // size+1
        );
        when(repository.findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), any(Pageable.class)))
                .thenReturn(subRows);

        // when
        GetAnimalDepartmentsResponse resp = service.getDepartments(lastId, size, district);

        // then
        verify(repository).findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), any(Pageable.class));
        verify(repository).findAllByOrganizationContainingAndOrganizationContainingAndIdGreaterThanOrderByIdAsc(eq("서울특별시"), eq("강동구"), eq(0L), any(Pageable.class));
        verify(repository).findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(eq(district), eq(0L), pageableCaptor.capture());

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageSize()).isEqualTo(size + 1);

        assertThat(resp.departments()).hasSize(size);
        assertThat(resp.isLast()).isFalse();          // size+1이 왔으니 더 있음
        assertThat(resp.lastId()).isEqualTo(11L);     // 잘린 페이지의 마지막 id
    }

    @DisplayName("커서 값 전달 검증: lastId가 5면 repo 호출도 5로 전달된다")
    @Test
    void should_PassThroughCursorValue() {
        // given
        final int size = 2;
        final Long lastId = 5L;

        var rows = mkList(6, size + 1, "서울특별시 종로구");
        when(repository.findAllByIdGreaterThanOrderByIdAsc(eq(lastId), any(Pageable.class))).thenReturn(rows);

        // when
        GetAnimalDepartmentsResponse resp = service.getDepartments(lastId, size, null);

        // then
        verify(repository).findAllByIdGreaterThanOrderByIdAsc(eq(5L), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(size + 1);

        assertThat(resp.departments()).hasSize(size);
        assertThat(resp.isLast()).isFalse();
        assertThat(resp.lastId()).isEqualTo(7L); // 6,7까지 잘림
    }

    @DisplayName("lastId가 0(또는 null)로 오면 0 기준으로 첫 페이지 조회")
    @Test
    void should_QueryFromZero_When_LastIdIsZeroOrNull() {// given
        final int size = 3;
        var rows = List.of(
                dep(1L, "서울특별시 종로구", "부서1", "02-0001"),
                dep(2L, "서울특별시 종로구", "부서2", "02-0002"),
                dep(3L, "서울특별시 종로구", "부서3", "02-0003"),
                dep(4L, "서울특별시 종로구", "부서4", "02-0004")
        );
        when(repository.findAllByIdGreaterThanOrderByIdAsc(eq(0L), any(Pageable.class)))
                .thenReturn(rows, rows); // null 케이스와 0L 케이스 두 번 호출 대비

        // when
        //case 1: lastId == null
        GetAnimalDepartmentsResponse respNull = service.getDepartments(null, size, null);
        //case 2: lastId == 0L
        GetAnimalDepartmentsResponse respZero = service.getDepartments(0L, size, null);

        // then
        //cursor=0으로 두 번 호출되어야 함
        verify(repository, times(2))
                .findAllByIdGreaterThanOrderByIdAsc(eq(0L), pageableCaptor.capture());

        //district가 없음 -> 다른 검색 메서드는 호출되면 안 됨
        verify(repository, never()).findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(anyString(), anyLong(), any());
        verify(repository, never()).findAllByOrganizationContainingAndOrganizationContainingAndIdGreaterThanOrderByIdAsc(anyString(), anyString(), anyLong(), any());
        verify(repository, never()).findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(anyString(), anyLong(), any());

        //둘 다 page=0, size=size+1
        var used = pageableCaptor.getAllValues();
        assertThat(used).hasSize(2);
        used.forEach(p -> {
            assertThat(p.getPageNumber()).isEqualTo(0);
            assertThat(p.getPageSize()).isEqualTo(size + 1);
        });

        //두 케이스 결과 동일해야 함
        for (GetAnimalDepartmentsResponse resp : List.of(respNull, respZero)) {
            assertThat(resp.departments()).hasSize(size); // 1,2,3 으로 잘림
            assertThat(resp.isLast()).isFalse();          // size+1 받았으므로 더 있음
            assertThat(resp.lastId()).isEqualTo(3L);      // 잘린 페이지의 마지막 id
        }
    }
}
