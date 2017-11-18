commit a40e498e966e6f87078442e88a0033c20df47855
Author: Zhaohui Yu <yuyijq@hotmail.com>
Date:   Tue Sep 26 11:54:16 2017 +0800

    Merge pull request #596 from yuyijq:refactoroverridenotify

    这里每个服务export的时候都会订阅一个OverrideListener，那么configurators节点发生变动的时候，每个服务的OverrideListener都会触发，那每个服务的OverrideListener去修改自己就好了，为什么要把bounds里全拿出来呢？还增加了出bug的几率