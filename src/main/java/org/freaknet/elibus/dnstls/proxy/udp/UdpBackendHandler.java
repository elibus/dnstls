package org.freaknet.elibus.dnstls.proxy.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.freaknet.elibus.dnstls.proxy.BackendHandler;

@Slf4j
@Getter
public class UdpBackendHandler extends BackendHandler {

    private final InetSocketAddress sender;

    /**
     * Concrete implementation of a BackendHandler to proxy requests from the frontend to a backend DNS.
     * 
     * @param inboundChannel the inbound channel originating the request
     * @param sender the address of the request sender
     */
    public UdpBackendHandler(Channel inboundChannel,  InetSocketAddress sender) {

        super(inboundChannel);
        this.sender = sender;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        
        // Discard first two bytes of the TCP reply
        msg.readByte();
        msg.readByte();
     
        inboundChannel.writeAndFlush(new DatagramPacket(msg.retain(), this.sender))
            .addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        log.error("fail write: {}", future.cause());
                        future.channel().close();
                    }
                }
            });
    }
}
