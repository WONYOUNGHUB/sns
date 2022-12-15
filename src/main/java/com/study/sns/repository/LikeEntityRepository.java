package com.study.sns.repository;


import com.study.sns.model.Post;
import com.study.sns.model.entity.LikeEntity;
import com.study.sns.model.entity.PostEntity;
import com.study.sns.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface LikeEntityRepository extends JpaRepository<LikeEntity,Integer> {

    Optional<LikeEntity> findByUserAndPost(UserEntity user, PostEntity post);

    List<LikeEntity>findAllByPost(PostEntity post);

//    @Query(value = "SELECT COUNT(*) FROM LikeEntity entity WHERE entity.post =: post")
//    Integer countByPost(@Param("post") PostEntity post);

    long countByPost(PostEntity post);

    @Transactional
    @Modifying
    @Query("UPDATE LikeEntity entity SET deleted_at = NOW() where entity.post = :post")
    void deleteAllByPost(@Param("post") PostEntity postEntity);
    //JPA에서 영속성관리(라이프사이클) 체크를 하게되는데 deleteALl 같은경우 데이터를 가져와서 삭제하는것이기떄문에 비효율적 이게 아닌 삭제쿼리를 날리기만하면됨
    //이경우에는 쿼리를 따로 작성하는것이 효율적이다.
}
