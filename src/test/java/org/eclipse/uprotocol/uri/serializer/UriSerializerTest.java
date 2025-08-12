/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.uri.serializer;

import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


public class UriSerializerTest {


    @Test
    @DisplayName("Test using the serializers")
    public void testUsingTheSerializers() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .setUeId(1)
                .setUeVersionMajor(2)
                .setResourceId(3)
                .build();

        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/1/2/3", serializedUri);
    }

    @Test
    @DisplayName("Test deserializing a null UUri")
    public void testDeserializingANullUuri() {
        UUri uri = UriSerializer.deserialize(null);
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing an empty UUri")
    public void testDeserializingAnEmptyUuri() {
        UUri uri = UriSerializer.deserialize("");
        assertTrue(UriValidator.isEmpty(uri));
    }


    @Test
    @DisplayName("Test deserializing a blank UUri")
    public void testDeserializingABlankUuri() {
        UUri uri = UriSerializer.deserialize("  ");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing with a valid URI that has scheme")
    public void testDeserializingWithAValidUriThatHasScheme() {
        UUri uri = UriSerializer.deserialize("up://myAuthority/1/2/3");
        assertEquals("myAuthority", uri.getAuthorityName());
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(3, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing with a valid URI that has scheme but nothing else")
    public void testDeserializingWithAValidUriThatHasSchemeButNothingElse() {
        UUri uri = UriSerializer.deserialize("up://");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with all fields")
    public void testDeserializingAValidUuriWithAllFields() {
        UUri uri = UriSerializer.deserialize("//myAuthority/1/2/3");
        assertEquals("myAuthority", uri.getAuthorityName());
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(3, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with only authority")
    public void testDeserializingAValidUuriWithOnlyAuthority() {
        UUri uri = UriSerializer.deserialize("//myAuthority");
        assertEquals("myAuthority", uri.getAuthorityName());
        assertEquals(0, uri.getUeId());
        assertEquals(0, uri.getUeVersionMajor());
        assertEquals(0, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with only authority and ueId")
    public void testDeserializingAValidUuriWithOnlyAuthorityAndUeid() {
        UUri uri = UriSerializer.deserialize("//myAuthority/1");
        assertEquals("myAuthority", uri.getAuthorityName());
        assertEquals(1, uri.getUeId());
        assertEquals(0, uri.getUeVersionMajor());
        assertEquals(0, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with only authority, ueId and ueVersionMajor")
    public void testDeserializingAValidUuriWithOnlyAuthorityUeidAndUeversionmajor() {
        UUri uri = UriSerializer.deserialize("//myAuthority/1/2");
        assertEquals("myAuthority", uri.getAuthorityName());
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(0, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing a string with invalid characters at the beginning")
    public void testDeserializingAStringWithInvalidCharacters() {
        UUri uri = UriSerializer.deserialize("$$");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with names instead of ids for UeId")
    public void testDeserializingAStringWithNamesInsteadOfIdsForUeid() {
        UUri uri = UriSerializer.deserialize("//myAuthority/myUeId/2/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with names instead of ids for UeVersionMajor")
    public void testDeserializingAStringWithNamesInsteadOfIdsForUeversionmajor() {
        UUri uri = UriSerializer.deserialize("//myAuthority/1/myUeVersionMajor/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with names instead of ids for ResourceId")
    public void testDeserializingAStringWithNamesInsteadOfIdsForResourceid() {
        UUri uri = UriSerializer.deserialize("//myAuthority/1/2/myResourceId");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string without authority")
    public void testDeserializingAStringWithoutAuthority() {
        UUri uri = UriSerializer.deserialize("/1/2/3");
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(3, uri.getResourceId());
        assertTrue(uri.getAuthorityName().isBlank());
    }

    @Test
    @DisplayName("Test deserializing a string without authority and ResourceId")
    public void testDeserializingAStringWithoutAuthorityAndResourceid() {
        UUri uri = UriSerializer.deserialize("/1/2");
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(0, uri.getResourceId());
        assertTrue(uri.getAuthorityName().isBlank());
    }

    @Test
    @DisplayName("Test deserializing a string without authority, ResourceId and UeVersionMajor")
    public void testDeserializingAStringWithoutAuthorityResourceidAndUeversionmajor() {
        UUri uri = UriSerializer.deserialize("/1");
        assertEquals(1, uri.getUeId());
        assertEquals(0, uri.getUeVersionMajor());
        assertEquals(0, uri.getResourceId());
        assertTrue(uri.getAuthorityName().isBlank());
    }

    @Test
    @DisplayName("Test deserializing a string with blank authority")
    public void testDeserializingAStringWithBlankAuthority() {
        UUri uri = UriSerializer.deserialize("///2");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with all the items are the wildcard values")
    public void testDeserializingAStringWithAllTheItemsAreTheWildcardValues() {
        UUri uri = UriSerializer.deserialize("//*/FFFF/ff/ffff");
        assertEquals("*", uri.getAuthorityName());
        assertEquals(0xFFFF, uri.getUeId());
        assertEquals(0xFF, uri.getUeVersionMajor());
        assertEquals(0xFFFF, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing a string with uEId() out of range")
    public void testDeserializingAStringWithUeidOutOfRange() {
        UUri uri = UriSerializer.deserialize("/fffffffff/2/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with uEVersionMajor out of range")
    public void testDeserializingAStringWithUeversionmajorOutOfRange() {
        UUri uri = UriSerializer.deserialize("/1/256/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with resourceId out of range")
    public void testDeserializingAStringWithResourceidOutOfRange() {
        UUri uri = UriSerializer.deserialize("/1/2/65536");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with negative uEId")
    public void testDeserializingAStringWithNegativeUeid() {
        UUri uri = UriSerializer.deserialize("/-1/2/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with negative uEVersionMajor")
    public void testDeserializingAStringWithNegativeUeversionmajor() {
        UUri uri = UriSerializer.deserialize("/1/-2/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with negative resourceId")
    public void testDeserializingAStringWithNegativeResourceid() {
        UUri uri = UriSerializer.deserialize("/1/2/-3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with wildcard ResourceId")
    public void testDeserializingAStringWithWildcardResourceid() {
        UUri uri = UriSerializer.deserialize("/1/2/ffff");
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(0xFFFF, uri.getResourceId());
    }

    @Test
    @DisplayName("Test serializing an Empty UUri")
    public void testSerializingAnEmptyUuri() {
        UUri uri = UUri.getDefaultInstance();
        String serializedUri = UriSerializer.serialize(uri);
        assertTrue(serializedUri.isBlank());
    }

    @Test
    @DisplayName("Test serializing a null UUri")
    public void testSerializingANullUuri() {
        String serializedUri = UriSerializer.serialize(null);
        assertTrue(serializedUri.isBlank());
    }

    @Test
    @DisplayName("Test serializing a full UUri")
    public void testSerializingAFullUuri() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .setUeId(1)
                .setUeVersionMajor(2)
                .setResourceId(3)
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/1/2/3", serializedUri);
    }

    @Test
    @DisplayName("Test serializing a UUri with only authority")
    public void testSerializingAUuriWithOnlyAuthority() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/0/0/0", serializedUri);
    }

    @Test
    @DisplayName("Test serializing a UUri with only authority and ueId")
    public void testSerializingAUuriWithOnlyAuthorityAndUeid() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .setUeId(1)
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/1/0/0", serializedUri);
    }

    @Test
    @DisplayName("Test serializing a UUri with only authority, ueId and ueVersionMajor")
    public void testSerializingAUuriWithOnlyAuthorityUeidAndUeversionmajor() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .setUeId(1)
                .setUeVersionMajor(2)
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/1/2/0", serializedUri);
    }

    @Test
    @DisplayName("Test serializing a UUri with only authority, ueId, ueVersionMajor and resourceId")
    public void testSerializingAUuriWithOnlyAuthorityUeidUeversionmajorAndResourceid() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .setUeId(1)
                .setUeVersionMajor(2)
                .setResourceId(3)
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/1/2/3", serializedUri);
    }

    @Test
    @DisplayName("Test serializing a UUri that has a blank authority")
    public void testSerializingAUuriThatHasABlankAuthority() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("")
                .setUeId(1)
                .setUeVersionMajor(2)
                .setResourceId(3)
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("/1/2/3", serializedUri);
    }
}
