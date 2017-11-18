commit 53e07b5aab8071769a48d3c1acb9cb5b398935f6
Author: Dennis Oelkers <dennis@torch.sh>
Date:   Tue Jan 13 13:11:51 2015 +0100

    Extracting more DTOs, putting them into rest-models module.

    Slightly refactoring Ping class, no need to be static.
    Changing InputStateListeners, Radio became useless, sharing one now for
    status output.