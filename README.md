# Bill My Services Java client

[![Build Status](https://travis-ci.org/billmyservices/wsb-cli-sh.svg?branch=master)](https://travis-ci.org/billmyservices/wsb-cli-sh)

See <a href="https://www.billmyservices.com">www.billmyservices.com</a> for details.

### Dependencies

Transitive dependencies:

* org.asynchttpclient, async-http-client, 2.1.0-alpha24
* com.jsoniter, jsoniter, 0.9.16

## Maven

Add to your <code>pom.xml</code> file:

**TO DO:** https://stackoverflow.com/a/14013645/1540749

```
<repositories>
    <repository>
        <id>mvn-repo</id>
        <url>https://raw.github.com/YOUR-USERNAME/YOUR-PROJECT-NAME/mvn-repo/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```

## Run tests

```shell
$ mvn test -Dbillmyservices_userid={your-user-id} -Dbillmyservices_secretkey={your-secret-key}
```


