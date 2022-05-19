package org.freaknet.elibus.dnstls.proxy.udp.channel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

class DefaultUdpChannelBootstrapFactoryTest {
 
    ApplicationContext context = Mockito.mock(ApplicationContext.class);
    EventLoopGroup eventLoop = Mockito.mock(EventLoopGroup.class);
    DefaultUdpChannelBootstrapFactory factory = new DefaultUdpChannelBootstrapFactory(this.context);

    Bootstrap bootstrap;

    @BeforeEach
    void setup() {
        
        when(this.context.getBean(eq("eventLoopGroup"), ArgumentMatchers.<Integer>any())).thenReturn(this.eventLoop);

        this.bootstrap = factory.getBootstrap("8.8.8.8", 853);
    }

    @Test
    void getBootstrap_shouldSetEventLoopGroup() {

        EventLoopGroup actual = this.bootstrap.config().group();
        EventLoopGroup expected = this.eventLoop;

        assertThat(actual, is(expected));
    }

    @Test
    void getBootstrap_shouldSetAllocator() {

        Map<ChannelOption<?>, Object> options = this.bootstrap.config().options();
        
        var expected = PooledByteBufAllocator.DEFAULT;
        var actual = (PooledByteBufAllocator) options.get(ChannelOption.ALLOCATOR);

        assertThat(expected, is(actual));
    }
    
    @Test
    void getBootstrap_shouldSetRcvBufferSize() {

        Map<ChannelOption<?>, Object> options = this.bootstrap.config().options();

        var expected = 512;
        var actual = (int) options.get(ChannelOption.SO_RCVBUF);

        assertThat(expected, is(actual));
    }

    @Test
    void getBootstrap_shouldSetAutoReadToFalse() {

        Map<ChannelOption<?>, Object> options = this.bootstrap.config().options();

        var expected = false;
        var actual = (boolean) options.get(ChannelOption.AUTO_READ);

        assertThat(expected, is(actual));
    }

    @Test
    void supports_shouldBeTrueForAnyOS() {

        boolean actual = this.factory.supports("osname");

        assertTrue(actual);
    }
}
