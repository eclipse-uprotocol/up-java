package org.eclipse.uprotocol.transport.datamodel;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.eclipse.uprotocol.transport.datamodel.UStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UStatusTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(UStatus.class)
                .suppress(Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the equals and hash code for ok scenarios")
    public void testHashCodeEquals_ok_scenarios() {
        // Sets call equals and hash code - using them to test functionality
        Set<Object> statuses = new HashSet<>();
        statuses.add(UStatus.ok());
        statuses.add(UStatus.ok());
        statuses.add(UStatus.ok("ackId"));
        statuses.add("5");

        assertEquals(3, statuses.size());
    }

    @Test
    @DisplayName("Make sure the equals and hash code for fail scenarios")
    public void testHashCodeEquals_fail_scenarios() {
        // Sets call equals and hash code - using them to test functionality
        Set<Object> statuses = new HashSet<>();
        // these two are the same
        statuses.add(UStatus.failed());
        statuses.add(UStatus.failed());

        // these two are the same
        statuses.add(UStatus.failed("boom"));
        statuses.add(UStatus.failed("boom", UStatus.Code.UNKNOWN));

        statuses.add(UStatus.failed("bam"));
        statuses.add(UStatus.failed("boom", UStatus.Code.UNSPECIFIED.value()));
        statuses.add(UStatus.failed("boom", UStatus.Code.INVALID_ARGUMENT));
        statuses.add("5");

        assertEquals(6, statuses.size());
    }

    @Test
    @DisplayName("Make sure the toString works on ok status")
    public void testToString_for_ok_status() {
        UStatus ok = UStatus.ok();
        assertEquals("UStatus ok id=ok code=0", ok.toString());
    }

    @Test
    @DisplayName("Make sure the toString works on ok status with Id")
    public void testToString_for_ok_status_with_id() {
        UStatus ok = UStatus.ok("boo");
        assertEquals("UStatus ok id=boo code=0", ok.toString());
    }

    @Test
    @DisplayName("Make sure the toString works on failed status")
    public void testToString_for_failed_status() {
        UStatus failed = UStatus.failed();
        assertEquals("UStatus failed msg=failed code=2", failed.toString());
    }

    @Test
    @DisplayName("Make sure the toString works on failed status with message")
    public void testToString_for_failed_status_with_message() {
        UStatus failed = UStatus.failed("boom");
        assertEquals("UStatus failed msg=boom code=2", failed.toString());
    }

    @Test
    @DisplayName("Make sure the toString works on failed status with message and failure reason")
    public void testToString_for_failed_status_with_message_and_failure_reason() {
        UStatus failed = UStatus.failed("boom", UStatus.Code.INVALID_ARGUMENT.value());
        assertEquals("UStatus failed msg=boom code=3", failed.toString());
    }

    @Test
    @DisplayName("Make sure the toString works on failed status with message and Code")
    public void testToString_for_failed_status_with_message_and_code() {
        UStatus failed = UStatus.failed("boom", UStatus.Code.INVALID_ARGUMENT);
        assertEquals("UStatus failed msg=boom code=3", failed.toString());
    }

    @Test
    @DisplayName("Create ok status")
    public void create_ok_status() {
        UStatus ok = UStatus.ok();
        assertTrue(ok.isSuccess());
        assertFalse(ok.isFailed());
        assertEquals("ok", ok.msg());
        assertEquals(0, ok.getCode());
    }

    @Test
    @DisplayName("Create ok status with Id")
    public void create_ok_status_with_id() {
        UStatus ok = UStatus.ok("boo");
        assertTrue(ok.isSuccess());
        assertFalse(ok.isFailed());
        assertEquals("boo", ok.msg());
        assertEquals(0, ok.getCode());
    }

    @Test
    @DisplayName("Create failed status")
    public void create_failed_status() {
        UStatus failed = UStatus.failed();
        assertFalse(failed.isSuccess());
        assertTrue(failed.isFailed());
        assertEquals("failed", failed.msg());
        assertEquals(2, failed.getCode());
    }

    @Test
    @DisplayName("Create failed status with message")
    public void create_failed_status_with_message() {
        UStatus failed = UStatus.failed("boom");
        assertFalse(failed.isSuccess());
        assertTrue(failed.isFailed());
        assertEquals("boom", failed.msg());
        assertEquals(2, failed.getCode());
    }

    @Test
    @DisplayName("Create failed status with message and failure reason")
    public void create_failed_status_with_message_and_failure_reason() {
        UStatus failed = UStatus.failed("boom", UStatus.Code.INVALID_ARGUMENT.value());
        assertFalse(failed.isSuccess());
        assertTrue(failed.isFailed());
        assertEquals("boom", failed.msg());
        assertEquals(3, failed.getCode());
    }

    @Test
    @DisplayName("Create failed status with message and Code")
    public void create_failed_status_with_message_and_code() {
        UStatus failed = UStatus.failed("boom", UStatus.Code.INVALID_ARGUMENT);
        assertFalse(failed.isSuccess());
        assertTrue(failed.isFailed());
        assertEquals("boom", failed.msg());
        assertEquals(3, failed.getCode());
    }

    @Test
    @DisplayName("Code from a known int code")
    public void code_from_a_known_int_code() {
        final Optional<UStatus.Code> code = UStatus.Code.from(4);
        assertTrue(code.isPresent());
        assertEquals("DEADLINE_EXCEEDED", code.get().name());
    }

    @Test
    @DisplayName("Code from a unknown int code")
    public void code_from_a_unknown_int_code() {
        final Optional<UStatus.Code> code = UStatus.Code.from(299);
        assertTrue(code.isEmpty());
    }

    @Test
    @DisplayName("Code from a known google code")
    public void code_from_a_known_google_code() {
        final Optional<UStatus.Code> code = UStatus.Code.from(com.google.rpc.Code.INVALID_ARGUMENT);
        assertTrue(code.isPresent());
        assertEquals("INVALID_ARGUMENT", code.get().name());
    }

    @Test
    @DisplayName("Code from a null google code")
    public void code_from_a_null_google_code() {
        final Optional<UStatus.Code> code = UStatus.Code.from(null);
        assertTrue(code.isEmpty());
    }

    @Test
    @DisplayName("Code from a UNRECOGNIZED google code")
    public void code_from_a_UNRECOGNIZED_google_code() {
        final Optional<UStatus.Code> code = UStatus.Code.from(com.google.rpc.Code.UNRECOGNIZED);
        assertTrue(code.isEmpty());
    }

}