commit ad4405f24478065f7936ab6a5e35a468bb51b7fd
Author: Ali Beyad <ali@elastic.co>
Date:   Tue Dec 20 12:25:52 2016 -0500

    Adds setting level to allocation decider explanations (#22268)

    The allocation decider explanation messages where improved in #21771 to
    include the specific Elasticsearch setting that contributed to the
    decision taken by the decider.  This commit improves upon the
    explanation message output by including whether the setting was an index
    level setting or a cluster level setting.  This will further help the
    user understand and locate the setting that is the cause of shards
    remaining unassigned or remaining on their current node.