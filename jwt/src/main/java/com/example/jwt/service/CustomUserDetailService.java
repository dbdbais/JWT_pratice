package com.example.jwt.service;

import com.example.jwt.dto.CustomUserDetails;
import com.example.jwt.entity.UserEntity;
import com.example.jwt.entity.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username);
        //해당하는 유저 찾는다.
        if(userEntity != null){
            return new CustomUserDetails(userEntity);
        }
        return null;
    }
}
