package org.eclipse.uprotocol.utransport;

import java.util.Objects;

/**
 * UProtocol general Ack requestRpcMessage for all operations.
 * Can be made into a Monad in the future.
 */
public abstract class Status {

    private static final String OK = "ok";
    private static final String FAILED = "failed";

    public abstract boolean isSuccess();
    public abstract String msg();

    public boolean isFailed() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return String.format("Ack %s %s %s", isSuccess() ? "ok" : "failed",
                isSuccess() ? "id =" : "msg=", msg());
    }

    private static class OKSTATUS extends Status {

        /**
         * A successful Ack can contain an id for tracking purposes.
         */
        private final String id;

        private OKSTATUS(String id) {
            this.id = id;
        }

        @Override
        public boolean isSuccess() {
            return true;
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
    private static class FAILSTATUS extends Status {

        private final String failMsg;

        // TODO make this into an Enum so Steven does not loose his panties
        private final int failureReason;

        private FAILSTATUS(String failMsg, int failureReason) {
            this.failMsg = failMsg;
            this.failureReason = failureReason;
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FAILSTATUS failstatus = (FAILSTATUS) o;
            return failureReason == failstatus.failureReason && Objects.equals(failMsg, failstatus.failMsg);
        }

        @Override
        public int hashCode() {
            return Objects.hash(failMsg, failureReason);
        }

    }

    public static Status ok() {
        return new OKSTATUS(Status.OK);
    }

    public static Status ok(String ackId) {
        return new OKSTATUS(ackId);
    }

    public static Status failed() {
        return new FAILSTATUS(FAILED, 0);
    }

    public static Status failed(String msg) {
        return new FAILSTATUS(msg, 0);
    }

    public static Status failed(String msg, int failureReason) {
        return new FAILSTATUS(msg, failureReason);
    }

}
