commit 094f7aa20e4fb5c752fbdddb47f8cb70bb721f53
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Mon Feb 22 14:28:34 2016 +0100

    Fix Hazelcast Cache auto-configuration ordering

    Spring Boot supports the automatic configuration of an additional
    HazelcastInstance if one already exists and an explicit property has been
    set to use a different configuration for caching. So three cases are
    supported really: no `HazelcastInstance` exists so we need to create one
    anyway or an `HazelcastInstance` already exists; in that latter case, we
    should either reuse it or create a new one.

    Unfortunately, the conditions that checked those three use cases were
    not ordered consistently and we could easily get in a situation where
    both conditions were evaluated.

    This commit makes sure that we  first check if an `HazelcastInstance`
    exists and then (and only then) we create the missing `HazelcastInstance`
    used for caching. The tests have also been improved to validate the
    proper `HazelcastInstance` is used for caching.

    Closes gh-5181