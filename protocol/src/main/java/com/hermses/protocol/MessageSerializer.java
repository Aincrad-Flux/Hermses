package com.hermses.protocol;

public interface MessageSerializer {
    String serialize(Message message) throws Exception;
    Message deserialize(String payload) throws Exception;
}
