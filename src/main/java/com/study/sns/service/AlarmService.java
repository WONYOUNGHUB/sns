package com.study.sns.service;


import com.study.sns.exception.ErrorCode;
import com.study.sns.exception.SnsApplicationException;
import com.study.sns.model.AlarmArgs;
import com.study.sns.model.AlarmType;
import com.study.sns.model.entity.AlarmEntity;
import com.study.sns.model.entity.UserEntity;
import com.study.sns.repository.AlarmEntityRepository;
import com.study.sns.repository.EmitterRepository;
import com.study.sns.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private  final static Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private  final  static  String ALARM_NAME="alarm";

    private  final EmitterRepository emitterRepository;
    private  final AlarmEntityRepository alarmEntityRepository;
    private  final UserEntityRepository userEntityRepository;

        public  void  send(AlarmType type, AlarmArgs arg,Integer receiverUserId){
         UserEntity user = userEntityRepository.findById(receiverUserId).orElseThrow(()-> new SnsApplicationException(ErrorCode.USER_NOT_FOUND));
             //alarm save
            AlarmEntity alarmEntity = alarmEntityRepository.save(AlarmEntity.of(user,type,arg));

            emitterRepository.get(receiverUserId).ifPresentOrElse(sseEmitter -> {
                try {
                    sseEmitter.send(SseEmitter.event().id(alarmEntity.getId().toString()).name(ALARM_NAME).data("new alarm"));
                }catch (IOException e){
                    emitterRepository.delete(receiverUserId);
                    throw  new SnsApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
                }
               },
                    () -> log.info("No emitter founded")
            );
        }


        public SseEmitter connectAlarm(Integer userId){
            SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
            emitterRepository.save(userId,sseEmitter);
            sseEmitter.onCompletion(()->emitterRepository.delete(userId));
            sseEmitter.onTimeout(()->emitterRepository.delete(userId));


            try{
                sseEmitter.send(SseEmitter.event().id("").name(ALARM_NAME).data("connect complete"));


            }catch (IOException exception){
                throw new SnsApplicationException(ErrorCode.ALARM_CONNECT_ERROR);

            }
                return sseEmitter;
        }


}
