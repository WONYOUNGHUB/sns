package com.study.sns.repository;

import com.study.sns.model.Comment;
import com.study.sns.model.Post;
import com.study.sns.model.entity.CommentEntity;
import com.study.sns.model.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface CommentEntityRepository extends JpaRepository<CommentEntity,Integer> {

    Page<CommentEntity> findAllByPost(PostEntity post, Pageable pageable);
    @Transactional
    @Modifying
    @Query("UPDATE CommentEntity entity SET deleted_At = NOW() where entity.post = :post")
    void deleteAllByPost(@Param("post") PostEntity postEntity);
    //JPA에서 영속성관리(라이프사이클) 체크를 하게되는데 deleteALl 같은경우 데이터를 가져와서 삭제하는것이기떄문에 비효율적 이게 아닌 삭제쿼리를 날리기만하면됨
    //이경우에는 쿼리를 따로 작성하는것이 효율적이다.
}
