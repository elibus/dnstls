package org.freaknet.elibus.dnstls.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BackendChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final String host;
    private final int port;
    private final BackendHandler proxyBackendHandler;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        SslContext sslContext = SslContextBuilder.forClient().build();
        SslHandler sslHandler = sslContext.newHandler(channel.alloc(), this.host, this.port);
        
        channel.pipeline().addLast(sslHandler);
        channel.pipeline().addLast(proxyBackendHandler);
    }
}
