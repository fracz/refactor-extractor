commit 1d586cb50a94540f6b931a8d525ba75273f314f0
Author: Matthias J. Sax <matthias@confluent.io>
Date:   Sat Dec 10 21:48:44 2016 -0800

    KAFKA-4476: Kafka Streams gets stuck if metadata is missing

     - break loop in StreamPartitionAssigner.assign() in case partition metadata is missing
     - fit state transition issue (follow up to KAFKA-3637: Add method that checks if streams are initialised)
     - some test improvements

    Author: Matthias J. Sax <matthias@confluent.io>

    Reviewers: Eno Thereska, Ismael Juma, Guozhang Wang

    Closes #2209 from mjsax/kafka-4476-stuck-on-missing-metadata