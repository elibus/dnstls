package org.freaknet.elibus.dnstls.proxy.tcp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.charset.Charset;

import org.freaknet.elibus.dnstls.proxy.testing.EchoHandler;
import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;

class TcpBackendHandlerTest {
   

    EmbeddedChannel inboundChannel = new EmbeddedChannel(new EchoHandler());

    @Test
    void tcpBackendHandler_shouldWriteResponseToInbound() {

        EmbeddedChannel channel = new EmbeddedChannel(new TcpBackendHandler(this.inboundChannel));

        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeCharSequence("Some", Charset.defaultCharset());
        channel.writeInbound(buffer);

        assertThat(this.inboundChannel.readOutbound(), is(buffer));
        buffer.release();
    }
}
