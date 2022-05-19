package org.freaknet.elibus.dnstls.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Getter
public abstract class BackendHandler extends SimpleChannelInboundHandler<ByteBuf> {
    
    protected final Channel inboundChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        
        ctx.read();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        log.error(cause.getMessage());
        ctx.close();
    }

    protected static void closeOnFlush(Channel channel) {

        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
