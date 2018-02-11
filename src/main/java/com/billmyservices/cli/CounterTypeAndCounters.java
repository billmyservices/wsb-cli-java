package com.billmyservices.cli;

/**
 * Immutable counter type with counters definition
 */
public final class CounterTypeAndCounters {

    private CounterType counterType;

    private Counter[] counters;

    public CounterType getCounterType() {
        return counterType;
    }

    public Counter[] getCounters() {
        return counters;
    }
}
