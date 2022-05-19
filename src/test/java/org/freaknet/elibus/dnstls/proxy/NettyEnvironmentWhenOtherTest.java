package org.freaknet.elibus.dnstls.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

@DisabledOnOs(OS.LINUX)
class NettyEnvironmentWhenOtherTest {
    
    @Test
    void test_getSocketChannel() {

        var actual = NettyEnvironment.getSocketChannelClass();
        var expected = NioSocketChannel.class;

        assertThat(actual, is(expected));
    }
    
    @Test
    void test_getServerSocketChannel() {

        var actual = NettyEnvironment.getServerSocketChannelClass();
        var expected = NioServerSocketChannel.class;

        assertThat(actual, is(expected));
    }

    @Test
    void test_getDatagramChannelClass() {

        var actual = NettyEnvironment.getDatagramChannelClass();
        var expected = NioDatagramChannel.class;

        assertThat(actual, is(expected));
    }
}
