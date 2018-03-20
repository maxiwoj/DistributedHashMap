package distributedmap;

import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
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

public class DistributedStringMap extends ReceiverAdapter implements SimpleStringMap {
    private ProtocolStack protocolStack;
    private JChannel jChannel;
    private Map<String, String> state = new ConcurrentHashMap<>();

    public DistributedStringMap() throws Exception {
        initJGroupsConnection();
    }

    private void initJGroupsConnection() throws Exception {
//        System.setProperty("java.net.preferIPv4Stack", "true");
        jChannel = new JChannel(false);

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
                .addProtocol(new FRAG2());

        protocolStack.init();
        jChannel.setReceiver(this);
        jChannel.connect(NetworkConstants.CHANNEL_NAME);
        jChannel.getState();
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
        final String val = this.state.remove(key);
        updateDistr(new AbstractMap.SimpleEntry<>(key, null));
        return val;
    }

    @Override
    public void receive(Message msg) {
        System.out.println("Received message: " + msg.toString());
        final Map.Entry<String, String> entry = (Map.Entry<String, String>) msg.getObject();
        state.put(entry.getKey(), entry.getValue());
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("** view: " + view);
    }

    @Override
    public synchronized void getState(OutputStream output) throws Exception {
        Util.objectToStream(state, new DataOutputStream(output));
//        DictionaryProtos.Dictionary dict = DictionaryProtos.Dictionary.newBuilder().putAllType(hashMapState).build();
//        System.out.println(Arrays.toString(dict.getTypeMap().values().toArray()));
//        dict.writeTo(CodedOutputStream.newInstance(output));
//        super.getState(output);
    }

    @Override
    public synchronized void setState(InputStream input) throws Exception {
        Map<String,String> map=(Map<String,String>)Util.objectFromStream(new DataInputStream(input));
        state.clear();
        state.putAll(map);
        System.out.println("received state (" + map.size() + " messages in chat history):");
        for(String str: map.values()) {
            System.out.println(str);
        }
//        DictionaryProtos.Dictionary dict = DictionaryProtos.Dictionary.parseFrom(input);
//        System.out.println("State set: " + Arrays.toString(state.values().toArray()));
//        this.state.clear();
//        this.state.putAll(dict.getTypeMap());
//        super.setState(input);
    }

    private void updateDistr(Map.Entry<String, String> mapEntry){
        Message msg = new Message(null, null, mapEntry);
        try {
            jChannel.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        DictionaryProtos.Dictionary dict = DictionaryProtos.Dictionary.newBuilder().putAllType(this.receiver.getHashMapState()).build();
//        Message msg = new Message(null, null, dict);
//        try {
//            this.jChannel.send(msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
