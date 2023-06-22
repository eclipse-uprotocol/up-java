package org.eclipse.uprotocol;

/**
 * UProtocol general Ack message for all operations.
 * Can be made into a Monad in the future.
 */
public abstract class Ack {
    private static final String OK = "ok";
    private static final String FAILED = "failed";

    abstract boolean isSuccess();
    abstract String msg();

    public boolean isFailed() {
        return !isSuccess();
    }

    private static class OKACK extends Ack {

        @Override
        public boolean isSuccess() {
            return true;
        }
        @Override
        public String msg() {
            return Ack.OK;
        }
    }
    private static class FAILACK extends Ack {

        private final String failMsg;

        // TODO make this into an Enum so Steven does not loose his panties
        private final int failureReason;

        private FAILACK(String failMsg, int failureReason) {
            this.failMsg = failMsg;
            this.failureReason = failureReason;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        String msg() {
            return this.failMsg;
        }
    }

    public static Ack ok() {
        return new OKACK();
    }

    public static Ack failed() {
        return new FAILACK(FAILED, 0);
    }

    public static Ack failed(String msg) {
        return new FAILACK(msg, 0);
    }

    public static Ack failed(String msg, int failureReason) {
        return new FAILACK(msg, failureReason);
    }
}
