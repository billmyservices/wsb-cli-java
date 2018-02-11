package com.billmyservices.cli;

/**
 * Different counter behavior
 */
public enum CounterVersion {

    /**
     * Absolute counter, the counter value will be guarantee to be between k1 and k2
     */
    AbsoluteCounter,

    /**
     * Frequency counter, no more than k1 accumulated value will be guarantee to be for each k2 seconds intervals
     */
    FrequencyCounter
}
