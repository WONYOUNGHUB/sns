package com.study.sns.controller.response;

import com.study.sns.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class UserResponse {


        private Integer id;
        private String userName;

        public static UserResponse fromUser(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getUsername()
            );
        }
}
