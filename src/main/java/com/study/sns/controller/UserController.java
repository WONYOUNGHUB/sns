package com.study.sns.controller;

import com.study.sns.controller.request.UserJoinRequest;
import com.study.sns.controller.request.UserLoginRequest;
import com.study.sns.controller.response.AlarmResponse;
import com.study.sns.controller.response.Response;
import com.study.sns.controller.response.UserJoinResponse;
import com.study.sns.controller.response.UserLoginResponse;
import com.study.sns.exception.ErrorCode;
import com.study.sns.exception.SnsApplicationException;
import com.study.sns.model.User;
import com.study.sns.service.AlarmService;
import com.study.sns.service.UserService;
import com.study.sns.util.ClassUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AlarmService alarmService;

    @PostMapping("/join")
    public Response<UserJoinResponse> join(@RequestBody UserJoinRequest request){

        User user = userService.join(request.getUserName(), request.getPassword());
        return Response.success(UserJoinResponse.fromUser(user));
    }
    @PostMapping("/login")
    public Response<UserLoginResponse>login(@RequestBody UserLoginRequest request){
        String token = userService.login(request.getName(),request.getPassword());
        return Response.success(new UserLoginResponse(token));
    }
    @GetMapping("/alarm")
    public Response<Page<AlarmResponse>>alarm(Pageable pageable, Authentication authentication){
        User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(),User.class).orElseThrow(()->new SnsApplicationException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Casting to User class failed"));
        return Response.success(userService.alarmList(user.getId(),pageable).map(AlarmResponse::fromAlarm));
    }
    @GetMapping("/alarm/subscribe")
    public SseEmitter subscribe(Authentication authentication){
        User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(),User.class).orElseThrow(()->new SnsApplicationException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Casting to User class failed"));
        return alarmService.connectAlarm(user.getId());
    }


}
