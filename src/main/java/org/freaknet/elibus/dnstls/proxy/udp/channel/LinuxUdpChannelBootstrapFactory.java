package org.freaknet.elibus.dnstls.proxy.udp.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;

import java.net.InetSocketAddress;

import lombok.RequiredArgsConstructor;

import org.freaknet.elibus.dnstls.proxy.NettyEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(10)
public class LinuxUdpChannelBootstrapFactory implements UdpChannelBootstrapFactory {
    
    private final ApplicationContext context;

    @Value("${dnstls.udp.threads:8}")
    private Integer threads;

    private EventLoopGroup eventLoopGroup;

    @Override
    public Bootstrap getBootstrap(String host, int port) {

        final Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(getEventLoopGroup())
            .channel(NettyEnvironment.getDatagramChannelClass())
            .localAddress(new InetSocketAddress(host, port))
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .option(EpollChannelOption.SO_REUSEPORT, true)
            .option(ChannelOption.SO_RCVBUF, 512)
            .option(ChannelOption.AUTO_READ, false);

        return bootstrap;
    }

    @Override
    public EventLoopGroup getEventLoopGroup() {

        if (this.eventLoopGroup == null) {
            this.eventLoopGroup = (EventLoopGroup) context.getBean("eventLoopGroup", this.threads);
        }

        return this.eventLoopGroup;
    }

    @Override
    public boolean supports(String osname) {
        
        return osname.contains("nux");
    }
}
