package org.freaknet.elibus.dnstls.proxy.udp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;

import java.util.concurrent.ExecutorService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UdpChannelInitializer extends ChannelInitializer<DatagramChannel> {
    
    private final ApplicationContext context;
    private final ExecutorService executorService;

    @Override
    protected void initChannel(DatagramChannel channel) throws Exception {
        
        // UdpFrontenHandler cannot be a singleton because is not shareable
        channel.pipeline().addLast(new UdpFrontenHandler(this.context, this.executorService));
    }
}
