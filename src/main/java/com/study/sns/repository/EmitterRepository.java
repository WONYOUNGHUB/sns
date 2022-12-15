package com.study.sns.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EmitterRepository {

    //로컬캐싱
    private Map<String, SseEmitter>emitterMap = new HashMap<>();

    public  SseEmitter save(Integer userId, SseEmitter sseEmitter){
        final String key = getKey(userId);
        emitterMap.put(key,sseEmitter);
        log.info("Set sseEmitter {}",userId);
        return sseEmitter;
    }

    public Optional<SseEmitter> get(Integer userId){
        final  String key = getKey(userId);
        emitterMap.get(key);
        log.info("Get sseEmitter{}",userId);
        return Optional.of(emitterMap.get(key));
    }
    public void delete(Integer userId){
       emitterMap.remove(getKey(userId));
    }
    private  String getKey(Integer userId ){
        return "Emitter:UID"+userId;
    }

}
