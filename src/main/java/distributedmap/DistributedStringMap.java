package distributedmap;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.STATE_TRANSFER;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DistributedStringMap extends ReceiverAdapter implements SimpleStringMap {
    private ProtocolStack protocolStack;
    private JChannel jChannel;
    private final Logger logger;
    private Map<String, String> state = new ConcurrentHashMap<>();

    public DistributedStringMap() throws Exception {
        logger = Logger.getLogger(this.getClass().getName());
        logger.setLevel(NetworkConstants.LOGGING_LEVEL);

        initJGroupsConnection();
    }

    private void initJGroupsConnection() throws Exception {

//        System.setProperty("java.net.preferIPv4Stack", "true");
        jChannel = new JChannel(false);

        initProtocolStack();
        jChannel.setReceiver(this);
        jChannel.connect(NetworkConstants.CHANNEL_NAME);
        jChannel.getState(null, 10000);
    }

    private void initProtocolStack() throws Exception {
        protocolStack = new ProtocolStack();
        jChannel.setProtocolStack(protocolStack);
        protocolStack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName(NetworkConstants.MULTICAST_IP_ADDRESS)))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new STATE_TRANSFER())
                .addProtocol(new FRAG2());

        protocolStack.init();
    }

    @Override
    public boolean containsKey(String key) {
        return this.state.containsKey(key);
    }

    @Override
    public String get(String key) {
        return this.state.get(key);
    }

    @Override
    public String put(String key, String value) {
        final String val = this.state.put(key, value);
        updateDistr(new AbstractMap.SimpleEntry<>(key, value));
        return val;
    }

    @Override
    public String remove(String key) {
//        TODO: Actual Remove
        final String val = this.state.remove(key);
        updateDistr(new AbstractMap.SimpleEntry<>(key, ""));
        return val;
    }

    @Override
    public void receive(Message msg) {
        logger.info("Received message: " + msg.toString());
        final Map.Entry<String, String> entry = (Map.Entry<String, String>) msg.getObject();
        if(entry.getValue().equals("")){
            state.remove(entry.getKey());
        } else {
            state.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void viewAccepted(View view) {
        logger.info("** nodes: " + view.getMembers());
    }

    @Override
    public synchronized void getState(OutputStream output) throws Exception {
        this.logger.info("Getting state");
        Util.objectToStream(state, new DataOutputStream(output));
    }

    @Override
    public synchronized void setState(InputStream input) throws Exception {
        Map<String,String> map=(Map<String,String>)Util.objectFromStream(new DataInputStream(input));
        state.clear();
        state.putAll(map);
        logger.info("received state (" + map.size() + " hashmap entries):");
    }

    private void updateDistr(Map.Entry<String, String> mapEntry){
        Message msg = new Message(null, null, mapEntry);
        try {
            jChannel.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
