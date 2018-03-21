package com.billmyservices.cli;

import com.jsoniter.JsonIterator;
import io.netty.handler.codec.http.HttpMethod;
import org.asynchttpclient.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpMethod.*;

/**
 * Immutable, thread safe, http non blocking Bill My Service client.
 */
public class BMSClient {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final String DEFAULT_BMS_URL = "http://services.billmyservices.com";
    private static final String SETTING_NAME_URL = "billmyservices_url";
    private static final String SETTING_NAME_USERID = "billmyservices_userid";
    private static final String SETTING_NAME_SECRETKEY = "billmyservices_secretkey";

    private static BMSClient lazyDefaultSingleton = null;

    private final String url;
    private final String userId;
    private final SecretKeySpec keySpec;
    private final AsyncHttpClient httpClient;

    /**
     * Create a new one BMSClient thread safe instance.
     *
     * @param billMyServicesURL the Bill My Service endpoint
     * @param userId            your Bill My Service user profile Id
     * @param secretKey         your Bill My Service secret key (Base64 encoded)
     * @param httpClient        the http client to use
     */
    public BMSClient(final String billMyServicesURL, final String userId, final String secretKey, final AsyncHttpClient httpClient) {
        this.url = billMyServicesURL;
        this.userId = userId;
        this.keySpec = new SecretKeySpec(Base64.getDecoder().decode(secretKey), HMAC_ALG);
        this.httpClient = httpClient;
    }

    /**
     * Return all your counter types
     *
     * @return a list of counter types
     */
    public ListenableFuture<Result<CounterType[]>> listCounterTypes() {
        return rawCall(GET, null, null, null, null, null, null, null)
                .execute(new AsyncCompletionHandler<Result<CounterType[]>>() {
                    @Override
                    public Result<CounterType[]> onCompleted(Response response) throws Exception {
                        return withStatus(200, response, r -> jsonParser(r, CounterType[].class));
                    }
                });
    }

    /**
     * Add a new one counter type
     *
     * @param counterType the counter type information
     * @return true if success false otherwise
     */
    public ListenableFuture<Result<Boolean>> addCounterType(final CounterType counterType) {
        return rawCall(PUT, counterType.getCode(), null, counterType.getName(), counterType.getValue(), counterType.getK1(), counterType.getK2(), counterType.getVersion())
                .execute(new AsyncCompletionHandler<Result<Boolean>>() {
                    @Override
                    public Result<Boolean> onCompleted(Response response) throws Exception {
                        return withStatus(200, response);
                    }
                });
    }

    /**
     * Read one counter type with all their counters
     *
     * @param counterTypeCode your counter type code
     * @return the readed counter type
     */
    public ListenableFuture<Result<CounterTypeAndCounters>> readCounterType(final String counterTypeCode) {
        return rawCall(GET, counterTypeCode, null, null, null, null, null, null)
                .execute(new AsyncCompletionHandler<Result<CounterTypeAndCounters>>() {
                    @Override
                    public Result<CounterTypeAndCounters> onCompleted(Response response) throws Exception {
                        return withStatus(200, response, r -> jsonParser(r, CounterTypeAndCounters.class));
                    }
                });
    }

    /**
     * Delete one counter type
     *
     * @param counterTypeCode your counter type code
     * @return true if success false otherwise
     */
    public ListenableFuture<Result<Boolean>> deleteCounterType(final String counterTypeCode) {
        return rawCall(DELETE, counterTypeCode, null, null, null, null, null, null)
                .execute(new AsyncCompletionHandler<Result<Boolean>>() {
                    @Override
                    public Result<Boolean> onCompleted(Response response) throws Exception {
                        return withStatus(200, response);
                    }
                });
    }

    /**
     * Read one counter, if not exist, the default values will be returned
     *
     * @param counterTypeCode your counter type code
     * @param counterCode     your counter code
     * @return the readed counter
     */
    public ListenableFuture<Result<Counter>> readCounter(final String counterTypeCode, final String counterCode) {
        return rawCall(GET, counterTypeCode, counterCode, null, null, null, null, null)
                .execute(new AsyncCompletionHandler<Result<Counter>>() {
                    @Override
                    public Result<Counter> onCompleted(Response response) throws Exception {
                        return withStatus(200, response, r -> jsonParser(r, Counter.class));
                    }
                });
    }

