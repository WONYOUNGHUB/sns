package com.study.sns.model;

import com.study.sns.model.entity.CommentEntity;
import com.study.sns.model.entity.PostEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.criteria.CriteriaBuilder;
import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class Comment {
    private  Integer id;

    private String comment;

    private String userName;

    private Integer postId;

    private Timestamp registeredAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;


    public static Comment fromEntity(CommentEntity entity){
        return new Comment(
                entity.getId(),
                entity.getComment(),
                entity.getUser().getUserName(),
                entity.getPost().getId(),
                entity.getRegisteredAt(),
                entity.getDeletedAt(),
                entity.getUpdatedAt()
        );
    }


}
