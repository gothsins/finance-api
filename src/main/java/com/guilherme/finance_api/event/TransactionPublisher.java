package com.guilherme.finance_api.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    public void publish(TransactionEvent event) {
        log.info("📤 Publicando evento na fila: transação '{}' do usuário {}",
                event.getDescription(), event.getUserEmail());

        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        log.info("✅ Evento publicado com sucesso.");
    }
}