package org.freaknet.elibus.dnstls.proxy;

import io.netty.channel.Channel;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NettyEnvironment {
    
    private static final boolean OS_LINUX = System.getProperty("os.name").contains("inux");

    public static Class<? extends SocketChannel> getSocketChannelClass() {

        return OS_LINUX ? EpollSocketChannel.class : NioSocketChannel.class;
    }

    public static Class<? extends ServerSocketChannel> getServerSocketChannelClass() {

        return OS_LINUX ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    public static Class<? extends Channel> getDatagramChannelClass() {
            
        return OS_LINUX ? EpollDatagramChannel.class : NioDatagramChannel.class;
    }
}
