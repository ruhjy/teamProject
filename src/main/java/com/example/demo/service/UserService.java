package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateEmail;
import com.example.demo.exception.UserNotFound;
import com.example.demo.mapper.UserMapper;
import com.example.demo.request.DeleteUser;
import com.example.demo.request.UserCreate;
import com.example.demo.request.UserEdit;
import com.example.demo.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor // final로 생성된 field의 생성자 자동 autowired
@Transactional(rollbackFor = Exception.class)
public class UserService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    public User add(UserCreate userCreate) {
        if (!userMapper.findByEmail(userCreate.getEmail()).isEmpty()) {
            throw new DuplicateEmail();
        }
        if (!userMapper.findByName(userCreate.getName()).isEmpty()){
            throw new DuplicateEmail("중복된 닉네임입니다.");
        }
        User user = User.builder()
                .name(userCreate.getName())
                .email(userCreate.getEmail())
                .password(passwordEncoder.encode(userCreate.getPassword()))
                .build();
        userMapper.save(user);
        return userMapper.findById(user.getId()).orElseThrow(UserNotFound::new);
    }

    public List<User> getUsers(int page) {
        return userMapper.findAll(page);
    }

    public UserResponse get(Long id) {
        User user = userMapper.findById(id).orElseThrow(UserNotFound::new);
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .dateTime(user.getDateTime())
                .build();
    }

    public void edit(UserEdit userEdit) {
        if(!userMapper.findByName(userEdit.getName()).isEmpty()
                && !userMapper.findByName(userEdit.getName()).orElseThrow().getId().equals(userEdit.getUserId())){
            throw new DuplicateEmail("중복된 닉네임입니다.");
        };

        userMapper.update(userEdit);

    }

    public void delete(DeleteUser deleteUser) {
        User user = userMapper.findById(deleteUser.getAuthedUserId()).orElseThrow();
        if(!passwordEncoder.matches(deleteUser.getPassword(), user.getPassword())){
            throw new DuplicateEmail("이전비밀번호를 확인하세요.");
        }
        userMapper.delete(user.getId());
    }
}

