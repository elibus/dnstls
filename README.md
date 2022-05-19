# dnstls
A simple async multi-threaded udp/tcp proxy over TLS written in Java.

## Background

## Getting started

### Prerequisites

This project uses Java 11 and gradle. You can install a current version of Java using [sdkman](https://sdkman.io/install) by running:

```sh
$ sdk install java 11.0.13.8.1-amzn
```

No need to install gradle, it will be downloaded automatically by the gradle wrapper.

### Run it as Spring Boot application

dntls can be run with:
```sh
$ ./gradlew bootrun
```

### Run it as a fat jar

`dnstls` can also be deployed and run as a fat jar. Build the application with:
```sh
$ ./gradlew build
```

and run it with:
```sh
elibus@alice dnstls $ (master) java -jar build/libs/dnstls-0.0.1.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.6.1)

2021-12-04 14:24:01.525  INFO 75469 --- [           main] org.freaknet.elibus.dnstls.Application   : Starting Application using Java 11.0.13 on alice.local with PID 75469 (/Users/elibus/dev/dnstls/build/libs/dnstls-0.0.1.jar started by elibus in /Users/elibus/dev/dnstls)
2021-12-04 14:24:01.527  INFO 75469 --- [           main] org.freaknet.elibus.dnstls.Application   : No active profile set, falling back to default profiles: default
2021-12-04 14:24:02.138  INFO 75469 --- [           main] o.f.elibus.dnstls.proxy.udp.UdpServer    : UDP server #0 started on 0.0.0.0:8553.
2021-12-04 14:24:02.155  INFO 75469 --- [           main] o.f.elibus.dnstls.proxy.tcp.TcpServer    : TCP server started on: 0.0.0.0:8553. Threads: 200
2021-12-04 14:24:02.212  INFO 75469 --- [           main] org.freaknet.elibus.dnstls.Application   : Started Application in 1.06 seconds (JVM running for 1.478)
```

### Run it as a docker container

There is no `Dockerfile` in this distribution because a container can be built using the gradle/buildpacks integration.
```sh
$ ./gradlew bootBuildImage
```

Then start the container with:
```sh
$ docker run -d -p 853:8553/tcp -p 853:8553/udp dnstls:0.0.1
```

If the application does not start or starts and fails some time after with `Caused by: java.lang.OutOfMemoryError: Direct buffer memory` error, the direct memory needs to be increase. JVM memory parameters inside a container are dinamically computed by the image and the predefined value is too low as netty allocate direct buffers as unsafe and not on the heap.
It's enough to increase `-XX:MaxDirectMemorySize` as shown below.

```
$ docker run -d -p 853:8553/tcp -p 853:8553/udp -e JAVA_OPTS="-XX:MaxDirectMemorySize=32M" dnstls:0.0.1
```

The higher the number of threads the bigger is the memory required.

### Configuration parameters

The application can be configured by means of standard [Spring Boot external configuration](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/html/boot-features-external-config.html).

The table below displays all supported configuration parameters with default values.
| Parameter | Description | Default |
|-----------|-------------|---------|
| dnstls.dns.host | Remote DNS host | 1.1.1.1 |
| dnstls.dns.port | Remote DNS port | 53 |
| dnstls.tcp.host | TCP interface to bind | 0.0.0.0 |
| dnstls.tcp.port | TCP port to bind | 8553 |
| dnstls.tcp.threads | Number of worker threads for the TCP server | 8 |
| dnstls.udp.host | UDP interface to bind | 0.0.0.0 |
| dnstls.udp.port | UDP port to bind | 8553 |
| dnstls.udp.threads |  Number of worker threads for the UDP server | 8 (1 listener) |

One simple way to configure the application is to use environment variables (replacing `.` with `_`) as follows:

```sh
$ export nstls_tcp_threads=10
$ export dnstls_udp_threads=10
$ java -jar build/libs/dnstls-0.0.1-SNAPSHOT.jar
```

or as a container:
```sh
$ docker run -d -p 853:8553/tcp -p 853:8553/udp -e dnstls_tcp_threads=10 -e dnstls_udp_threads=10 dnstls:0.0.1
```


## Implementation choices

### Language choice

I have chosen Java as language for two main reasons:
 - it's fit for the job
 - it's the primary language I have been working with over last few years

It may not be optimal compared to a `golang` or `rust` implementation as it has a bigger system footprint, however I needed to optimize my time to deliver something decent (delivery is a feature).
I consider this also to be a general principle: understand what you can deliver with the skills you have, optimization can be done later when and if needed. 

### Framework of choice

I am using Spring Boot because it got me started quickly and I can use dependency inversion without too much of reinventing the wheel. It also makes it easy to create a fat jar and a docker container.

### Multi-threading

The implementation is multi-threaded, this is required to serve more requests in parallel.
This is also preventing that a single slow request negatively affect the response time of subsequent requests. This can happen, for example, if the remote DNS system is slow or temporarely not reachable. The first request will block the system and next requests will be waiting to be dequeued one after another.
A multi-threaded implementation can serve multiple requests in parallel reducing the odds that one failing request makes other requests timeout.

I have also chosen a non-blocking thread model. It means that there is a fixed pool of threads serving requests and when a task blocks the thread execution does not and the executor can switch to another task. For example in case a task blocks on network I/O, the thread is free and can work on another task. This model allows for better concurrency, less memory and, in general, better performance and scalability.

The UDP server has a limitation inherited by the netty design which caps performance. Despite the implementation being multi-threaded, no matter the number of threads set in the `EventLoopGroup`, there will always be only one Channel and therefore one thread to serve all requests. After a datagram is received, the channel handler will schedule an async thread task for immediate execution to make the backend connection to the DNS non-blocking, which mitigates the issue but does not solve it completely.
A workaround, which I implemented, is to bind multiple times on the same port leveraging the `SO_REUSEPORT` feature. Binding multiple times to the same port will create multiple channels and each channel will be served by a different thread. However, `SO_REUSEPORT` is supported only on Linux and the load distribution will be done based on the source address, thus making this optimization not effective when there is a single source address.

### Optimizations

- The implementation uses `EpollEventLoopGroup` instead of the default `Nio`. On Linux this perform better as it uses the native transport and the `epoll` system call.
- The TCP server and the backend client use the `SO_KEEPALIVE` option to reuse connections and the `TCP_NODELAY` option to disable the TCP slow start

##  Imagine this proxy being deployed in an infrastructure. What would be the security concerns you would raise?

The proxy is meant to secure traffic between clients not supporting DNS over TLS and a DoT server. To be an effective measure, no unencrypted traffic should exit the host especially in a shared environment. An effective way could be to have a local instance of `dnstls` listening on the loopback interface only. The host can then be configured to use 127.0.0.1 as DNS.

## How would you integrate that solution in a distributed, microservices-oriented and containerized architecture?

One solution could be to have one instance per container, in general this could be acceptable as the overhead should be minimal.
A more efficient solution could be to deploy a local instance of `dnstls` listening on a local private virtualised network for the physical host, and make this network visible to all containers running on a single physical host thus sharing the same `dnstls` instance. This configuration not only requires less resources but also can implement a probably more effective central DNS cache as multiple containers share the same instance.

## What other improvements do you think would be interesting to add to the project?

 - Caching: `dnstls` could cache DNS responses to improve speed of resolution for frequently requests queries and reduce network traffic
 - DNS server pool: `dnstls` supports 1 DNS server. Normally this should not be a concern with DNS servers like Cloudfare, however it would be better to configure a pool of DNS servers.
 - Connection pooling: the current implementation does not support connection pooling. I would make sense to add it to improve latency as TCP connections can be kept open and reused.
 - Usage statistics and a health endpoint could be exposed to monitor the service and collect metrics.
 - To reduce the application footprint it could be compiled as native with graalVM, however at the moment netty is not fully supported.


## Performance test

I run some quick performance tests to see multithreading at work. The baseline queries 1,000 times the Google DNS over TLS with 100 threads in parallel.

```
$ for i in {1..1000}; do 
    echo kdig -d @1.1.1.1 +tls-ca +tls-host=dns.google.com example.com >/dev/null
done | time parallel -j 100
```

Below a similar test for `dnstls`.
```
for i in {1..1000}; do 
    echo dig example.com @127.0.0.1 -p 8553 -b 127.0.0.1 +nocmd +noall +answer +nottlid +retry=0 +tcp; >/dev/null
done | time parallel -j 100
```

`dnstls` can serve the same number of requests in half the time with no requests lost because of better backend connection handling.

| Options | real | user | sys |
----------|------|------|-----|
| Baseline | 14.10 | 56.33 | 23.62 |
|  Default | 19.66 | 8.48 | 12.38 |
| TCP_NODELAY | 12.82 | 8.16 | 11.60 |
| TCP_NODELAY + SO_KEEPALIVE | 7.78 | 7.20 | 10.43 |
