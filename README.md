# Bill My Services Java client

# Simple Bill My Services shell client

[![Build Status](https://travis-ci.org/billmyservices/wsb-cli-sh.svg?branch=master)](https://travis-ci.org/billmyservices/wsb-cli-sh)

See <a href="https://www.billmyservices.com">www.billmyservices.com</a> for details.

### Dependencies

1. `bash` shell, you can check running `$ bash --version`.
1. `base64` command, you can check running `$ base64 --version`.
1. `xxd` command, you can check running `$ xxd --version`.
1. `openssl` command, you can check running `$ openssl version`.
1. `curl` command, you can check running `$ curl --version`.
## How to install

## Run tests

```shell
$ mvn test -Dbillmyservices_userid= -Dbillmyservices_secretkey=
 ```

### Download

You only need the `wsb-cli` script file.

You can download directly running:

```
$ wget https://raw.githubusercontent.com/billmyservices/wsb-cli-sh/master/wsb-cli
...
```

And set execution permissions:

```
$ chmod +x wsb-cli
...
```

## How to run

### Run without arguments for help

```
$ ./wsb-cli
Web Service Billing command line interface
(c) wsb@computer-mind.com 2017

Usage:

  ./wsb-cli list
  ./wsb-cli read   <counter-type-code>
  ./wsb-cli add    <counter-type-code> <name> <value> <min> <max> <version>
  ./wsb-cli delete <counter-type-code>
  ./wsb-cli get    <counter-type-code> <counter-code>
  ./wsb-cli post   <counter-type-code> <counter-code> <value>
  ./wsb-cli reset  <counter-type-code> <counter-code>

ENVIRONMENT VARIABLES:

  WSB_USERID  your user id into the WSB site https://www.billmyservices.com
  WSB_SKEY    your secret key for messaging authentication and integrity, go to your user profile to get one
  WSB_URL     url to the WSB endpoint (default http://services.billmyservices.com)
  WSB_CURL    optional curl parameters
```

### Required environment variables

You must set at least **two** mandatory environment variables:

1. **WSB_USERID**, your *Bill My Services* account ID.
1. **WSB_SKEY**, your *Bill My Services* secret key.

You can get both values from your *Bill My Services* account profile creating a new account into https://www.billmyservices.com

Only for testing purposes (**is not guarantee will be available**, counters will be deleted every some time, ...) there is one public account:

```
export WSB_USERID=50
export WSB_SKEY=M6UxiYsELKKHclwYFfKluzvuwj7Bvtk1pY5RUtPhUb4=
```

### Running your first command

You can run a `wsb-cli` command with local environment variables (eg. using the public ones):

```
$ WSB_USERID=50 WSB_SKEY=M6UxiYsELKKHclwYFfKluzvuwj7Bvtk1pY5RUtPhUb4= ./wsb-cli list
[]
```

(The response could contains counters created by other people)

### Simple example

Set environment variables for the current *shell* session:

```
$ export WSB_USERID=50
$ export WSB_SKEY=M6UxiYsELKKHclwYFfKluzvuwj7Bvtk1pY5RUtPhUb4=
```

Creating a simple absolute **counter type** between 0 and 5:

```
$ ./wsb-cli add my05counter "My 0~5 counter" 0 0 5 AbsoluteCounter
```

Now, you can check listing it:

```
$ ./wsb-cli list
[{"value":0,"k2":5,"userId":47,"name":"My 0~5 counter","version":"AbsoluteCounter","k1":0,"code":"my05counter"}]
```

For any **specific counter code** we can read, and try to increment or decrement the value:
```
$ for i in `seq 1 10`
do
  ./wsb-cli get my05counter test012345
  echo ""
  if ./wsb-cli post my05counter test012345 1
  then
    echo "Increment done!"
  else
    echo "ERROR cannot increment anymore!"
  fi
done
```

With output:
```
{"timeRef":1517764766,"value":0,"code":"test012345","counterTypeId":12888}
Increment done!
{"timeRef":1517764766,"value":1,"code":"test012345","counterTypeId":12888}
Increment done!
{"timeRef":1517764766,"value":2,"code":"test012345","counterTypeId":12888}
Increment done!
{"timeRef":1517764766,"value":3,"code":"test012345","counterTypeId":12888}
Increment done!
{"timeRef":1517764766,"value":4,"code":"test012345","counterTypeId":12888}
Increment done!
{"timeRef":1517764766,"value":5,"code":"test012345","counterTypeId":12888}
ERROR cannot increment anymore!
{"timeRef":1517764766,"value":5,"code":"test012345","counterTypeId":12888}
ERROR cannot increment anymore!
{"timeRef":1517764766,"value":5,"code":"test012345","counterTypeId":12888}
ERROR cannot increment anymore!
{"timeRef":1517764766,"value":5,"code":"test012345","counterTypeId":12888}
ERROR cannot increment anymore!
{"timeRef":1517764766,"value":5,"code":"test012345","counterTypeId":12888}
ERROR cannot increment anymore!
```

Now, we can try to decrement:
```
$ for i in `seq 1 10`
do
  ./wsb-cli get my05counter test012345
  echo ""
  if ./wsb-cli post my05counter test012345 -1
  then
    echo "Decrement done!"
  else
    echo "ERROR cannot decrement anymore!"
  fi
done
```

With output:
```
{"timeRef":1517764766,"value":5,"code":"test012345","counterTypeId":12888}
Decrement done!
{"timeRef":1517764766,"value":4,"code":"test012345","counterTypeId":12888}
Decrement done!
{"timeRef":1517764766,"value":3,"code":"test012345","counterTypeId":12888}
Decrement done!
{"timeRef":1517764766,"value":2,"code":"test012345","counterTypeId":12888}
Decrement done!
{"timeRef":1517764766,"value":1,"code":"test012345","counterTypeId":12888}
Decrement done!
{"timeRef":1517764766,"value":0,"code":"test012345","counterTypeId":12888}
ERROR cannot decrement anymore!
{"timeRef":1517764766,"value":0,"code":"test012345","counterTypeId":12888}
ERROR cannot decrement anymore!
{"timeRef":1517764766,"value":0,"code":"test012345","counterTypeId":12888}
ERROR cannot decrement anymore!
{"timeRef":1517764766,"value":0,"code":"test012345","counterTypeId":12888}
ERROR cannot decrement anymore!
{"timeRef":1517764766,"value":0,"code":"test012345","counterTypeId":12888}
ERROR cannot decrement anymore!
```

## See <a href="https://www.billmyservices.com">www.billmyservices.com</a> for details

