commit c37405263b0928dc52a4fc4857fb23fce3e0c77a
Author: Wouter Born <github@maindrain.net>
Date:   Sun Nov 20 09:57:44 2016 +0100

    [Plugwise] Reliability improvements (#4797)

    * [Plugwise] Reliability improvements

    Improves the binding reliability by:
    * making sure CR is followed by LF when reading incoming packets, which
    should result in less protocol message errors
    * polling max 1 second for AcknowledgeMessages so the SendThread does
    not wait indefinitely when ACKs get lost

    Signed-off-by: Wouter Born <github@maindrain.net>

    * Use JLS modifier order

    Signed-off-by: Wouter Born <github@maindrain.net>