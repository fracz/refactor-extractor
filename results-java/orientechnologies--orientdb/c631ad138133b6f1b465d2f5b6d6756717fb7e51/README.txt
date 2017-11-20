commit c631ad138133b6f1b465d2f5b6d6756717fb7e51
Author: lvca <l.garulli@gmail.com>
Date:   Mon Jan 18 06:46:37 2016 -0600

    Distributed: series of fix to improve HA and performance

    Better mgmt of quorum. fixed test cases, fixed the case clusters are
    not aligned, avoided ThreadInterrupt exception.