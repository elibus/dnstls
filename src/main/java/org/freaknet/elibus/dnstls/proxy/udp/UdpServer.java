package org.freaknet.elibus.dnstls.proxy.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.freaknet.elibus.dnstls.proxy.udp.channel.UdpChannelBootstrapFactory;
import org.freaknet.elibus.dnstls.proxy.udp.channel.UdpChannelBootstrapFactoryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UdpServer {

    private EventLoopGroup workerGroup;
    private final UdpChannelInitializer udpChannelInitializer;
    private final UdpChannelBootstrapFactoryRegistry udpServerBootstrapFactoryRegistry;

    @Value("${dnstls.udp.threads:1}")
    private Integer threads;

    @Value("${dnstls.udp.host:0.0.0.0}")
    private String host;

    @Value("${dnstls.udp.port:8553}")
    private Integer port;

    /**
     * Starts an UDP frontend server.
     * 
     * @throws InterruptedException if cannot sync().
     */
    @PostConstruct
    public void start() throws InterruptedException {

        UdpChannelBootstrapFactory udpServerBootstrap = udpServerBootstrapFactoryRegistry.getFactory();
        this.workerGroup = udpServerBootstrap.getEventLoopGroup();

        Bootstrap bootstrap = udpServerBootstrap.getBootstrap(this.host, this.port);
        bootstrap.handler(this.udpChannelInitializer);

        for (int i = 0; i < this.threads; ++i) {
            ChannelFuture future = bootstrap.bind().await();
            if (future.isSuccess()) {
                log.info("UDP server #{} started on {}:{}.", i, this.host, this.port);
            } else {
                log.error("Fail to bind on port {}:{}: {}", this.host, this.port, future.cause());
            }
        }
    }

    /**
     * Shutdown gracefully.
     * 
     * @throws InterruptedException if cannot sync().
     */
    @PreDestroy
    public void destroy() throws InterruptedException {

        this.workerGroup.shutdownGracefully().sync();
        log.info("UDP server shutdown.");
    }
}
