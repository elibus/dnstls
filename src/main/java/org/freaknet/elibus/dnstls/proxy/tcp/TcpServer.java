package org.freaknet.elibus.dnstls.proxy.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.freaknet.elibus.dnstls.proxy.NettyEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TcpServer {

    private final ApplicationContext context;
    private final TcpChannelInitializer tcpChannelInitializer;

    @Value("${dnstls.tcp.threads:8}")
    private Integer threads;

    @Value("${dnstls.tcp.host:0.0.0.0}")
    private String host;

    @Value("${dnstls.tcp.port:8553}")
    private Integer port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Start the TCP frontend server.
     * 
     * @throws InterruptedException if fails to sync().
     */
    @PostConstruct
    public void start() throws InterruptedException {

        this.bossGroup = (EventLoopGroup) context.getBean("eventLoopGroup", 1);
        this.workerGroup = (EventLoopGroup) context.getBean("eventLoopGroup", this.threads);

        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
            .channel(NettyEnvironment.getServerSocketChannelClass())
            .localAddress(new InetSocketAddress(this.host, this.port))
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.AUTO_READ, false)
            .childHandler(this.tcpChannelInitializer);

        ChannelFuture future = bootstrap.bind().sync();

        if (future.isSuccess()) {
            log.info("TCP server started on: {}:{}. Threads: {}", this.host, this.port, this.threads);
        }
    }

    /**
     * Shutdown gracefully.
     * 
     * @throws InterruptedException if cannot sync().
     */
    @PreDestroy
    public void destroy() throws InterruptedException {

        this.bossGroup.shutdownGracefully().sync();
        this.workerGroup.shutdownGracefully().sync();
        log.info("TCP server shutdown.");
    }
}
