package com.kuit.findyou.global.jwt.security;

import com.kuit.findyou.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Long userId;
        try{
             userId = Long.parseLong(username);
        }
        catch(NumberFormatException e) {
            log.warn("[loadUserByUsername] Invalid userId format: {}", username);
            throw new UsernameNotFoundException("Invalid userId format: " + username);
        }

        return userRepository.findById(userId)
                .map(user ->{
                    log.info("[loadUserByUsername] User was found. user = {}",  user);
                    return new CustomUserDetails(user);
                })
                .orElseThrow(() -> {
                    log.info("[loadUserByUsername] User was not found!! userId={}", userId);
                    throw new UsernameNotFoundException("User not found with " + userId);
                });
    }
}
