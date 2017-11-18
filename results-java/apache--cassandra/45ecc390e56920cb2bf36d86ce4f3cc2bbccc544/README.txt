commit 45ecc390e56920cb2bf36d86ce4f3cc2bbccc544
Author: Eric Evans <eevans@apache.org>
Date:   Tue May 11 17:20:37 2010 +0000

    bring avro get() up to date w/ changes in trunk

     * refactoring of private methods in light of multiget() removal in trunk
     * update get() for keyspace argument remove
     * update get() for change to byte[] keys
     * added ttl attribute to column schema

    Patch by eevans

    git-svn-id: https://svn.apache.org/repos/asf/cassandra/trunk@943188 13f79535-47bb-0310-9956-ffa450edef68