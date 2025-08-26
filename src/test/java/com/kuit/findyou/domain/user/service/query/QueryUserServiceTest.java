package com.kuit.findyou.domain.user.service.query;

import com.kuit.findyou.domain.user.dto.GetUserProfileResponse;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryUserServiceTest {
    @InjectMocks
    QueryUserServiceImpl queryUserService;
    @Mock
    UserRepository userRepository;
    @DisplayName("사용자가 존재하면 프로필을 조회에 성공한다")
    @Test
    void shouldReturnUserProfile_WhenUserExists(){
        // given
        final long userId = 1L;
        final String name = "name";
        final String profileImage = "default";
        User mockUser = mock(User.class);
        when(userRepository.getReferenceById(anyLong())).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn(name);
        when(mockUser.getProfileImageUrl()).thenReturn(profileImage);

        // when
        GetUserProfileResponse userProfile = queryUserService.getUserProfile(userId);

        // then
        assertThat(userProfile.nickname()).isEqualTo(name);
        assertThat(userProfile.profileImage()).isEqualTo(profileImage);
    }
}