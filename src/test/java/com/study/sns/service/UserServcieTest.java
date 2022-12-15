package com.study.sns.service;

import com.study.sns.exception.ErrorCode;
import com.study.sns.model.entity.UserEntity;
import com.study.sns.exception.SnsApplicationException;
import com.study.sns.repository.UserEntityRepository;
import fixture.UserEntityFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServcieTest {
    @Autowired
    UserService userService;
    @MockBean
    UserEntityRepository userEntityRepository;
    @MockBean
    private BCryptPasswordEncoder encoder;
    @Test
    public void 회원가입이_정상적으로_작동하는경우(){
        String userName= "userName";
        String password = "password";
        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());
        when(encoder.encode(password)).thenReturn("encrypt_password");
        when(userEntityRepository.save(any())).thenReturn(UserEntityFixture.get(userName,password,1));
        Assertions.assertDoesNotThrow(()->userService.join(userName,password));
    }

    @Test
    public void 회원가입시_유저네임으로_유저가이미있는경우(){
        String userName= "userName";
        String password = "password";
        UserEntity fixture = UserEntityFixture.get(userName,password,1);
        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));
        when(encoder.encode(password)).thenReturn("encrypt_password");
        when(userEntityRepository.save(any())).thenReturn(Optional.of(fixture));

      SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class,()->userService.join(userName,password));
      Assertions.assertEquals(ErrorCode.DUPLICATED_USER_NAME,e.getErrorCode());
    }

    @Test
    public void 로그인이_정상적으로_작동하는경우(){
        String userName= "userName";
        String password = "password";


        UserEntity fixture = UserEntityFixture.get(userName,password,1);

        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));
        when(encoder.matches(password,fixture.getPassword())).thenReturn(true);
        Assertions.assertDoesNotThrow(()->userService.login(userName, password));

    }

    @Test
    public void 로그인시에_유저네임으로_회원가입한유저가_없는경우(){
        String userName= "userName";
        String password = "password";

        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());

        SnsApplicationException e =  Assertions.assertThrows(SnsApplicationException.class,()->userService.login(userName,password));
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND,e.getErrorCode());
    }
    @Test
    public void 로그인시에_패스워드가_틀린경우(){
        String userName= "userName";
        String password = "password";
        String wrongPassword = "wrongPassword";
        UserEntity fixture = UserEntityFixture.get(userName,password,1);
        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));

        SnsApplicationException e= Assertions.assertThrows(SnsApplicationException.class,()->userService.login(userName,password));
        Assertions.assertEquals(ErrorCode.INVALID_PASSWORD,e.getErrorCode());
    }
}
