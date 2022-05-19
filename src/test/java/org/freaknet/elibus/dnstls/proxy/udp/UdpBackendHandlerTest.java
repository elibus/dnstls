package org.freaknet.elibus.dnstls.proxy.udp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.freaknet.elibus.dnstls.proxy.testing.EchoHandler;
import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;

class UdpBackendHandlerTest {
   

    InetSocketAddress sender = new InetSocketAddress("1.1.1.1", 853);
    EmbeddedChannel inboundChannel = new EmbeddedChannel(new EchoHandler());

    @Test
    void tcpBackendHandler_shouldWriteTcpResponseToInboundAsUdpResponse() {

        EmbeddedChannel channel = new EmbeddedChannel(new UdpBackendHandler(this.inboundChannel, this.sender));

        ByteBuf tcpResponse = UnpooledByteBufAllocator.DEFAULT.buffer();
        // add two bytes for the TCP response
        tcpResponse.writeByte(0);
        tcpResponse.writeByte(0);
        tcpResponse.writeCharSequence("Some", Charset.defaultCharset());

        ByteBuf expectedUdpResponse = UnpooledByteBufAllocator.DEFAULT.buffer();
        expectedUdpResponse.writeCharSequence("Some", Charset.defaultCharset());

        channel.writeInbound(tcpResponse);

        DatagramPacket actualUdpResponse = (DatagramPacket) this.inboundChannel.readOutbound();

        assertThat(actualUdpResponse.content(), equalTo(expectedUdpResponse));
        assertThat(actualUdpResponse.recipient(), is(this.sender));

        tcpResponse.release();
        expectedUdpResponse.release();
    }
}
