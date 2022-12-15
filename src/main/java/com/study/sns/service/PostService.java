package com.study.sns.service;


import com.study.sns.exception.ErrorCode;
import com.study.sns.exception.SnsApplicationException;
import com.study.sns.model.AlarmArgs;
import com.study.sns.model.AlarmType;
import com.study.sns.model.Comment;
import com.study.sns.model.Post;
import com.study.sns.model.entity.*;
import com.study.sns.model.event.AlarmEvent;
import com.study.sns.producer.AlarmProducer;
import com.study.sns.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostEntityRepository postEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final LikeEntityRepository likeEntityRepository;
    private final CommentEntityRepository commentEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private  final  AlarmService alarmService;
    private  final AlarmProducer alarmProducer;

    @Transactional
    public void create(String title, String body, String userName) {
        //user find
        UserEntity userEntity = getUserEntityOrException(userName);
        //post save
        PostEntity saved = postEntityRepository.save(PostEntity.of(title, body, userEntity));
    }

    @Transactional
    public Post modify(String title, String body, String userName, Integer postId) {
        UserEntity userEntity =getUserEntityOrException(userName);
        //post exist
        PostEntity postEntity = getPostEntityOrException(postId);
        //post permission
        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s has no permission with %s", userName, postId));
        }
        postEntity.setTitle(title);
        postEntity.setBody(body);

        return Post.fromEntity(postEntityRepository.saveAndFlush(postEntity));
    }

    @Transactional
    public void delete(String userName, Integer postId) {
        UserEntity userEntity =getUserEntityOrException(userName);
        //post exist
        PostEntity postEntity = getPostEntityOrException(postId);
        //post permission
        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s has no permission with %s", userName, postId));
        }
        likeEntityRepository.deleteAllByPost(postEntity);
        commentEntityRepository.deleteAllByPost(postEntity);
        postEntityRepository.delete(postEntity);
    }

    public Page<Post> list(Pageable pageable) {
        return postEntityRepository.findAll(pageable).map(Post::fromEntity);
    }

    public Page<Post> my(String userName, Pageable pageable) {
      UserEntity userEntity = getUserEntityOrException(userName);
      return postEntityRepository.findAllByUser(userEntity,pageable).map(Post::fromEntity);
    }

    @Transactional
    public void like(Integer postId, String userName) {
        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);
        //check like ->throw

        likeEntityRepository.findByUserAndPost(userEntity, postEntity).ifPresent(it -> {
            throw new SnsApplicationException(ErrorCode.ALREADY_LIKED, String.format("userName %s already like post %d", userName, postId));
        });

        //like save
        likeEntityRepository.save(LikeEntity.of(userEntity, postEntity));
        alarmProducer.send(new AlarmEvent(postEntity.getUser().getId(), AlarmType.NEW_LIKE_ON_POST,new AlarmArgs(userEntity.getId(),postEntity.getId())));
    }

    public long likeCount(Integer postId) {

        PostEntity postEntity =getPostEntityOrException(postId);
        //count like
        return likeEntityRepository.countByPost(postEntity);
    }
    @Transactional
    public void comment(Integer postId, String userName,String comment){
        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);

        //comment save
        commentEntityRepository.save(CommentEntity.of(userEntity,postEntity,comment));

       AlarmEntity alarmEntity = alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_COMMENT_ON_POST,new AlarmArgs(userEntity.getId(),postEntity.getId())));
        alarmProducer.send(new AlarmEvent(postEntity.getUser().getId(), AlarmType.NEW_COMMENT_ON_POST,new AlarmArgs(userEntity.getId(),postEntity.getId())));

    }

    public Page<Comment> getComments(Integer postId, Pageable pageable){
        PostEntity postEntity = getPostEntityOrException(postId);
           return commentEntityRepository.findAllByPost(postEntity,pageable).map(Comment::fromEntity);
    }

    //postexist
    private PostEntity getPostEntityOrException(Integer postId){
        return  postEntityRepository.findById(postId).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("%s not found", postId)));
    }

    //userexist
    private UserEntity getUserEntityOrException(String userName){
        return userEntityRepository.findByUserName(userName).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", userName)));
    }
}

