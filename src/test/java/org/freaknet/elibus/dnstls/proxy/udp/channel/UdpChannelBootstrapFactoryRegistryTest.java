package org.freaknet.elibus.dnstls.proxy.udp.channel;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;

class UdpChannelBootstrapFactoryRegistryTest {

    private final List<UdpChannelBootstrapFactory> bootstraps = List.of(
            new MockChannelBootstrapFactory(),
            new MockTrueChannelBootstrapFactory()
    );
    
    @Test
    void getFactory_shouldReturnTheCorrectMock() {

        var registry = new UdpChannelBootstrapFactoryRegistry(this.bootstraps);
        var actual = registry.getFactory();
        assertThat(actual, instanceOf(MockTrueChannelBootstrapFactory.class));
    }

    private class MockTrueChannelBootstrapFactory extends MockChannelBootstrapFactory {
        @Override
        public boolean supports(String osname) {
            return true;
        }
    }

    private class MockChannelBootstrapFactory implements UdpChannelBootstrapFactory {

        @Override
        public boolean supports(String osname) {
            return false;
        }

        @Override
        public Bootstrap getBootstrap(String host, int port) {
            return null;
        }

        @Override
        public EventLoopGroup getEventLoopGroup() {
            return null;
        }
    }
}
