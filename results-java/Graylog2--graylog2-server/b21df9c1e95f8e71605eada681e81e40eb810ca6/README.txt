commit b21df9c1e95f8e71605eada681e81e40eb810ca6
Author: Jochen Schalanda <jochen@torch.sh>
Date:   Fri Oct 17 11:34:09 2014 +0200

    Improve readability and completeness of PrintModelProcessor output

    The previous implementation of PrintModelProcessor didn't output all registered
    resources from the Jersey ResourceModel. The output is now complete, sorted and
    printed through the logging subsystem instead of being dumped to stdout.