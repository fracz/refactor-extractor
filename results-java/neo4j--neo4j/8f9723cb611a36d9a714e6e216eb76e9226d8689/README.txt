commit 8f9723cb611a36d9a714e6e216eb76e9226d8689
Author: Michael Hunger <github@jexp.de>
Date:   Thu Feb 14 18:07:31 2013 +0100

    Changes around shell variable handling, exporting databases or cypher results
    and general shell improvements

    o no welcome message with command mode (-c), also configurable with -Dquiet=true
    o shell variables passed to cypher queries as parameters
    o allow json for shell variable values, maps, arrays, etc
    o dump command for exporting a db or the subgraph of a cypher statement
    o can reassign shell variables, export a=10 && export b=a -> b=10
    o shell can execute a contents file directly with -f file which is faster
      than piping via stdout
    o Enforced that shell variables names are valid identifiers.