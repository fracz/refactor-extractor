commit 7ca523102fab51c9449ba7b71cd768bec411d50a
Author: Mike van Riel <me@mikevanriel.com>
Date:   Thu May 15 08:13:15 2014 +0200

    #1248: Rewired ServiceProviders and Config

    In order to improve segregation between components I have moved their
    configuration into their own namespaces and rewired the configuration
    to match that.

    In addition I have extracted parts of the Application class into their
    own ServiceProviders because these match a configuration heading