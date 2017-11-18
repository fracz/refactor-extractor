commit 3a35bb61da8556c67775d389c59294eaf62f27f4
Author: lutovich <konstantin.lutovich@neotechnology.com>
Date:   Tue Jul 7 17:25:18 2015 +0200

    Introduced notion of relationship constraints

    This commit is mostly about renaming and refactoring of class hierarchies
    that are used to represent indexes and constraints.

    Following changes were made:

     - MandatoryPropertyConstraint renamed to MandatoryNodePropertyConstraint
        together with all relevant methods

     - reworked hierarchy of PropertyConstraint class to reflect
        existence of node and relationship constraints

     - reworked hierarchy of SchemaRule and it's interface to
        provide information about both label and relationship type.
        This is expected to be a temporary state of the interface until
        it is further refactored and specialized

     - reworked hierarchy of ConstraintDefinition interface which
        now also has accessor methods for both label and relationship type.
        This is expected to be a temporary state, which can't be really fixed
        until next major release