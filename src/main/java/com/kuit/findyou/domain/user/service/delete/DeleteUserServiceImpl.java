package com.kuit.findyou.domain.user.service.delete;

import com.kuit.findyou.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeleteUserServiceImpl implements DeleteUserService{

    private final UserRepository userRepository;

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
