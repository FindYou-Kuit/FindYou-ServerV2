package com.kuit.findyou.domain.user.service.change_nickname;

import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChangeNicknameServiceImpl implements ChangeNicknameService{

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void changeNickName(Long userId, String newNickName) {
        User loginedUser = userRepository.getReferenceById(userId);

        loginedUser.changeNickname(newNickName);
    }
}
