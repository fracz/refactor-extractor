commit ae4a19eb5c77dabbaa1a26055d504936a6433b66
Author: Adrian Cole <adriancole@users.noreply.github.com>
Date:   Sat Sep 17 12:09:40 2016 +0800

    Unbreaks ElasticsearchStorage.Builder; reduces scope of internal client (#1304)

    This does a few things to make the recently refactored Elasticsearch
    component more maintainable:

    * Unbreaks api changes to ElasticsearchStorage.Builder
      * This now delegates to InternalElasticsearchClient.Builder
    * Reduces scope of InternalElasticsearchClient
      * Removes dependency writes as this is only used in tests
    * Various small cleanups.