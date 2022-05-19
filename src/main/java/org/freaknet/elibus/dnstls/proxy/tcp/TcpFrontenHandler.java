package org.freaknet.elibus.dnstls.proxy.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import lombok.RequiredArgsConstructor;

import org.freaknet.elibus.dnstls.proxy.BackendHandler;
import org.freaknet.elibus.dnstls.proxy.FrontenHandler;
import org.springframework.context.ApplicationContext;

@RequiredArgsConstructor
public class TcpFrontenHandler extends FrontenHandler {

    private final ApplicationContext context;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        
        final Channel inboundChannel = ctx.channel();

        BackendHandler handler = (TcpBackendHandler) context.getBean("tcpBackendHandler", inboundChannel);
        Bootstrap b = (Bootstrap) context.getBean("backendChannelBootstrap", inboundChannel, handler);
                    
        ChannelFuture f = b.connect();
        this.outboundChannel = f.channel();

        f.addListener(new ChannelFutureListenerImpl(inboundChannel));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        
        if (this.outboundChannel.isActive()) {
            this.outboundChannel
                .writeAndFlush(msg)
                .addListener(new ChannelFutureListenerImpl(ctx.channel()));
        }
    }
}