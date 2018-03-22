package distributedmap;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.View;

import java.util.Collections;
import java.util.List;

public class MergeViewHandler extends Thread {

    private JChannel jChannel;
    private MergeView view;

    public MergeViewHandler(JChannel jChannel, MergeView view) {

        this.jChannel = jChannel;
        this.view = view;
    }

    @Override
    public void run() {
        List<View> subgroups= Collections.synchronizedList(view.getSubgroups());
        View tmp_view=subgroups.get(0); // picks the first
        Address local_addr=jChannel.getAddress();
        if(!tmp_view.getMembers().contains(local_addr)) {
            System.out.println("Not member of the new primary partition (" + tmp_view + "), will re-acquire the state");
            try {
                jChannel.getState(null, 30000);
            }
            catch(Exception ex) {
            }
        }
        else {
            System.out.println("Not member of the new primary partition (" + tmp_view + "), will do nothing");
        }
    }
}
