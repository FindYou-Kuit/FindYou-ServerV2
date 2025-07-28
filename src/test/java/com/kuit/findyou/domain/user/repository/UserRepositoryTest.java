package com.kuit.findyou.domain.user.repository;

import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
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

    @DisplayName("카카오 ID로 유저가 조회되는지 테스트")
    @Test
    void should_ReturnUser_When_UserWithKakaoIdExists(){
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        User user = createUser(NAME, ROLE, KAKAO_ID);

        // when
        Optional<User> optUser = userRepository.findByKakaoId(KAKAO_ID);

        // then
        assertThat(optUser.isPresent()).isTrue();
        User foundUser = optUser.get();
        assertThat(foundUser.getName()).isEqualTo(NAME);
        assertThat(foundUser.getRole()).isEqualTo(ROLE);
        assertThat(foundUser.getKakaoId()).isEqualTo(KAKAO_ID);
    }

    private User createUser(String name, Role role, Long kakaoId){
        User build = User.builder()
                .name(name)
                .role(role)
                .kakaoId(kakaoId)
                .build();
        return userRepository.save(build);
    }
}