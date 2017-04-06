commit f9530dfe8f360183b42c32828c62743017fc0e47
Author: javanna <cavannaluca@gmail.com>
Date:   Thu Sep 8 00:01:43 2016 +0200

    remove FetchSubPhaseContext in favour of generic fetch sub phase builder of type object

    The context was an object where the parsed info are stored. That is more of what we call the builder since after the search refactoring. No need for generics in FetchSubPhaseParser then. Also the previous setHitsExecutionNeeded wasn't useful, it can be removed as well, given that once there is a parsed ext section, it will become a builder that can be retrieved by the sub fetch phase. The sub fetch phase is responsible for doing nothing in case the builder is not set, meaning that the fetch sub phase is plugged in but the request didn't have the corresponding section.