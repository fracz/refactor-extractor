commit 2655072bfdf5bef44c34495a01ec2470a82bcacb
Merge: 1fd7089 342c4b5
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Wed Jul 29 09:12:56 2015 +0200

    Merge branch '2.7' into 2.8

    * 2.7:
      [php7] Fix for substr() always returning a string
      [Security] Do not save the target path in the session for a stateless firewall
      Fix calls to HttpCache#getSurrogate triggering E_USER_DEPRECATED errors.
      [DependencyInjection] fixed FrozenParameterBag and improved Parameter…