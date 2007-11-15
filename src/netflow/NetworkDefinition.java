package netflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author slava
 * @version $Id $
 */
public class NetworkDefinition {
    private Integer networkId;
    private InetAddress networkAddress;
    private InetAddress netmask;
    private InetAddress returnAddress;
    private long na;
    private long nm;
    private static final Log log = LogFactory.getLog(NetworkDefinition.class);

    public NetworkDefinition(Integer nid, String network, String netmask, String returnAd) {
        try {
            this.networkId = nid;
            this.networkAddress = InetAddress.getByName(network);
            this.netmask = InetAddress.getByName(netmask);
            this.returnAddress = InetAddress.getByName(returnAd);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public NetworkDefinition(Integer nid, String network, String netmask) {
        try {
            this.networkId = nid;
            this.networkAddress = InetAddress.getByName(network);
            this.netmask = InetAddress.getByName(netmask);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public boolean isMyAddress(String address) {
        boolean result = false;
        try {
            InetAddress foreign = InetAddress.getByName(address);
            if (na == 0) {
                na = addrToLong(networkAddress);
            }
            if (nm == 0) {
                nm = addrToLong(netmask);
            }
            long fa = addrToLong(foreign);
            if ((na & nm) == (fa & nm)) {
                if (getBroadcastAddress() == fa) {
                    result = getBroadcastAddress() == na; // say, 10.0.4.1/32 - very rare and illegal, but used
                } else {
                    result = true;
                }
            }
            if (returnAddress != null && ! result){
                result = foreign.equals(returnAddress);
            }
        } catch (Exception e) {
            log.error("Bad host: " + e.getMessage());
        }
        return result;
    }

    public static long addrToLong(InetAddress address) {
        byte[] rawIP = address.getAddress();
        return ((rawIP[0] & 0xff) << 24 | (rawIP[1] & 0xff) << 16 |
                (rawIP[2] & 0xff) << 8 | (rawIP[3] & 0xff)) & 0xffffffffL;
    }

    private long getBroadcastAddress() {
        return ((na | (~(nm) & 0xff)));
    }

    private InetAddress getReadableBroadcast() throws UnknownHostException {
        Long addr = getBroadcastAddress();
        return InetAddress.getByName(addr.toString());
    }

    public Integer getNetworkId() {
        return networkId;
    }

    public String toString(){
        return networkId + ". [address=" + networkAddress + "; mask=" + netmask + "]";
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkDefinition that = (NetworkDefinition) o;

        if (!netmask.equals(that.netmask)) return false;
        if (!networkAddress.equals(that.networkAddress)) return false;
        if (!networkId.equals(that.networkId)) return false;
        return !(returnAddress != null ? !returnAddress.equals(that.returnAddress) : that.returnAddress != null);

        }

    public int hashCode() {
        int result;
        result = networkId.hashCode();
        result = 31 * result + networkAddress.hashCode();
        result = 31 * result + netmask.hashCode();
        result = 31 * result + (returnAddress != null ? returnAddress.hashCode() : 0);
        return result;
    }
}
