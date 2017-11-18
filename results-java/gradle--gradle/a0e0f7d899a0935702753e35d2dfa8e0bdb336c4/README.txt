commit a0e0f7d899a0935702753e35d2dfa8e0bdb336c4
Author: Adam Murdoch <adam@gradle.com>
Date:   Fri Apr 29 14:00:33 2016 +1000

    Apply property and method lookup improvements to more parts of the configure dsl, in particular those that use `ConfigureUtil`, which for example includes the `dependencies { }` closure.