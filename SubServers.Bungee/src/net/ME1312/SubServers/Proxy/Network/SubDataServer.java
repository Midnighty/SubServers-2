package net.ME1312.SubServers.Proxy.Network;

import net.ME1312.SubServers.Proxy.Library.Exception.IllegalPacketException;
import net.ME1312.SubServers.Proxy.Library.Version.Version;
import net.ME1312.SubServers.Proxy.Network.Packet.*;
import net.ME1312.SubServers.Proxy.SubPlugin;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * SubDataServer Class
 *
 * @author ME1312
 */
public final class SubDataServer {
    private static HashMap<Class<? extends PacketOut>, String> pOut = new HashMap<Class<? extends PacketOut>, String>();
    private static HashMap<String, PacketIn> pIn = new HashMap<String, PacketIn>();
    private static List<InetAddress> allowedAddresses = new ArrayList<InetAddress>();
    private static boolean defaults = false;
    private HashMap<InetSocketAddress, Client> clients = new HashMap<InetSocketAddress, Client>();
    private ServerSocket server;
    private SubPlugin plugin;

    /**
     * SubData Server Instance
     *
     * @param plugin SubPlugin
     * @param port Port
     * @param backlog Connection Queue
     * @param address Bind Address
     * @throws IOException
     */
    public SubDataServer(SubPlugin plugin, int port, int backlog, InetAddress address) throws IOException {
        server = new ServerSocket(port, backlog, address);
        this.plugin = plugin;

        allowConnection(address);
        if (!defaults) loadDefaults();
    }