    /**
     * Post an increment counter value
     *
     * @param counterTypeCode your counter type code
     * @param counterCode     your counter code
     * @param valueDelta      the value delta
     * @return true if success false otherwise
     */
    public ListenableFuture<Result<Boolean>> postCounter(final String counterTypeCode, final String counterCode, final long valueDelta) {
        return rawCall(POST, counterTypeCode, counterCode, null, valueDelta, null, null, null)
                .execute(new AsyncCompletionHandler<Result<Boolean>>() {
                    @Override
                    public Result<Boolean> onCompleted(Response response) throws Exception {
                        return withStatus(200, response);
                    }
                });
    }

    /**
     * Reset one counter
     *
     * @param counterTypeCode your counter type code
     * @param counterCode     your counter code
     * @return true if success false otherwise
     */
    public ListenableFuture<Result<Boolean>> resetCounter(final String counterTypeCode, final String counterCode) {
        return rawCall(DELETE, counterTypeCode, counterCode, null, null, null, null, null)
                .execute(new AsyncCompletionHandler<Result<Boolean>>() {
                    @Override
                    public Result<Boolean> onCompleted(Response response) throws Exception {
                        return withStatus(200, response);
                    }
                });
    }

    /**
     * Return the async http client.
     *
     * @return the async http client.
     */
    public AsyncHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * HTTP status validator, return Result with fail if the http status is not the expected otherwise, the value mapper will be invoked
     *
     * @param httpStatus the expected http status
     * @param response   the response
     * @param mapper     the success value mapper
     * @param <T>        the success value type
     * @return the result
     */
    private static <T> Result<T> withStatus(final int httpStatus, final Response response, final Function<Response, Result<T>> mapper) {
        if (response.getStatusCode() == httpStatus)
            return mapper.apply(response);
        return new Failed<>("expected HTTP %d but %d, response was `%s`", httpStatus, response.getStatusCode(), response.getResponseBody());
    }

    /**
     * HTTP status validator, return Result with fail if the http status is not the expected otherwise, true
     *
     * @param httpStatus the expected http status
     * @param response   the response
     * @return the result
     */
    private static Result<Boolean> withStatus(final int httpStatus, final Response response) {
        return withStatus(httpStatus, response, ignore -> new Success<>(true));
    }

    /**
     * Try to parse from JSON the response body
     *
     * @param response the response
     * @param clazz    the expected deserialized class
     * @param <T>      the deserialized type
     * @return the result
     */
    private static <T> Result<T> jsonParser(final Response response, final Class<T> clazz) {
        try {
            return new Success<>(JsonIterator.deserialize(response.getResponseBody(), clazz));
        } catch (Exception e) {
            return new Failed<T>(e.getLocalizedMessage());
        }
    }

