package com.billmyservices.cli;

import org.asynchttpclient.ListenableFuture;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static com.billmyservices.cli.CounterVersion.AbsoluteCounter;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Bill My Services, Java client, test suite")
class BMSClientTest {
    private final int CONCURRENT_TESTS = 40;
    private final long MAX_FUTURES_TIME_MS = 300; // Travis CI is slow requesting... usual local value could be 100 mS
    private final int THREADS = 4;
    private final Logger LOGGER = Logger.getLogger(BMSClient.class.getName());

    @BeforeAll
    static void init() throws ExecutionException, InterruptedException {

        // wakeup client connection (first connections could be slow if ip route, http-async pool setup, ...)
        BMSClient.getDefault().listCounterTypes().get();

    }

    private static CounterType rndCounterType() {
        return new CounterType(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                0, -5, 5,
                AbsoluteCounter
        );
    }

    private void counterTypeManagementTest() throws ExecutionException, InterruptedException {

        final BMSClient bms = BMSClient.getDefault();

        final CounterType counterType = rndCounterType();

        assertTrue(bms.listCounterTypes().get()
                .guard(rs -> Arrays.stream(rs).noneMatch(r -> counterType.getCode().equals(r.getCode())), "new counter should not be in list")
                .isSuccess());

        // should be created a new one counter type
        assertTrue(bms.addCounterType(counterType).get().isSuccess());

        assertTrue(bms.listCounterTypes().get()
                .guard(rs -> Arrays.stream(rs).anyMatch(r -> counterType.getCode().equals(r.getCode())), "new counter should be in list")
                .isSuccess());

        assertTrue(bms.readCounterType(counterType.getCode()).get()
                .guard(ct -> counterType.getCode().equals(ct.getCounterType().getCode())
                        && counterType.getName().equals(ct.getCounterType().getName())
                        && counterType.getValue() == ct.getCounterType().getValue()
                        && counterType.getK1() == ct.getCounterType().getK1()
                        && counterType.getK2() == ct.getCounterType().getK2()
                        && counterType.getVersion().equals(ct.getCounterType().getVersion()), "should be possible read the counter type")
                .isSuccess());

        // should be possible delete the counter type
        assertTrue(bms.deleteCounterType(counterType.getCode()).get().isSuccess());

        assertTrue(bms.listCounterTypes().get()
                .guard(rs -> Arrays.stream(rs).noneMatch(r -> counterType.getCode().equals(r.getCode())), "new counter type should not be in list")
                .isSuccess());

    }

    private void absoluteCountersTest() throws ExecutionException, InterruptedException {

        final BMSClient bms = BMSClient.getDefault();

        final CounterType counterType = rndCounterType();

        final String counterCode = UUID.randomUUID().toString();

        // should be created a new one counter type
        assertTrue(bms.addCounterType(counterType).get().isSuccess());

        // should be able increment 3
        assertTrue(bms.postCounter(counterType.getCode(), counterCode, 3L).get().isSuccess());

        assertTrue(bms.readCounter(counterType.getCode(), counterCode).get()
                .guard(k -> counterCode.equals(k.getCode())
                        && counterType.getValue() + 3L == k.getValue(), "should be able read one unused counter")
                .isSuccess());

        // should be able reset the counter
        assertTrue(bms.resetCounter(counterType.getCode(), counterCode).get().isSuccess());

        assertTrue(bms.readCounter(counterType.getCode(), counterCode).get()
                .guard(k -> counterCode.equals(k.getCode())
                        && counterType.getValue() == k.getValue(), "the counter should be reseted")
                .isSuccess());

        // should NOT be able big increments
        for (final long delta : LongStream.of(6L, -6L, -10L, 10L).toArray())
            assertFalse(bms.postCounter(counterType.getCode(), counterCode, delta).get().isSuccess());

        // should NOT be able the following increments
        for (final long delta : LongStream.of(5L, -1L, -2L, -3L, -2L, -2L, 10L).toArray())
            assertTrue(bms.postCounter(counterType.getCode(), counterCode, delta).get().isSuccess());

        // should be possible delete the counter type
        assertTrue(bms.deleteCounterType(counterType.getCode()).get().isSuccess());
    }

    void nonBlockingTest() {

        final BMSClient bms = BMSClient.getDefault();

        final List<CounterType> counterTypes = IntStream.range(0, CONCURRENT_TESTS).mapToObj(ignore -> rndCounterType()).collect(toList());

        final long t0 = System.currentTimeMillis();

        final List<ListenableFuture<Result<Boolean>>> futures1 = counterTypes.stream().map(bms::addCounterType).collect(toList());

        final long t1 = System.currentTimeMillis();

        final List<Result<Boolean>> results1 = futures1.stream().map(k -> {
            try {
                return k.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).collect(toList());

        final long t2 = System.currentTimeMillis();

        final List<ListenableFuture<Result<Boolean>>> futures2 = counterTypes.stream().map(ct -> bms.deleteCounterType(ct.getCode())).collect(toList());

        final long t3 = System.currentTimeMillis();

        final List<Result<Boolean>> results2 = futures2.stream().map(k -> {
            try {
                return k.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).collect(toList());

        final long t4 = System.currentTimeMillis();

        final long d1 = t1 - t0;
        final long d2 = t3 - t2;

        LOGGER.info(String.format("Delta times: %d, %d, %d, %d%n", d1, t2 - t1, d2, t4 - t3));

        assertTrue(d1 < MAX_FUTURES_TIME_MS);

        assertTrue(d2 < MAX_FUTURES_TIME_MS);

        assertTrue(results1.stream().allMatch(Result::isSuccess));

        assertTrue(results2.stream().allMatch(Result::isSuccess));

    }

    private void concurrentTest() {

        final List<ParallelTest> ts = IntStream.range(0, THREADS).mapToObj(ignore -> new ParallelTest(this)).collect(toList());

        ts.forEach(ParallelTest::start);

        for (final ParallelTest t : ts)
            try {
                t.join();
            } catch (InterruptedException e) {
                fail("multiple threaded connections failed", e);
            }

        for (final ParallelTest t : ts)
            if (!t.result.isSuccess()) {
                LOGGER.warning(t.result.getErrorMessage());
                fail(t.result.getErrorMessage());
            }
    }

    @Test
    @DisplayName("Counter type management")
    void runCounterTypeManagementTest() throws ExecutionException, InterruptedException {
        counterTypeManagementTest();
    }

    @Test
    @DisplayName("Absolute counters")
    void runAbsoluteCountersTest() throws ExecutionException, InterruptedException {
        absoluteCountersTest();
    }

    @Test
    @DisplayName("Non blocking support")
    void runNonBlockingTest() {
        nonBlockingTest();
    }

    @Test
    @DisplayName("Concurrency support")
    void runConcurrentTest() {
        concurrentTest();
    }

}

class ParallelTest extends Thread {
    private final BMSClientTest test;
    Result<Boolean> result = new Failed<>("test not executed");

    ParallelTest(final BMSClientTest test) {
        this.test = test;
    }

    public void run() {
        try {
            test.nonBlockingTest();
            result = new Success<>(true);
        } catch (AssertionFailedError e) {
            result = new Failed<>(e.getLocalizedMessage());
        }
    }

    public Result<Boolean> getResult() {
        return result;
    }
}
