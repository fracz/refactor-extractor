commit 3b2abf0751909127e162bfb645ae6211d0f49e58
Author: Alex Denvir <coldfff@gmail.com>
Date:   Sat Apr 5 14:23:48 2014 +0100

    Additional MongoDB adapter improvements
     * suggest mongofill/mongofill
     * Instantiate MongoCollection in MongoDBResourceManager
     * Allow for all Mongo/MongoClient options to be passed to constructor
     * Implement flushable interface
     * Index collection on 'key' when created