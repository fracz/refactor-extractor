commit bebaf9799ce0d98230810e099e2b6df1508756c9
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Mon Jul 14 21:30:11 2014 +0200

    [Tests] stability improvements

    added explicit cleaning of temp unicast ping results
    reduce gateway local.list_timeout to 10s.
    testVerifyApiBlocksDuringPartition: verify master node has stepped down before restoring partition