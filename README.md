# Bill My Services Java client

[![Build Status](https://travis-ci.org/billmyservices/wsb-cli-sh.svg?branch=master)](https://travis-ci.org/billmyservices/wsb-cli-sh)

See <a href="https://www.billmyservices.com">www.billmyservices.com</a> for details.

### Dependencies

Transitive dependencies:

* org.asynchttpclient, async-http-client, 2.1.0-alpha24
* com.jsoniter, jsoniter, 0.9.16

## Run tests

```shell
$ mvn test -Dbillmyservices_userid={your-user-id} -Dbillmyservices_secretkey={your-secret-key}
```

## Maven

Add to your <code>pom.xml</code> file:

```xml
<repositories>
    <repository>
        <id>mvn-repo</id>
        <url>https://raw.github.com/billmyservices/wsb-cli-java/mvn-repo/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>billmyservices</groupId>
        <artifactId>wsb-cli-java</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Example

```java
package com.test;

import com.billmyservices.cli.BMSClient;
import com.billmyservices.cli.CounterType;
import com.billmyservices.cli.CounterVersion;
import com.billmyservices.cli.Result;
import org.asynchttpclient.ListenableFuture;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String... args) throws IOException, InterruptedException, ExecutionException {

        // get the default singleton client
        final BMSClient bms = BMSClient.getDefault();


        // usually our counter types will already exists, here we create one random programmatically
        // but you can use the https://www.billmyservices.com user interface for it
        final String myCounterTypeCode = UUID.randomUUID().toString();

        createCounterTypeProgrammatically(bms, myCounterTypeCode);




        // with any counter type we can check resources access, Peter will have access for five times, no more
        for(int i = 0; i < 7; i++) {

            // Peter want access to our resource one (1L) more time
            final ListenableFuture<Result<Boolean>> futureCounterCheck = bms.postCounter(myCounterTypeCode, "Peter", 1L);

            // we can run something while the counter is increased
            Thread.sleep(1000);

            // have Peter access to the resource?
            if (futureCounterCheck.get().isSuccess())
                System.out.printf("Yes, Peter is granted!%n");
            else
                System.out.printf("No, Peter is not granted!%n");

        }

        // we could close explicitly the http client resources
        bms.getHttpClient().close();

    }

    private static void createCounterTypeProgrammatically(final BMSClient bms, final String counterTypeCode) throws InterruptedException, ExecutionException {

        // create one counter type, you could reuse similar counter types (not to create one each time)
        final CounterType counterType = new CounterType(counterTypeCode, "my simple counter", 0L, 0L, 5L, CounterVersion.AbsoluteCounter);
        final ListenableFuture<Result<Boolean>> futureCounterType = bms.addCounterType(counterType);

        // we can run something while the counter type is created
        Thread.sleep(1000);

        // now we need the counter type, cannot wait anymore
        if(!futureCounterType.get().isSuccess())
            throw new IllegalStateException("cannot create the counter!");

    }

}
```

Now, you can compile using this build:

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <configuration>
                <archive>
                    <manifest>
                        <mainClass>com.test.Main</mainClass>
                    </manifest>
                </archive>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <finalName>testMain</finalName>
                <appendAssemblyId>false</appendAssemblyId>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Deploy using:

```
$ mvn clean compile assembly:single
```

An run using:

```
$ java -Dbillmyservices_userid=50 -Dbillmyservices_secretkey=M6UxiYsELKKHclwYFfKluzvuwj7Bvtk1pY5RUtPhUb4= -jar target/testMain.jar
```

With running output:

```
Yes, Peter is granted!
Yes, Peter is granted!
Yes, Peter is granted!
Yes, Peter is granted!
Yes, Peter is granted!
No, Peter is not granted!
No, Peter is not granted!
```
