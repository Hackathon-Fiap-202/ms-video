package com.nextimefood.msvideo.application.ports.outgoing;

public interface MessagePublisherPort {

    void publish(String queueName, Object payload);

}
