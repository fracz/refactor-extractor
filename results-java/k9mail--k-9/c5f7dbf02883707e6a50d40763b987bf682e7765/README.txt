commit c5f7dbf02883707e6a50d40763b987bf682e7765
Author: Fiouz <fiouzy@gmail.com>
Date:   Sat Dec 18 10:12:52 2010 +0000

    Execute LocalMessage.appendMessage() & LocalMessage.setFlag() in the same transaction for small message storing in order to speed up DB update.
    This is a per message basis optimization. More improved speed could be attained by batching several messages in the same transaction.