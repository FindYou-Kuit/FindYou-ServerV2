package com.kuit.findyou.domain.user.repository;

import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @DisplayName("중복된 닉네임 존재 여부가 조회되는지 테스트")
    @Test
    void should_ReturnTrue_When_DuplicateNameExists(){
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        final String DEVICE_ID = "1234";
        User user = createUser(NAME, ROLE, KAKAO_ID, DEVICE_ID);

        // when
        boolean exists = userRepository.existsByName(NAME);

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("디바이스 ID로 유저가 조회되는지 테스트")
    @Test
    void should_ReturnUser_When_UserWithDeviceIdExists(){
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        final String DEVICE_ID = "1234";
        User user = createUser(NAME, ROLE, KAKAO_ID, DEVICE_ID);

        // when
        Optional<User> optUser = userRepository.findByDeviceId(DEVICE_ID);

        // then
        assertThat(optUser.isPresent()).isTrue();
        User foundUser = optUser.get();
        assertThat(foundUser.getName()).isEqualTo(NAME);
        assertThat(foundUser.getRole()).isEqualTo(ROLE);
        assertThat(foundUser.getKakaoId()).isEqualTo(KAKAO_ID);
        assertThat(foundUser.getDeviceId()).isEqualTo(DEVICE_ID);
    }

    @DisplayName("카카오 ID로 유저가 조회되는지 테스트")
    @Test
    void should_ReturnUser_When_UserWithKakaoIdExists(){
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        final String DEVICE_ID = "1234";
        User user = createUser(NAME, ROLE, KAKAO_ID, DEVICE_ID);

        // when
        Optional<User> optUser = userRepository.findByKakaoId(KAKAO_ID);

        // then
        assertThat(optUser.isPresent()).isTrue();
        User foundUser = optUser.get();
        assertThat(foundUser.getName()).isEqualTo(NAME);
        assertThat(foundUser.getRole()).isEqualTo(ROLE);
        assertThat(foundUser.getKakaoId()).isEqualTo(KAKAO_ID);
        assertThat(foundUser.getDeviceId()).isEqualTo(DEVICE_ID);
    }

    @Test
    @DisplayName("changeNickname 호출 후 더티체킹으로 UPDATE 가 반영된다")
    void dirtyChecking_NicknameChange() {
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        final String DEVICE_ID = "1234";
        User user = createUser(NAME, ROLE, KAKAO_ID, DEVICE_ID);

        // when
        user.changeNickname("찾아유");
        em.flush();
        em.clear();

        // then
        User found = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("찾아유", found.getName());
    }

    private User createUser(String name, Role role, Long kakaoId, String deviceId){
        User build = User.builder()
                .name(name)
                .role(role)
                .kakaoId(kakaoId)
                .deviceId(deviceId)
                .build();
        return userRepository.save(build);
    }
}