package Webserver;

// Peer class to represent a registered peer
public class Peer {
    private final String ip;
    private final int port;

    public Peer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Peer peer = (Peer) obj;
        return port == peer.port && ip.equals(peer.ip);
    }

    @Override
    public int hashCode() {
        return ip.hashCode() + port;
    }

    @Override
    public String toString() {
        return "Peer{" + "ip='" + ip + '\'' + ", port=" + port + '}';
    }
}
