commit 65338972c7f7e6fee0e86bb300cc2502f42edcc1
Author: Carsten Brandt <mail@cebe.cc>
Date:   Wed Nov 13 18:42:50 2013 +0100

    refactored ActiveRecord classes to use Interfaces and traits

    this allows us to implement other activerecord implementations based on
    NoSQL dbms