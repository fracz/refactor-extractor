commit c7f9fd504e0f64be54c4c44b4f752b6c336f4150
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Sun Jan 18 15:02:19 2015 +0100

    clear out object references in input buffer implementations early

    this avoid object promotion to survivor or tenured generations, which leads to greatly improved gc performance when running parallel new gc and cms

    note that running those GC algorithms can sometimes erroneously trigger the "long gc" warning, because our check does not take concurrent phases into account.