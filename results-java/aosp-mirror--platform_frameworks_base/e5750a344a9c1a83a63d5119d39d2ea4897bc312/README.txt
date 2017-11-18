commit e5750a344a9c1a83a63d5119d39d2ea4897bc312
Author: Chia-chi Yeh <chiachi@android.com>
Date:   Wed Aug 3 14:42:11 2011 -0700

    NativeDaemonConnector: offload callbacks to another thread.

    Now callbacks can communicate to the same daemon without causing a
    deadlock. This also improves the latency of calls because they no
    longer need to wait for the callbacks for the pending events.

    Change-Id: I153fcf16bd64de79ee1c1a57d3cfdb12b354cf47