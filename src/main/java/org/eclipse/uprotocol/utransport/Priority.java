package org.eclipse.uprotocol.utransport;

import java.util.Arrays;
import java.util.Optional;

public enum Priority {
    // Low Priority. No bandwidth assurance such as File Transfer.
    LOW ("CS0", 0),
    // Standard, undifferentiated application such as General (unclassified).
    STANDARD ("CS1", 1),
    // Operations, Administration, and Management such as Streamer messages (sub, connect, etc…)
    OPERATIONS ("CS2", 2),
    // Multimedia streaming such as Video Streaming
    MULTIMEDIA_STREAMING ("CS3", 3),
    // Real-time interactive such as High priority (rpc events)
    REALTIME_INTERACTIVE ("CS4", 4),
    // Signaling such as Important
    SIGNALING("CS5", 5),
    // Network control such as Safety Critical
    NETWORK_CONTROL ("CS6", 6);

    private final String qosString;
    private final int value;
    public String qosString() {
        return qosString;
    }

    public int intValue() {
        return value;
    }

    Priority(String qosString, int value) {
        this.qosString = qosString;
        this.value = value;
    }

    /**
     * Find the Priority matching the numeric value. Mind you, it might not exist.
     * @param value numeric priority value.
     * @return Returns the Priority matching the numeric value. Mind you, it might not exist.
     */
    public static Optional<Priority> from(int value) {
        return Arrays.stream(Priority.values())
                .filter(p -> p.intValue() == value)
                .findAny();
    }

    /**
     * Find the Priority matching the QOS String value. Mind you, it might not exist.
     * @param qosString QOS String priority value.
     * @return Returns the Priority matching the QOS String value. Mind you, it might not exist.
     */
    public static Optional<Priority> from(String qosString) {
        return Arrays.stream(Priority.values())
                .filter(p -> p.qosString().equals(qosString))
                .findAny();
    }
}
