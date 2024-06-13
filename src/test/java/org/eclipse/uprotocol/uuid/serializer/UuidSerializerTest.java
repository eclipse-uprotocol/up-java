package org.eclipse.uprotocol.uuid.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.uprotocol.v1.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UuidSerializerTest {
    @Test
    @DisplayName("Test serializer with good uuid")
    void test_serializer_with_good_uuid() {
        final String uuidStr = "123e4567-e89b-12d3-a456-426614174000";
        final UUID uuid = UuidSerializer.deserialize(uuidStr);
        
        assertEquals(uuidStr, UuidSerializer.serialize(uuid));
    }

    @Test
    @DisplayName("Test serializer with null uuid")
    void test_serializer_with_null_uuid() {
        final UUID uuid = null;
        assertEquals("", UuidSerializer.serialize(uuid));
    }

    @Test
    @DisplayName("Test serializer with empty uuid")
    void test_serializer_with_empty_uuid() {
        final UUID uuid = UUID.getDefaultInstance();
        assertEquals("00000000-0000-0000-0000-000000000000", UuidSerializer.serialize(uuid));
    }

    @Test
    @DisplayName("Test deserializer with invalid uuid")
    void test_deserializer_with_invalid_uuid() {
        final String uuidStr = "sdsadfasdfsfgagASDfadasfgsdfgs";
        final UUID uuid = UuidSerializer.deserialize(uuidStr);
        assertEquals(UUID.getDefaultInstance(), uuid);
    }
    
}
