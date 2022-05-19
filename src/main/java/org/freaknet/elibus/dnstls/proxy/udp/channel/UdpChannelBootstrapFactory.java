package org.freaknet.elibus.dnstls.proxy.udp.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;

public interface UdpChannelBootstrapFactory {
    
    public boolean supports(String osname);

    public Bootstrap getBootstrap(String host, int port);
    
    public EventLoopGroup getEventLoopGroup();
}