    private void loadDefaults() {
        defaults = true;
        for (String s : plugin.config.get().getSection("Settings").getSection("SubData").getStringList("Allowed-Connections")) {
            try {
                allowedAddresses.add(InetAddress.getByName(s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        registerPacket(new PacketAuthorization(plugin), "Authorization");
        registerPacket(new PacketDownloadBuildScript(plugin), "DownloadBuildScript");
        registerPacket(new PacketDownloadHostInfo(plugin), "DownloadHostInfo");
        registerPacket(new PacketDownloadLang(plugin), "DownloadLang");
        registerPacket(new PacketDownloadPlayerList(plugin), "DownloadPlayerList");
        registerPacket(new PacketDownloadServerInfo(plugin), "DownloadServerInfo");
        registerPacket(new PacketDownloadServerList(plugin), "DownloadServerList");
        registerPacket(new PacketInfoPassthrough(plugin), "InfoPassthrough");
        registerPacket(new PacketLinkServer(plugin), "LinkServer");

        registerPacket(PacketAuthorization.class, "Authorization");
        registerPacket(PacketDownloadBuildScript.class, "DownloadBuildScript");
        registerPacket(PacketDownloadHostInfo.class, "DownloadHostInfo");
        registerPacket(PacketDownloadLang.class, "DownloadLang");
        registerPacket(PacketDownloadPlayerList.class, "DownloadPlayerList");
        registerPacket(PacketDownloadServerInfo.class, "DownloadServerInfo");
        registerPacket(PacketDownloadServerList.class, "DownloadServerList");
        registerPacket(PacketInfoPassthrough.class, "InfoPassthrough");
        registerPacket(PacketLinkServer.class, "LinkServer");
    }

    /**
     * Gets the Server Socket
     *
     * @return Server Socket
     */
    public ServerSocket getServer() {
        return server;
    }

    /**
     * Add a Client to the Network
     *
     * @param socket Client to add
     * @throws IOException
     */
    public Client addClient(Socket socket) throws IOException {
        if (allowedAddresses.contains(socket.getInetAddress())) {
            Client client = new Client(plugin, socket);
            System.out.println("SubData > " + client.getAddress().toString() + " has connected");
            clients.put(client.getAddress(), client);
            return client;
        } else {
            System.out.println("SubData > " + socket.getInetAddress().toString() + ":" + socket.getPort() + " attempted to connect, but isn't whitelisted");
            socket.close();
            return null;
        }
    }

    /**
     * Grabs a Client from the Network
     *
     * @param socket Socket to search
     * @return Client
     */
    public Client getClient(Socket socket) {
        return clients.get(new InetSocketAddress(socket.getInetAddress(), socket.getPort()));
    }

    /**
     * Grabs a Client from the Network
     *
     * @param address Address to search
     * @return Client
     */
    public Client getClient(InetSocketAddress address) {
        return clients.get(address);
    }

    /**
     * Remove a Client from the Network
     *
     * @param client Client to Kick
     * @throws IOException
     */
    public void removeClient(Client client) throws IOException {
        SocketAddress address = client.getAddress();
        if (clients.keySet().contains(address)) {
            clients.remove(address);
            client.disconnect();
            System.out.println("SubData > " + client.getAddress().toString() + " has disconnected");
        }
    }

    /**
     * Remove a Client from the Network
     *
     * @param address Address to Kick
     * @throws IOException
     */
    public void removeClient(InetSocketAddress address) throws IOException {
        Client client = clients.get(address);
        if (clients.keySet().contains(address)) {
            clients.remove(address);
            client.disconnect();
            System.out.println("SubData > " + client.getAddress().toString() + " has disconnected");
        }
    }

    /**
     * Register PacketIn to the Network
     *
     * @param packet PacketIn to register
     * @param handle Handle to Bind
     */
    public static void registerPacket(PacketIn packet, String handle) {
        if (!pIn.keySet().contains(handle)) {
            pIn.put(handle, packet);
        } else {
            throw new IllegalStateException("PacketIn Handle \"" + handle + "\" is already in use!");
        }
    }

    /**
     * Register PacketOut to the Network
     *
     * @param packet PacketOut to register
     * @param handle Handle to bind
     */
    public static void registerPacket(Class<? extends PacketOut> packet, String handle) {
        if (!pOut.values().contains(handle)) {
            pOut.put(packet, handle);
        } else {
            throw new IllegalStateException("PacketOut Handle \"" + handle + "\" is already in use!");
        }
    }

    /**
     * Grab PacketIn Instance via handle
     *
     * @param handle Handle
     * @return PacketIn
     */
    public static PacketIn getPacket(String handle) {
        return pIn.get(handle);
    }

    /**
     * Broadcast a Packet to everything on the Network
     * <b>Warning:</b> There are usually different types of applications on the network at once, they may not recognise the same packet handles
     *
     * @param packet Packet to send
     */
    public void broadcastPacket(PacketOut packet) {
        for (Client client : clients.values()) {
            client.sendPacket(packet);
        }
    }

    /**
     * Allow Connections from an Address
     *
     * @param address Address to allow
     */
    public static void allowConnection(InetAddress address) {
        if (!allowedAddresses.contains(address)) allowedAddresses.add(address);
    }

    /**
     * Deny Connections from an Address
     *
     * @param address Address to deny
     */
    public static void denyConnection(InetAddress address) {
        allowedAddresses.remove(address);
    }

    /**
     * JSON Encode PacketOut
     *
     * @param packet PacketOut
     * @return JSON Formatted Packet
     * @throws IllegalPacketException
     */
    protected static JSONObject encodePacket(PacketOut packet) throws IllegalPacketException {
        JSONObject json = new JSONObject();

        if (!pOut.keySet().contains(packet.getClass())) throw new IllegalPacketException("Unknown PacketOut Channel: " + packet.getClass().getCanonicalName());
        if (packet.getVersion().toString() == null) throw new NullPointerException("PacketOut Version cannot be null: " + packet.getClass().getCanonicalName());

        JSONObject contents = packet.generate();
        json.put("h", pOut.get(packet.getClass()));
        json.put("v", packet.getVersion().toString());
        if (contents != null) json.put("c", contents);
        return json;
    }

    /**
     * JSON Decode PacketIn
     *
     * @param json JSON to Decode
     * @return PacketIn
     * @throws IllegalPacketException
     * @throws InvocationTargetException
     */
    protected static PacketIn decodePacket(JSONObject json) throws IllegalPacketException, InvocationTargetException {
        if (!json.keySet().contains("h") || !json.keySet().contains("v")) throw new IllegalPacketException("Unknown Packet Format: " + json.toString());
        if (!pIn.keySet().contains(json.getString("h"))) throw new IllegalPacketException("Unknown PacketIn Channel: " + json.getString("h"));

        PacketIn packet = pIn.get(json.getString("h"));
        if (!new Version(json.getString("v")).equals(packet.getVersion())) throw new IllegalPacketException("Packet Version Mismatch in " + json.getString("h") + ": " + json.getString("v") + "->" + packet.getVersion().toString());
        return packet;
    }

    /**
     * Drops All Connections and Stops the SubData Listener
     *
     * @throws IOException
     */
    public void destroy() throws IOException {
        while(clients.size() > 0) {
            removeClient((Client) clients.values().toArray()[0]);
        }
        server.close();
        System.out.println("SubServers > The SubData Listener has been closed");
        plugin.subdata = null;
    }
}