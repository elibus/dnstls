package org.freaknet.elibus.dnstls.proxy.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ApplicationContext context;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        // TcpFrontenHandler cannot be a singleton because is not shareable
        channel.pipeline().addLast(new TcpFrontenHandler(this.context));
    }
}
