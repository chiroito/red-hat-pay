package rhpay.monitoring;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;

@Label("CallDistributedTask")
@Category({"RedHatPay", "DataGrid"})
public class CallDistributedTaskEvent extends Event {

    @Label("segment")
    private final int segment;

    @Label("doing")
    private final String doing;

    public CallDistributedTaskEvent(int segment, String doing) {
        this.segment = segment;
        this.doing = doing;
    }
}
