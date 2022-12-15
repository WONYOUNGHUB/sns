package com.study.sns.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.sns.controller.request.PostCommentRequest;
import com.study.sns.controller.request.PostCreateRequest;
import com.study.sns.controller.request.PostModifyRequest;
import com.study.sns.controller.request.UserLoginRequest;
import com.study.sns.exception.ErrorCode;
import com.study.sns.exception.SnsApplicationException;
import com.study.sns.model.Post;
import com.study.sns.service.PostService;
import fixture.PostEntityFixture;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerTest {
    @MockBean
    private PostService postService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    @WithMockUser
    void 포스트작성() throws  Exception {
        String title = "title";
        String body = "body";
        mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body)))
        ).andDo(print()).andExpect(status().isOk());
    }
        @Test
        @WithAnonymousUser
        void 포트스작성시로그인하지않은경우() throws Exception{
            String title = "title";
            String body = "body";
            mockMvc.perform(post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title,body)))
            ).andDo(print()).andExpect(status().isOk());
        }

    @Test
    @WithMockUser
    void 포스트수정() throws  Exception {
        String title = "title";
        String body = "body";
        when(postService.modify(eq(title),eq(body),any(),eq(1))).thenReturn(Post.fromEntity(PostEntityFixture.get("userName",1,1)));
        mockMvc.perform(put("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
        ).andDo(print()).andExpect(status().isOk());
    }
    @Test
    @WithAnonymousUser
    void 포스트수정시로그인하지않은경우에러발생() throws  Exception {
        String title = "title";
        String body = "body";
        mockMvc.perform(put("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
        ).andDo(print()).andExpect(status().isServiceUnavailable());
    }
    @Test
    @WithMockUser
    void 포스트수정시본인이작성한글이아닌경우에러발생() throws  Exception {
        String title = "title";
        String body = "body";
        //mocking
        doThrow(new SnsApplicationException(ErrorCode.INVALID_PERMISSION)).when(postService).modify(eq(title),eq(body),any(),eq(1));

        mockMvc.perform(put("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
        ).andDo(print()).andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser
    void 포스트수정시수정하려는글이없는경우에러발생() throws  Exception {
        String title = "title";
        String body = "body";
        //mocking

        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).modify(eq(title),eq(body),any(),eq(1));
        mockMvc.perform(put("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
        ).andDo(print()).andExpect(status().isNotFound());
    }
     @Test
    @WithMockUser
    void 포스트삭제() throws  Exception {
        mockMvc.perform(delete("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk());
    }
    @Test
    @WithAnonymousUser
    void 포스트삭제시로그인하지않은경우() throws  Exception {
        mockMvc.perform(delete("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser
    void 포스트삭제시작성자와_삭제요청자가_다를경우() throws  Exception {
      //mocking
        doThrow(new SnsApplicationException(ErrorCode.INVALID_PERMISSION)).when(postService).delete(any(),any());
        mockMvc.perform(delete("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 포스트삭제시삭제하려는포스트가존재하지않는경우() throws  Exception {
        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).delete(any(),any());


        mockMvc.perform(delete("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser
    void 피드목록() throws  Exception {
        // TODO:mocking
        when(postService.list(any())).thenReturn(Page.empty());
        mockMvc.perform(get("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk());
    }
    @Test
    @WithAnonymousUser
    void 피드목록요청시로그인하지않은경우() throws  Exception {
        // TODO:mocking
        when(postService.list(any())).thenReturn(Page.empty());
        mockMvc.perform(delete("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser
    void 내피드목록() throws  Exception {
        // TODO:mocking
        when(postService.my(any(),any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts/my")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk());
    }
    @Test
    @WithAnonymousUser
    void 내피드목록요청시로그인하지않은경우() throws  Exception {
        // TODO:mocking
        when(postService.my(any(),any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts/my")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser
    void 좋아요기능() throws  Exception {

        mockMvc.perform(post("/api/v1/posts/1/likes")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isOk());
    }
    @Test
    @WithAnonymousUser
    void 좋아요버튼클릭시로그인하지않은경우() throws  Exception {
        mockMvc.perform(post("/api/v1/posts/1/likes")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isUnauthorized());
    }
    @Test
    @WithAnonymousUser
    void 좋아요버튼클릭시게시물이없는경우() throws  Exception {
        // TODO:mocking
       doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).like(any(),any());

        mockMvc.perform(post("/api/v1/posts/1/likes")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print()).andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser
    void 댓글기능() throws  Exception {

        mockMvc.perform(post("/api/v1/posts/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))
        ).andDo(print()).andExpect(status().isOk());
    }
    @Test
    @WithAnonymousUser
    void 댓글작성시로그인하지않은경우() throws  Exception {
        mockMvc.perform(post("/api/v1/posts/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))
        ).andDo(print()).andExpect(status().isUnauthorized());
    }
    @Test
    @WithAnonymousUser
    void 댓글작성시게시물이없는경우() throws  Exception {
        // TODO:mocking
        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).comment(any(),any(),any());

        mockMvc.perform(post("/api/v1/posts/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))
        ).andDo(print()).andExpect(status().isNotFound());
    }
}
