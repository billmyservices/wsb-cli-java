package com.billmyservices.cli;

/**
 * Immutable counter definition
 */
public final class Counter {
    private String code;
    private long timeRef;
    private long value;

    private Counter() {
    }

    /**
     * Your own counter code
     *
     * @return Your own counter code
     */
    public String getCode() {
        return code;
    }

    /**
     * The UNIX EPOCH time
     *
     * @return The UNIX EPOCH time
     */
    public long getTimeRef() {
        return timeRef;
    }

    /**
     * The current counter value
     *
     * @return The current counter value
     */
    public long getValue() {
        return value;
    }
}
