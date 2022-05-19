package org.freaknet.elibus.dnstls.proxy.configuration;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;

import org.freaknet.elibus.dnstls.proxy.BackendChannelInitializer;
import org.freaknet.elibus.dnstls.proxy.BackendHandler;
import org.freaknet.elibus.dnstls.proxy.tcp.TcpBackendHandler;
import org.freaknet.elibus.dnstls.proxy.udp.UdpBackendHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

@SpringBootTest(
    properties = { "host=1.1.1.1", "port=8553" }
)
class BeanFactoryConfigurationTest {

    @Autowired
    public ApplicationContext applicationContext;
    
    private BackendHandler handler;
    private Channel channel;
    private EventLoop eventLoop;

    @BeforeEach
    void setup() {
        
        this.handler = Mockito.mock(BackendHandler.class);
        this.channel = Mockito.mock(Channel.class);
        this.eventLoop = Mockito.mock(EventLoop.class);
        when(this.channel.eventLoop()).thenReturn(this.eventLoop);
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void createEventLoopGroup_whenLinux_shouldUseEpollEventLoopGroup() {

        var actual = (EventLoopGroup) this.applicationContext.getBean("eventLoopGroup", 1);

        assertThat(actual, instanceOf(EpollEventLoopGroup.class));
    }

    @Test
    @DisabledOnOs(OS.LINUX)
    void createEventLoopGroup_whenNotLinus_shouldUseNioEventLoopGroup() {

        var actual = (NioEventLoopGroup) this.applicationContext.getBean("eventLoopGroup", 1);

        assertThat(actual, instanceOf(NioEventLoopGroup.class));
    }

    @Test
    void createTcpBackendHandler_shouldSetInboundChannel() {

        var expected = this.channel;
        var handler = (TcpBackendHandler) this.applicationContext.getBean("tcpBackendHandler", expected);

        var actual = handler.getInboundChannel();

        assertThat(actual, is(expected));
    }

    @Test
    void createUdpBackendHandler_shouldSetInboundChannel() {
        
        var expected = this.channel;
        var handler = (UdpBackendHandler) this.applicationContext.getBean("udpBackendHandler", expected, null);

        var actual = handler.getInboundChannel();

        assertThat(actual, is(expected));
    }

    @Test
    void createUdpBackendHandler_shouldSetSender() {
        
        var expected = new InetSocketAddress("1.1.1.1", 853);
        var handler = (UdpBackendHandler) this.applicationContext.getBean("udpBackendHandler", null, expected);

        var actual = handler.getSender();

        assertThat(actual, is(expected));
    }

    @Test
    void createBackendChannelBootstrap_shouldSetEventLoop() {

        var expected = this.eventLoop;
        var bootstrap = (Bootstrap) this.applicationContext.getBean("backendChannelBootstrap", this.channel, this.handler);

        var actual = bootstrap.config().group();

        assertThat(actual, is(expected));
    }

    @Test
    void createBackendChannelBootstrap_shouldSetRemoteAddress() {

        var bootstrap = (Bootstrap) this.applicationContext.getBean("backendChannelBootstrap", this.channel, this.handler);

        var actual = (InetSocketAddress) bootstrap.config().remoteAddress();

        assertThat(actual.getHostName(), is("1.1.1.1"));
        assertThat(actual.getPort(), is(853));
    }

    @Test
    void createBackendChannelBootstrap_shouldSetHandler() {

        var bootstrap = (Bootstrap) this.applicationContext.getBean("backendChannelBootstrap", this.channel, this.handler);

        var actual = bootstrap.config().handler();

        assertThat(actual, instanceOf(BackendChannelInitializer.class));
    }

    @Test
    void createBackendChannelBootstrap_shouldSetAutoReadToFalse() {

        var bootstrap = (Bootstrap) this.applicationContext.getBean("backendChannelBootstrap", this.channel, this.handler);

        var actual = bootstrap.config().options().get(ChannelOption.AUTO_READ);

        assertThat(actual, is(false));
    }
}
