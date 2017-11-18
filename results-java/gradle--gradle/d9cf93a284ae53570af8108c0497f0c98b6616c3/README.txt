commit d9cf93a284ae53570af8108c0497f0c98b6616c3
Author: Adam Murdoch <adam@gradle.com>
Date:   Wed Oct 5 16:48:48 2016 +1100

    Moved `CacheLockingManager` back to build session scope for now. Some improvements to how test daemons are cleaned up required before this can be shared across builds.