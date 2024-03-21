package org.eclipse.uprotocol.uri.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpAddressTest {
    
    @Test
    @DisplayName("Test toBytes with null ipAddress")
    public void testToBytesWithNullIpAddress() {
        final byte[] bytes = IpAddress.toBytes(null);
        assertEquals(0, bytes.length);
    }

    @Test
    @DisplayName("Test toBytes with empty ipAddress")
    public void testToBytesWithEmptyIpAddress() {
        final byte[] bytes = IpAddress.toBytes("");
        assertEquals(0, bytes.length);
    }

    @Test
    @DisplayName("Test toBytes with invalid ipAddress")
    public void testToBytesWithInvalidIpAddress() {
        final byte[] bytes = IpAddress.toBytes("invalid");
        assertEquals(0, bytes.length);
    }

    @Test
    @DisplayName("Test toBytes with valid IPv4 address")
    public void testToBytesWithValidIPv4Address() {
        final byte[] bytes = IpAddress.toBytes("192.168.1.100");
        assertEquals(4, bytes.length);
        assertEquals(192, Byte.toUnsignedInt(bytes[0]));
        assertEquals(168, Byte.toUnsignedInt(bytes[1]));
        assertEquals(1, Byte.toUnsignedInt(bytes[2]));
        assertEquals(100, Byte.toUnsignedInt(bytes[3]));
    }

    @Test
    @DisplayName("Test toBytes with valid IPv6 address")
    public void testToBytesWithValidIPv6Address() throws UnknownHostException {
        final byte[] bytes = IpAddress.toBytes("2001:db8:85a3:0:0:8a2e:370:7334");
        
        assertEquals(InetAddress.getByAddress(bytes).getHostAddress(), "2001:db8:85a3:0:0:8a2e:370:7334");
    }

    @Test
    @DisplayName("Test isValid with null ipAddress")
    public void testIsValidWithNullIpAddress() {
        assertFalse(IpAddress.isValid(null));
    }

    @Test
    @DisplayName("Test isValid with empty ipAddress")
    public void testIsValidWithEmptyIpAddress() {
        assertFalse(IpAddress.isValid(""));
    }

    @Test
    @DisplayName("Test isValid with invalid ipAddress")
    public void testIsValidWithInvalidIpAddress() {
        assertFalse(IpAddress.isValid("invalid"));
    }

    @Test
    @DisplayName("Test isValid with valid IPv4 address")
    public void testIsValidWithValidIPv4Address() {
        assertTrue(IpAddress.isValid("192.168.1.100"));
    }

    @Test
    @DisplayName("Test isValid with valid IPv6 address")
    public void testIsValidWithValidIPv6Address() {
        assertTrue(IpAddress.isValid("2001:db8:85a3:0:0:8a2e:370:7334"));
    }

    @Test
    @DisplayName("Test isValid with invalid IPv4 address")
    public void testIsValidWithInvalidIPv4Address() {
        final byte[] bytes = IpAddress.toBytes("192.168.1.2586");
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid("192.168.1.2586"));
    }

    @Test
    @DisplayName("Test isValid with invalid IPv4 passing large number")
    public void testIsValidWithInvalidIPv4PassingLargeNumber() {
        final String ipString = "2875687346587326457836485623874658723645782364875623847562378465.1.1.abc";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);

        assertFalse(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid IPv4 passing negative value")
    public void testIsValidWithInvalidIPv4PassingNegative() {
        final String ipString = "-1.1.1.abc";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));

    }

    @Test
    @DisplayName("Test isValid with invalid IPv4 passing charaters")
    public void testIsValidWithInvalidIPv4PassingCharacters() {
        final String ipString = "1.1.1.abc";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));

    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address")
    public void testIsValidWithInvalidIPv6Address() {
        final String ipString = "ZX1:db8::";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));

    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address passing weird values")
    public void testIsValidWithInvalidIPv6AddressPassingWeirdValues() {
        final String ipString = "-1:ZX1:db8::";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));

    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address that has way too many groups")
    public void testIsValidWithInvalidIPv6AddressThatHasWayTooManyGroups() {
        final String ipString = "2001:db8:85a3:0:0:8a2e:370:7334:1234";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));

    }

    @Test
    @DisplayName("Test isValid with valid IPv6 address that has 8 groups")
    public void testIsValidWithValidIPv6AddressThatHas8Groups() {
        final String ipString = "2001:db8:85a3:0:0:8a2e:370:7334";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 16);
        assertTrue(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address with too many empty groups")
    public void testIsValidWithInvalidIPv6AddressWithTooManyEmptyGroups() {
        final String ipString = "2001::85a3::8a2e::7334";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));

    }

    @Test
    @DisplayName("Test isValid with valid IPv6 address with one empty group")
    public void testIsValidWithValidIPv6AddressWithOneEmptyGroup() {
        final String ipString = "2001:db8:85a3::8a2e:370:7334";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 16);
        assertTrue(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address that ends with a colon")
    public void testIsValidWithInvalidIPv6AddressThatEndsWithAColon() {
        final String ipString = "2001:db8:85a3::8a2e:370:7334:";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address that has doesn't have double colon and not enough groups")
    public void testIsValidWithInvalidIPv6AddressThatHasDoesntHaveDoubleColonAndNotEnoughGroups() {
        final String ipString = "2001:db8:85a3:0:0:8a2e:370";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with valid IPv6 address that ends with double colons")
    public void testIsValidWithValidIPv6AddressThatEndsWithDoubleColons() {
        final String ipString = "2001:db8:85a3:8a2e::";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 16);
        assertTrue(IpAddress.isValid(ipString));

    }

    @Test
    @DisplayName("Test isValid with all number values")
    public void testIsValidWithAllNumberValues() {
        final String ipString = "123:456:7890::";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 16);
        assertTrue(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with valid lowercase hexidecimal letters")
    public void testIsValidWithValidLowercaseHexidecimalLetters() {
        final String ipString = "abcd:ef12:3456::";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 16);
        assertTrue(IpAddress.isValid(ipString));
    }
    
    @Test
    @DisplayName("Test isValid with valid uppercase hexidecimal letters")
    public void testIsValidWithValidUppercaseHexidecimalLetters() {
        final String ipString = "ABCD:EF12:3456::";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 16);
        assertTrue(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid uppercase hexidecimal letters")
    public void testIsValidWithInvalidUppercaseHexidecimalLetters() {
        final String ipString = "ABCD:EFG2:3456::";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid hexidecimal letters")
    public void testIsValidWithInvalidLowercaseHexidecimalLetters() {
        final String ipString = "-C=[]:E{12g:3456";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid digit")
    public void testIsValidWithInvalidDigit1() {
        final String ipString = "aC=[]:E{12g:3456";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid digit")
    public void testIsValidWithInvalidDigit2() {
        final String ipString = "aCd[:E{12g:3456";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid digit")
    public void testIsValidWithInvalidDigit3() {
        final String ipString = "aCd:E{2g:3456";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address that has double colon and 8 groups")
    public void testIsValidWithInvalidIPv6AddressThatHasDoubleColonAnd8Groups() {
        final String ipString = "dead:beef:85a3::0:0:8a2e:370";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 16);
        assertTrue(IpAddress.isValid(ipString));
    }

    @Test
    @DisplayName("Test isValid with invalid IPv6 address that has only has 7 groups")
    public void testIsValidWithInvalidIPv6AddressThatHasOnlyHas7Groups() {
        final String ipString = "dead:beef:85a3:0:0:8a2e:370";
        final byte[] bytes = IpAddress.toBytes(ipString);
        assertEquals(bytes.length, 0);
        assertFalse(IpAddress.isValid(ipString));
    }
}
