commit 855c199f608e5ad428b53a357101f1efd8f1f4eb
Author: Colin Goodheart-Smithe <colings86@users.noreply.github.com>
Date:   Tue Jul 14 12:39:56 2015 +0100

    Preparing ValuesSourceAggregatorFactory/Parser for refactoring

    This change adds AbstractValuesSourceParser which will be the new class used to create ValuesSourceAggregatorFactory objects. AbstractValuesSourceParser parses all the parameters required for ValuesSource and passes to the sub-class to parse any other (implementation specific) parameters. After parsing is complete it will call createFactory on the implementing class to create the AggregatorFactory object and then set the ValuesSource specific parameters before returning it.

    ValuesSourceAggregatorFactory also now has setter methods so that it can be used as the 'builder' object in the future.