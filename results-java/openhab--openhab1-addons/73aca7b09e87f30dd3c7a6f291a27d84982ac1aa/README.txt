commit 73aca7b09e87f30dd3c7a6f291a27d84982ac1aa
Author: wezhunter <wezhunter86@gmail.com>
Date:   Thu Oct 9 11:57:47 2014 +0200

    Z-Wave: Node Deletion - improve FailedNode logic

    Remove unnecessary call to delete node xml - already done on success
    event at controller level.
    Allow a FAILED node to be set to alive again if packets are received
    from it.
    Heal process of failed nodes handled correctly.