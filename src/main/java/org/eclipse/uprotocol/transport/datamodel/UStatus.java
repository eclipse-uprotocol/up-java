package org.eclipse.uprotocol.transport.datamodel;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * UProtocol general status for all operations.
 * A UStatus is generated using the static factory methods, making is easy to quickly create UStatus objects.
 * Example: UStatus ok = UStatus.ok();
 */
public abstract class UStatus {

    private static final String OK = "ok";
    private static final String FAILED = "failed";

    public abstract boolean isSuccess();
    public abstract String msg();
    public abstract int getCode();
    
    /**
     * Enum to contain the status code that we map to google.rpc.Code.
     * Please refer to <a href="https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto">code.proto</a>
     * for documentation on the codes listed below
     *
     */
    public enum Code {

        OK (com.google.rpc.Code.OK_VALUE),
        CANCELLED (com.google.rpc.Code.CANCELLED_VALUE),
        UNKNOWN (com.google.rpc.Code.UNKNOWN_VALUE),
        INVALID_ARGUMENT (com.google.rpc.Code.INVALID_ARGUMENT_VALUE),
        DEADLINE_EXCEEDED (com.google.rpc.Code.DEADLINE_EXCEEDED_VALUE),
        NOT_FOUND (com.google.rpc.Code.NOT_FOUND_VALUE),
        ALREADY_EXISTS (com.google.rpc.Code.ALREADY_EXISTS_VALUE),
        PERMISSION_DENIED (com.google.rpc.Code.PERMISSION_DENIED_VALUE),
        UNAUTHENTICATED (com.google.rpc.Code.UNAUTHENTICATED_VALUE),
        RESOURCE_EXHAUSTED (com.google.rpc.Code.RESOURCE_EXHAUSTED_VALUE),
        FAILED_PRECONDITION (com.google.rpc.Code.FAILED_PRECONDITION_VALUE),
        ABORTED (com.google.rpc.Code.ABORTED_VALUE),
        OUT_OF_RANGE (com.google.rpc.Code.OUT_OF_RANGE_VALUE),
        UNIMPLEMENTED (com.google.rpc.Code.UNIMPLEMENTED_VALUE),
        INTERNAL (com.google.rpc.Code.INTERNAL_VALUE),
        UNAVAILABLE (com.google.rpc.Code.UNAVAILABLE_VALUE),
        DATA_LOSS (com.google.rpc.Code.DATA_LOSS_VALUE),
        UNSPECIFIED (-1);

        private final int value;
        Code (int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        /**
         * Get the Code from an integer value.
         * @param value The integer value of the Code.
         * @return Returns the Code if found, otherwise returns Optional.empty().
         */
        public static Optional<Code> from(int value) {
            return Arrays.stream(Code.values())
                    .filter(p -> p.value() == value)
                    .findAny();
        }


        /**
         * Get the Code from a google.rpc.Code.
         * @param code The google.rpc.Code.
         * @return Returns the Code if found, otherwise returns Optional.empty().
         */
        public static Optional<Code> from(com.google.rpc.Code code) {
            if (code == null || code == com.google.rpc.Code.UNRECOGNIZED) {
                return Optional.empty();
            }
            return Arrays.stream(Code.values())
                    .filter(p -> p.value() == code.getNumber())
                    .findAny();
        }
    }

    /**
     * Return true if UStatus is a failure
     * @return Returns true if the UStatus is successful.
     */
    public boolean isFailed() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return String.format("UStatus %s %s%s code=%s", isSuccess() ? "ok" : "failed",
                isSuccess() ? "id=" : "msg=", msg(),
                getCode());
    }

    /**
     * A successful UStatus.
     */
    private static class OKSTATUS extends UStatus {

        /**
         * A successful status could contain an id for tracking purposes.
         */
        private final String id;

        private OKSTATUS(String id) {
            this.id = id;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        public int getCode() {
            return Code.OK.value();
        }

        @Override
        public String msg() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OKSTATUS okstatus = (OKSTATUS) o;
            return Objects.equals(id, okstatus.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

    }


    /**
     * A failed UStatus.
     */
    private static class FAILSTATUS extends UStatus {

        private final String failMsg;
       
        private final Code code;

        private FAILSTATUS(String failMsg, Code code) {
            this.failMsg = failMsg;
            this.code = code;
        }

        private FAILSTATUS(String failMsg, int value) {
            Optional<Code> code = Code.from(value);
            this.failMsg = failMsg;
            this.code = code.orElse(Code.UNSPECIFIED);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String msg() {
            return this.failMsg;
        }

        @Override
        public int getCode() {
            return code.value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FAILSTATUS that = (FAILSTATUS) o;
            return Objects.equals(failMsg, that.failMsg) && code == that.code;
        }

        @Override
        public int hashCode() {
            return Objects.hash(failMsg, code);
        }
    }


    public static UStatus ok() {
        return new OKSTATUS(UStatus.OK);
    }

    public static UStatus ok(String ackId) {
        return new OKSTATUS(ackId);
    }

    public static UStatus failed() {
        return new FAILSTATUS(FAILED, Code.UNKNOWN.value());
    }

    public static UStatus failed(String msg) {
        return new FAILSTATUS(msg, Code.UNKNOWN.value());
    }

    public static UStatus failed(String msg, int failureReason) {
        return new FAILSTATUS(msg, failureReason);
    }

    public static UStatus failed(String msg, Code code) {
        return new FAILSTATUS(msg, code);
    }

}
