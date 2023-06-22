package org.eclipse.uprotocol.topic;

public class LongUAuthority implements UAuthority<String> {

    public LongUAuthority(org.eclipse.uprotocol.uri.datamodel.UAuthority uAuthority) {
        this.uAuthority = uAuthority;
    }

    private final org.eclipse.uprotocol.uri.datamodel.UAuthority uAuthority;

    public String device() {
        return uAuthority.device().orElse("");
    }

    public String domain() {
        return uAuthority.domain().orElse("");
    }
    public String getAuthority() {
        return uAuthority.toString();
    }
}
