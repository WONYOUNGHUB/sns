package com.study.sns.consumer;


import com.study.sns.model.event.AlarmEvent;
import com.study.sns.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AlarmConsumer {
    private  final AlarmService alarmService;
    @KafkaListener(topics = "${spring.kafka.topic.alarm")
    public  void consumeAlarm(AlarmEvent event, Acknowledgment ack){
      log.info("Consume the event{}",event);
      alarmService.send(event.getAlarmType(),event.getArgs(),event.getReceiveUserId());
      ack.acknowledge();
    }
}
