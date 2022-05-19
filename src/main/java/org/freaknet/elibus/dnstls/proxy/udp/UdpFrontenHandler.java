package org.freaknet.elibus.dnstls.proxy.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.freaknet.elibus.dnstls.proxy.FrontenHandler;
import org.springframework.context.ApplicationContext;

@Slf4j
@RequiredArgsConstructor
public class UdpFrontenHandler extends FrontenHandler {

    private final ApplicationContext context;
    private final ExecutorService executorService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        Callable<Void> callableTask = () -> {

            final Channel inboundChannel = ctx.channel();
            DatagramPacket packet = (DatagramPacket) msg;

            UdpBackendHandler handler = 
                (UdpBackendHandler) context.getBean("udpBackendHandler", inboundChannel, packet.sender());
            Bootstrap bootstrap = (Bootstrap) context.getBean("backendChannelBootstrap", inboundChannel, handler);

            ChannelFuture f = bootstrap.connect();
            this.outboundChannel = f.channel();

            ByteBuf dnsQuery = packet.content();
            ByteBuf buf = buildTcpRequest(ctx, dnsQuery, dnsQuery.readableBytes());
            dnsQuery.release();
            
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        outboundChannel.writeAndFlush(buf).addListener(new ChannelFutureListenerImpl(ctx.channel()));
                    } else {
                        log.info("fail write: {}", future.cause());
                        future.channel().close();
                    }
                }
            });

            return null;
        };
        
        executorService.submit(callableTask);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        ctx.read();
    }

    // Add two bytes (request size) to create a TCP dns request out of a UDP request
    private ByteBuf buildTcpRequest(ChannelHandlerContext ctx, ByteBuf content, int readableBytes) {

        byte[] requestSize = new byte[2];
        requestSize[1] = (byte) (readableBytes & 0xFF);
        requestSize[0] = (byte) ((readableBytes >> 8) & 0xFF);
        
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(requestSize);
        buf.writeBytes(content);
        
        return buf;
    }
}

