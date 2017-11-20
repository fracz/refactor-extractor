commit 13057bdc581a23d4e86a2a6a02fb109b399b6884
Author: thc202 <thc202@gmail.com>
Date:   Mon Mar 20 22:06:18 2017 +0000

    Improve node enumeration in pre-scan phase

    Change HostProcess to improve the node enumeration done before the
    active scan is actually started, that's later used for calculation of
    progress and inform (through logging) how much nodes are going to be
    scanned. The HostProcess no longer counts the node if it cannot be
    scanned (per existing rules of the HostProcess). Also, the starting log
    message will include if the scan is "in scope", which affects the number
    of nodes that can be scanned.