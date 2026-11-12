package com.mightyfin.WalletService.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration 
@ConditionalOnProperty(prefix = "wallet.outbox", name = "publisher-enabled", havingValue = "true")
public class KafkaTopicConfig {

    @Bean
    public NewTopic walletEventsTopic(OutboxProperties props) {
        return TopicBuilder.name(props.getTopic()).partitions(3).replicas(1).build();
    }
}
