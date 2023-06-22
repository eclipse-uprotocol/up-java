package org.eclipse.uprotocol.topic;

import java.net.InetAddress;

public class ShortUAuthority implements UAuthority<InetAddress> {

    private final InetAddress address;

    public ShortUAuthority(InetAddress address) {
        this.address = address;
    }

    @Override
    public String getAuthority() {
        return address.getHostAddress();
    }


}
