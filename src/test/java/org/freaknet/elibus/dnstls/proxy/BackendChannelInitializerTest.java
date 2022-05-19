package org.freaknet.elibus.dnstls.proxy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

class BackendChannelInitializerTest {
    
    SocketChannel channel = Mockito.mock(SocketChannel.class);
    ChannelPipeline pipeline = Mockito.mock(ChannelPipeline.class);
    BackendHandler backendHandler = Mockito.mock(BackendHandler.class);

    @BeforeEach
    void setup() {

        when(this.channel.pipeline()).thenReturn(this.pipeline);
        when(this.pipeline.addLast(any())).thenReturn(null);
    }

    @Test
    void initChannel_shouldConfigureSsl() throws Exception {

        var backendChannelInitializer = new BackendChannelInitializer("1.1.1.1", 853, backendHandler);
        backendChannelInitializer.initChannel(channel);
        
        verify(pipeline, times(1)).addLast(any(SslHandler.class));
    }

    @Test
    void initChannel_shouldConfigureHandler() throws Exception {

        var backendChannelInitializer = new BackendChannelInitializer("1.1.1.1", 853, backendHandler);
        backendChannelInitializer.initChannel(channel);

        verify(pipeline, times(1)).addLast(backendHandler);
    }
}
