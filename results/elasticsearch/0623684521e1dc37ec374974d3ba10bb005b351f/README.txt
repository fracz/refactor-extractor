commit 0623684521e1dc37ec374974d3ba10bb005b351f
Author: Christoph Büscher <christoph@elastic.co>
Date:   Wed Nov 4 12:25:57 2015 +0100

    Geo: Moving static factory methods to ShapeBuilders

    Currently the abstract ShapeBuilder class serves too many different
    purposes, making it hard to refactor and maintain the code. In order
    to reduce the size and responsibilities, this PR moved all the
    static factory methods used as a shortcut to create new shape builders
    out to a new ShapeBuilders class, similar to how QueryBuilders is
    used already.