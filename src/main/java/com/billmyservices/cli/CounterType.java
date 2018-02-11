package com.billmyservices.cli;

/**
 * Immutable counter type definition
 */
public final class CounterType {
    private String code;
    private String name;
    private long value;
    private long k1;
    private long k2;
    private CounterVersion version;

    private CounterType() {
    }

    /**
     * Create a new one counter type
     *
     * @param code    Your own counter type code
     * @param name    The name or description for this counter type
     * @param value   The default value for new counters of that type
     * @param k1      First configuration value
     * @param k2      Second configuration value
     * @param version The counters mode (or version), this define the behavior of counters
     */
    public CounterType(String code, String name, long value, long k1, long k2, CounterVersion version) {
        this.code = code;
        this.name = name;
        this.value = value;
        this.k1 = k1;
        this.k2 = k2;
        this.version = version;
    }

    /**
     * Your own counter type code
     *
     * @return Your own counter type code
     */
    public String getCode() {
        return code;
    }

    /**
     * The name or description for this counter type
     *
     * @return The name or description for this counter type
     */
    public String getName() {
        return name;
    }

    /**
     * The default value for new counters of that type
     *
     * @return The default value for new counters of that type
     */
    public long getValue() {
        return value;
    }

    /**
     * First configuration value
     *
     * @return First configuration value
     */
    public long getK1() {
        return k1;
    }

    /**
     * Second configuration value
     *
     * @return Second configuration value
     */
    public long getK2() {
        return k2;
    }

    /**
     * The counters mode (or version), this define the behavior of counters
     *
     * @return The counters mode (or version), this define the behavior of counters
     */
    public CounterVersion getVersion() {
        return version;
    }
}
