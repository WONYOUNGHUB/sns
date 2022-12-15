package com.study.sns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.sns.exception.ErrorCode;
import com.study.sns.exception.SnsApplicationException;
import com.study.sns.model.User;
import com.study.sns.controller.request.UserJoinRequest;
import com.study.sns.controller.request.UserLoginRequest;
import com.study.sns.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void 회원가입() throws Exception{
        String userName = "userName";
        String password = "password";

        when(userService.join(userName,password)).thenReturn(mock(User.class));

        mockMvc.perform(post("/api/v1/users/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new UserJoinRequest("userName","password")))

        ).andDo(print()).andExpect(status().isOk());
    }
    @Test
    public void 회원가입시_이미_회원가입된_유저네임으로_회원가입을_하는경우() throws Exception{
        String userName = "userName";
        String password = "password";
        when(userService.join(userName,password)).thenThrow(new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME));
        mockMvc.perform(post("/api/v1/users/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new UserJoinRequest(userName,password)))
        ).andDo(print()).andExpect(status().isConflict());

    }
    @Test
    public void 로그인() throws Exception{
        String userName = "userName";
        String password = "password";

        when(userService.login(userName,password)).thenReturn("test_token");

        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName,password)))
        ).andDo(print()).andExpect(status().isOk());
    }
    @Test
    public void 로그인시회원가입안된유저네임에러반환() throws Exception{
        String userName = "userName";
        String password = "password";

        when(userService.login(userName,password)).thenThrow(new SnsApplicationException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName,password)))
        ).andDo(print()).andExpect(status().isNotFound());
    }
    @Test
    public void 로그인시비밀번호틀릴경우에러반환() throws Exception{
        String userName = "userName";
        String password = "password";

        when(userService.login(userName,password)).thenThrow(new SnsApplicationException(ErrorCode.INVALID_PASSWORD));

        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName,password)))
        ).andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 알람기능() throws Exception{
        when(userService.alarmList(any(),any())).thenReturn(Page.empty());
        mockMvc.perform(get("/api/v1/users/alams")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
    @Test
    @WithAnonymousUser
    void 알람리스트요청시로그인하지않은경우() throws Exception{
        when(userService.alarmList(any(),any())).thenReturn(Page.empty());
        mockMvc.perform(get("/api/v1/users/alams")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
