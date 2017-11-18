commit 92b237a0bc05838c84b27c1ea6253effddd399b8
Author: Dennis Oelkers <dennis@torch.sh>
Date:   Sat Jan 18 15:40:02 2014 +0100

    Reducing code duplication due to server vs. radio

    Created submodule graylog2-shared which is supposed to hold code which
    is not part of the plugin api but still used by server and radio modules
    Introducing MetricsHost and ProcessingHost interfaces
    Moving several classes to shared module and refactor references,
           partially generifying those classes to use interfaces instead of
           Core/Radio objects