    /**
     * Since available HMAC algorithms are not thread safe, here we construct a new one, if performance is a problem,
     * cloning, other hmac api, pooling, ... could be moved on
     *
     * @return a new one Mac object
     */
    private Mac makeMac() {
        final Mac mac;
        try {
            mac = Mac.getInstance(HMAC_ALG);
            mac.init(keySpec);
            return mac;
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send a raw call to the server
     *
     * @param method          the HTTP method to use
     * @param counterTypeCode may be null, your own counter type code
     * @param counterCode     may be null, your own counter code
     * @param name            may be null, your counter type name
     * @param value           may be null, the operation value
     * @param k1              may be null, the counter type k1 value
     * @param k2              may be null, the counter type k2 value
     * @param counterVersion  may be null, the counter version
     * @return the server response
     */
    private BoundRequestBuilder rawCall(final HttpMethod method, final String counterTypeCode, final String counterCode, final String name, final Long value, final Long k1, final Long k2, final CounterVersion counterVersion) {

        // final URL
        final String URL;
        if (counterTypeCode == null) {
            if (counterCode == null) {
                URL = String.format("%s/%s", url, userId);
            } else {
                throw new IllegalArgumentException("if `counterTypeCode` is null then `counterCode` must be null");
            }
        } else {
            if (counterCode == null) {
                URL = String.format("%s/%s/%s", url, userId, counterTypeCode);
            } else {
                URL = String.format("%s/%s/%s/%s", url, userId, counterTypeCode, counterCode);
            }
        }

        // HTTP method
        final BoundRequestBuilder rq;
        switch (method.name()) {
            case "POST":
                rq = httpClient.preparePost(URL);
                break;
            case "GET":
                rq = httpClient.prepareGet(URL);
                break;
            case "DELETE":
                rq = httpClient.prepareDelete(URL);
                break;
            case "PUT":
                rq = httpClient.preparePut(URL);
                break;
            default:
                throw new IllegalArgumentException(String.format("The http method `%s` is not supported", method.name()));
        }

        final String _time = Long.toString(System.currentTimeMillis() / 1_000L);

        final String _value;
        final String _k1;
        final String _k2;
        final String _mode;

        if (value == null)
            _value = null;
        else {
            _value = Long.toString(value);
            rq.addHeader("wsb-value", _value);
        }

        if (k1 == null)
            _k1 = null;
        else {
            _k1 = Long.toString(k1);
            rq.addHeader("wsb-k1", _k1);
        }

        if (k2 == null)
            _k2 = null;
        else {
            _k2 = Long.toString(k2);
            rq.addHeader("wsb-k2", _k2);
        }

        if (counterVersion == null)
            _mode = null;
        else {
            _mode = counterVersion.toString();
            rq.addHeader("wsb-mode", _mode);
        }

        rq.addHeader("wsb-time", _time);

        rq.addHeader("wsb-hmac", computeHMAC(join(userId, counterTypeCode, counterCode, name, _value, _k1, _k2, _mode, _time)));

        if (name != null)
            rq.addHeader("wsb-name", name);

        return rq;
    }

    /**
     * Return de hexadecimal representation of the computed hmac for the given input.
     *
     * @param xs string to hash
     * @return the hexadecimal hash
     */
    private String computeHMAC(final String xs) {
        return Base64.getEncoder().encodeToString(makeMac().doFinal(xs.getBytes(StandardCharsets.US_ASCII)));
    }

    /**
     * Try to construct the default `BMSClient` instance using the system properties: billmyservices_url, billmyservices_userid
     * and billmyservices_secretkey (Base64 encoded) or the environment variables: BILLMYSERVICES_URL, BILLMYSERVICES_USERID
     * and BILLMYSERVICES_SECRETKEY (Base64 encoded). For the URL, the default `http://services.billmyservices.com` value is used.
     *
     * @return one unique singleton default instance
     */
    public static BMSClient getDefault() {
        if (lazyDefaultSingleton == null)
            createSingletonDefault();
        return lazyDefaultSingleton;
    }

    /**
     * Construct the default singleton instance.
     */
    private synchronized static void createSingletonDefault() {
        if (lazyDefaultSingleton == null)
            lazyDefaultSingleton = new BMSClient(
                    getSettingValue(SETTING_NAME_URL, DEFAULT_BMS_URL),
                    getSettingValue(SETTING_NAME_USERID),
                    getSettingValue(SETTING_NAME_SECRETKEY),
                    new DefaultAsyncHttpClient()
            );
    }

    /**
     * Try to get one setting value from system properties, if not found, try to get it from the environment variables,
     * if not found, return the default value.
     *
     * @param key the setting key
     * @return the configured value
     */
    private static String getSettingValue(final String key, final String defaultValue) {

        final String fromSysProperty = System.getProperty(key.toLowerCase());
        if (fromSysProperty != null)
            return fromSysProperty;

        final String fromEnvironment = System.getenv(key.toUpperCase());
        if (fromEnvironment != null)
            return fromEnvironment;

        return defaultValue;
    }

    /**
     * Try to get one setting value from system properties, if not found, try to get it from the environment variables,
     * if not found, throws `IllegalArgumentException`.
     *
     * @param key the setting key
     * @return the configured value
     * @throws IllegalArgumentException if value not found
     */
    private static String getSettingValue(final String key) {
        final String value = getSettingValue(key, null);
        if (value == null)
            throw new IllegalArgumentException(String.format("Bill My Services configuration error, no settings found for the `%s` value", key));
        return value;
    }

    /**
     * Join non null strings (forced to be strings to force reuse conversions)
     *
     * @param xs object values
     * @return concatenated non null values
     */
    private static String join(final String... xs) {
        final StringBuilder s = new StringBuilder();
        for (final Object x : xs)
            if (x != null)
                s.append(x);
        return s.toString();
    }
}
