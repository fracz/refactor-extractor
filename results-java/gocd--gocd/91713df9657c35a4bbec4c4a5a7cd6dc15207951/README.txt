commit 91713df9657c35a4bbec4c4a5a7cd6dc15207951
Author: Tomasz Setkowski <tom@ai-traders.com>
Date:   Fri Jun 19 17:58:37 2015 +0200

    #1133 refactoring PipelineConfigs and MergePipelineConfigs

    removed list interface from PipelineConfigs
    removed some duplication in constructors of MergePipelineConfigs
    added shouldReturnIndexOfPipeline_When2Pipelines test to cover findBy
    better