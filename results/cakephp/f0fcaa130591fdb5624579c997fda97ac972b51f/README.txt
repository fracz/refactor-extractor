commit f0fcaa130591fdb5624579c997fda97ac972b51f
Author: Ber Clausen <crashcookie@gmail.com>
Date:   Sat Nov 9 05:12:59 2013 -0300

    Improve generateAssociationQuery():

    * bail early when $linkModel is Null (BC for now).
    * move SQL fields warmup and SQL statement building to its own functions
     (it will payoff later improving self documentation and readability).
    * make assignments and function calls only when needed (depending on the
     association type).