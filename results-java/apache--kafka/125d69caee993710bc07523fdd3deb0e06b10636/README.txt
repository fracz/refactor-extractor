commit 125d69caee993710bc07523fdd3deb0e06b10636
Author: Guozhang Wang <wangguoz@gmail.com>
Date:   Wed Aug 2 15:13:02 2017 -0700

    KAFKA-5671 Followup: Remove reflections in unit test classes

    1. Remove rest deprecation warnings in streams:jar.

    2. Consolidate all unit test classes' reflections to access internal topology builder from packages other than `o.a.k.streams`. We need to refactor the hierarchies of StreamTask, StreamThread and KafkaStreams to remove these hacky reflections.

    3. Minor fixes such as reference path, etc.

    4. Minor edits on web docs for the describe function under developer-guide.

    Author: Guozhang Wang <wangguoz@gmail.com>

    Reviewers: Bill Bejeck <bill@confluent.io>, Ismael Juma <ismael@juma.me.uk>, Damian Guy <damian.guy@gmail.com>

    Closes #3603 from guozhangwang/K5671-followup-comments