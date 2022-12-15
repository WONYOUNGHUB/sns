package com.study.sns.repository;

import com.study.sns.model.entity.AlarmEntity;
import com.study.sns.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmEntityRepository extends JpaRepository<AlarmEntity,Integer> {
    Page<AlarmEntity> findAllByUserId(Integer userId, Pageable pageable);
}
