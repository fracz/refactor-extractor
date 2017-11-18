commit a0a9a105dbde7d5b72b3f63341f1ccd5cb4c4883
Author: daz <darrell.deboer@gradleware.com>
Date:   Wed Mar 12 15:26:27 2014 -0600

    REVIEW-3670: Refactored GccCompatibleToolChain and ToolRegistry in preparation for improved DSL

    - Extracted Path searching out of ToolRegistry into ToolSearchPath
    - ToolRegistry is now a set of (configurable) tool instances
    - Targeting a platform involves first wrapping the ToolRegistry in a targeted ToolRegistry