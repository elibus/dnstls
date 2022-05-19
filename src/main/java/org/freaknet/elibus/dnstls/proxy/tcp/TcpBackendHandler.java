package org.freaknet.elibus.dnstls.proxy.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.freaknet.elibus.dnstls.proxy.BackendHandler;

@Slf4j
@Getter
public class TcpBackendHandler extends BackendHandler {

    public TcpBackendHandler(Channel inboundChannel) {
        super(inboundChannel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        closeOnFlush(this.inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        super.exceptionCaught(ctx, cause);
        closeOnFlush(this.inboundChannel);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        inboundChannel.writeAndFlush(msg.retain()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ctx.channel().close();
                } else {
                    log.error("fail write: {}", future.cause());
                    future.channel().close();
                }
            }
        });
    }
}
