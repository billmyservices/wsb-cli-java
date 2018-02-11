package com.billmyservices.cli;

import org.asynchttpclient.Response;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provide a way to specify success or failure
 *
 * @param <T> the success returned value
 */
public interface Result<T> {

    /**
     * Determine if the result operation was success (or failed)
     *
     * @return true if success, false otherwise
     */
    boolean isSuccess();

    /**
     * When failed, return the error message
     *
     * @return the error message
     * @throws IllegalStateException if is success
     */
    String getErrorMessage();

    /**
     * When success, return the returned value
     *
     * @return the resturned value
     * @throws IllegalStateException if is failed
     */
    T get();

    /**
     * Apply one assertion to the current success value, if fail, the new returned result will be failed
     *
     * @param mustBeTrue condition to check
     * @param orFailWith error message if check is false
     * @return a new result
     */
    Result<T> guard(Predicate<T> mustBeTrue, String orFailWith);

    /**
     * Apply one assertion to the current success value, if fail, the new returned result will be failed
     *
     * @param mustBeTrue condition to check
     * @param orFailWith error message if check is false based on the value
     * @return a new result
     */
    Result<T> guard(Predicate<T> mustBeTrue, Function<T, String> orFailWith);
}

class Success<T> implements Result<T> {

    private final T successValue;

    Success(final T successValue) {
        this.successValue = successValue;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public String getErrorMessage() {
        throw new IllegalStateException("Cannot get the error message if result is success");
    }

    @Override
    public T get() {
        return successValue;
    }

    @Override
    public Result<T> guard(Predicate<T> mustBeTrue, String orFailWith) {
        if (!mustBeTrue.test(successValue))
            return new Failed<T>(orFailWith);
        return this;
    }

    @Override
    public Result<T> guard(Predicate<T> mustBeTrue, Function<T, String> orFailWith) {
        if (!mustBeTrue.test(successValue))
            return new Failed<T>(orFailWith.apply(successValue));
        return this;
    }
}

class Failed<T> implements Result<T> {

    private final String errorMessage;

    Failed(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    Failed(final String format, final Object... args) {
        this.errorMessage = String.format(format, args);
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public T get() {
        throw new IllegalStateException("Cannot get the success value if result is failed");
    }

    @Override
    public Result<T> guard(Predicate<T> mustBeTrue, String orFailWith) {
        return this;
    }

    @Override
    public Result<T> guard(Predicate<T> mustBeTrue, Function<T, String> orFailWith) {
        return this;
    }
}