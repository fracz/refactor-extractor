commit 14d7f57946d19736d552f263f4eed70ea18f0006
Author: Anna.Kozlova <anna.kozlova@jetbrains.com>
Date:   Wed Oct 18 10:45:08 2017 +0200

    junit 4 -> junit 5: replace with static imports after refactoring (IDEA-179927)

    if replace simultaneously, all old assertions have to be checked if they have no conflicts with current static import