package org.freaknet.elibus.dnstls.proxy.tcp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;

class TcpFrontendHandlerTest {

    ApplicationContext context = Mockito.mock(ApplicationContext.class);
    TcpBackendHandler handler = Mockito.mock(TcpBackendHandler.class);
    Bootstrap bootstrap = Mockito.mock(Bootstrap.class);
    ChannelFuture future = Mockito.mock(ChannelFuture.class);
    EmbeddedChannel outbound = new EmbeddedChannel();

    @BeforeEach
    void setup() {
        when(this.context.getBean(eq("tcpBackendHandler"), any(Channel.class))).thenReturn(this.handler);
        when(this.context.getBean(eq("backendChannelBootstrap"), any(Channel.class), eq(this.handler))).thenReturn(this.bootstrap);
        when(this.bootstrap.connect()).thenReturn(this.future);
        when(this.future.channel()).thenReturn(this.outbound);
    }

    @Test
    void tcpFrontendHandler_shouldWriteQueryToOutbound() {

        EmbeddedChannel channel = new EmbeddedChannel(new TcpFrontenHandler(this.context));

        String aString = "Some";
        channel.writeInbound(aString);

        assertThat(this.outbound.readOutbound(), is(aString));
    }
}
