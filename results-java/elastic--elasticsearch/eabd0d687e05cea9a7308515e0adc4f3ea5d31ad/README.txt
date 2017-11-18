commit eabd0d687e05cea9a7308515e0adc4f3ea5d31ad
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Fri Sep 29 11:06:58 2017 +0200

    MetaData Builder doesn't properly prevent an alias with the same name as an index (#26804)

    Elasticsearch doesn't allow having an index alias named with the same name as an existing index. We currently have logic that tries to prevents that in the `MetaData.Builder#build()` method. Sadly that logic is flawed. Depending on iteration order, we may allow the above to happen (if we encounter the alias before the index).

    This commit fixes the above and improves the error message while at it.

    Note that we have a lot of protections in place before we end up relying on the metadata builder (validating this when we process APIs). I takes quite an abuse of the cluster to get that far.