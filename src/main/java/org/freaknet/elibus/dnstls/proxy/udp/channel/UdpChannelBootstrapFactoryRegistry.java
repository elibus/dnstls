package org.freaknet.elibus.dnstls.proxy.udp.channel;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class UdpChannelBootstrapFactoryRegistry {
    
    private final List<UdpChannelBootstrapFactory> bootstraps;
    
    /**
     * Get a UdpChannelBootstrapFactory supporting the platform.
     * 
     * @return a UdpChannelBootstrapFactory
     */
    public UdpChannelBootstrapFactory getFactory()  {

        String osname = System.getProperty("os.name");
        for (UdpChannelBootstrapFactory b : bootstraps) {
            if (b.supports(osname)) {
                return b;
            }
        }

        Assert.state(false, "No supported UdpServerBootstrapFactory.");
        
        return null;
    }
}
