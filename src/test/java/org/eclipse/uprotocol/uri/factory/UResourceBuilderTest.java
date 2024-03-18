package org.eclipse.uprotocol.uri.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.uprotocol.UServiceTopic;
import org.eclipse.uprotocol.v1.UResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UResourceBuilderTest {
    
    @Test
    @DisplayName("Test fromId with valid id")
    public void test_from_id_valid_id() {
        int id = 0;
        UResource resource = UResourceBuilder.fromId(id);
        assertEquals(resource.getName(), "rpc");
        assertEquals(resource.getInstance(), "response");
        assertEquals(resource.getId(), 0);
    }

    @Test
    @DisplayName("Test fromId with invalid id")
    public void test_from_id_invalid_id() {
        int id = -1;
        UResource resource = UResourceBuilder.fromId(id);
        assertEquals(resource, UResource.getDefaultInstance());
    }

    @Test
    @DisplayName("Test fromId with valid id that is below MIN_TOPIC_ID")
    public void test_from_id_valid_id_below_min_topic_id() {
        int id = 0x7FFF;
        UResource resource = UResourceBuilder.fromId(id);
        assertEquals(resource.getName(), "rpc");
        assertEquals(resource.getInstance(), "");
        assertEquals(resource.getId(), 0x7FFF);
    }

    @Test
    @DisplayName("Test fromId with valid id that is above MIN_TOPIC_ID")
    public void test_from_id_valid_id_above_min_topic_id() {
        int id = 0x8000;
        UResource resource = UResourceBuilder.fromId(id);
        assertEquals(resource.getName(), "");
        assertEquals(resource.getInstance(), "");
        assertEquals(resource.getId(), 0x8000);
    }

    @Test
    @DisplayName("Test fromUServiceTopic with valid service topic")
    public void test_from_uservice_topic_valid_service_topic() {
        UServiceTopic topic = UServiceTopic.newBuilder()
            .setName("SubscriptionChange")
            .setId(0)
            .setMessage("Update")
            .build();
        UResource resource = UResourceBuilder.fromUServiceTopic(topic);
        assertEquals(resource.getName(), "SubscriptionChange");
        assertEquals(resource.getInstance(), "");
        assertEquals(resource.getId(), 0);
        assertEquals(resource.getMessage(), "Update");
    }

    @Test
    @DisplayName("Test fromUServiceTopic with valid service topic that includes instance")
    public void test_from_uservice_topic_valid_service_topic_with_instance() {
        UServiceTopic topic = UServiceTopic.newBuilder()
            .setName("door.front_left")
            .setId(0x8000)
            .setMessage("Door")
            .build();
        UResource resource = UResourceBuilder.fromUServiceTopic(topic);
        assertEquals(resource.getName(), "door");
        assertEquals(resource.getInstance(), "front_left");
        assertEquals(resource.getId(), 0x8000);
        assertEquals(resource.getMessage(), "Door");
    }


    @Test
    @DisplayName("Test fromUServiceTopic with invalid service topic")
    public void test_from_uservice_topic_invalid_service_topic() {
        UServiceTopic topic = null;
        try {
            UResource resource = UResourceBuilder.fromUServiceTopic(topic);
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), "topic cannot be null");
        }
    }

}
