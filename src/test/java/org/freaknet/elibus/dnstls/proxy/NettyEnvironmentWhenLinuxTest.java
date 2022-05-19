package org.freaknet.elibus.dnstls.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;

@EnabledOnOs(OS.LINUX)
class NettyEnvironmentWhenLinuxTest {
    
    @Test
    void test_getSocketChannel() {

        var actual = NettyEnvironment.getSocketChannelClass();
        var expected = EpollSocketChannel.class;

        assertThat(actual, is(expected));
    }
    
    @Test
    void test_getServerSocketChannel() {

        var actual = NettyEnvironment.getServerSocketChannelClass();
        var expected = EpollServerSocketChannel.class;

        assertThat(actual, is(expected));
    }

    @Test
    void test_getDatagramChannelClass() {

        var actual = NettyEnvironment.getDatagramChannelClass();
        var expected = EpollDatagramChannel.class;

        assertThat(actual, is(expected));
    }
}
