package org.freaknet.elibus.dnstls.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public abstract class FrontenHandler extends ChannelInboundHandlerAdapter {

    protected Channel outboundChannel;

    @Override
    public abstract void channelActive(ChannelHandlerContext ctx);
    
    @Override
    public abstract void channelRead(ChannelHandlerContext ctx, Object msg);
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    private static void closeOnFlush(Channel channel) {

        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @RequiredArgsConstructor
    public final class ChannelFutureListenerImpl implements ChannelFutureListener {

        private final Channel channel;

        @Override
        public void operationComplete(ChannelFuture future) {
            if (future.isSuccess()) {
                this.channel.read();
            } else {
                future.channel().close();
            }
        }
    }
}