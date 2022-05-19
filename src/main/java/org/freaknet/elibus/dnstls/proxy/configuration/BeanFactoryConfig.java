package org.freaknet.elibus.dnstls.proxy.configuration;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetSocketAddress;

import org.freaknet.elibus.dnstls.proxy.BackendChannelInitializer;
import org.freaknet.elibus.dnstls.proxy.BackendHandler;
import org.freaknet.elibus.dnstls.proxy.NettyEnvironment;
import org.freaknet.elibus.dnstls.proxy.tcp.TcpBackendHandler;
import org.freaknet.elibus.dnstls.proxy.udp.UdpBackendHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class BeanFactoryConfig {

    private static final boolean OS_LINUX = System.getProperty("os.name").contains("inux");

    @Value("${dnstls.dns.host:1.1.1.1}")
    private String host;

    @Value("${dnstls.dns.port:853}")
    private Integer port;

    @Bean("udpBackendHandler")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected UdpBackendHandler createUdpBackendHandler(Channel inbound, InetSocketAddress sender) {

        return new UdpBackendHandler(inbound, sender);
    }

    @Bean("tcpBackendHandler")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected TcpBackendHandler createTcpBackendHandler(Channel inbound) {

        return new TcpBackendHandler(inbound);
    }

    @Bean("eventLoopGroup")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected EventLoopGroup createEventLoopGroup(int threads) {

        return OS_LINUX ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
    }

    @Bean("backendChannelBootstrap")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected Bootstrap createBackendChannelBootstrap(Channel channel, BackendHandler handler) {

        final Bootstrap bootstrap = new Bootstrap();

        return bootstrap
            .group(channel.eventLoop())
            .channel(NettyEnvironment.getSocketChannelClass())
            .remoteAddress(this.host, this.port)
            .handler(new BackendChannelInitializer(this.host, this.port, handler))
            .option(ChannelOption.AUTO_READ, false)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true);
    }
}