commit 34ea698a1755e2e09d70936c26027ce9e7950fd3
Author: thc202 <thc202@gmail.com>
Date:   Wed Jan 27 23:58:09 2016 +0000

    Prevent the active scanner from reporting progress higher than 100%

    Change class HostProcess to:
     - Use the same function for both the actual scan of the nodes as
     well the calculation of the number of nodes that will be scanned, to
     ensure that the same logic is applied in both cases (currently it
     wasn't);
     - Log, with INFO level, the number of nodes that will be scanned;
     - In the event that more nodes are found while the active scan is in
     progress prevent the status of a given scanner to be higher than 99%
     when reached the known number of nodes and report 100% once completed.
     Also, to improve the reported progress of following scanners update
     the total number of nodes being scanned.

    Change method ScanProgressItem.getProgressPercentage() and class
    ActiveScanAPI to not return 100% (or more) when the scanner is still
    running (for the former also remove outdated JavaDoc comment).