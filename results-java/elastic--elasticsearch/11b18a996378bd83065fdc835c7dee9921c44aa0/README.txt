commit 11b18a996378bd83065fdc835c7dee9921c44aa0
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Mar 8 15:06:10 2016 +0100

    Sort: Make ScoreSortBuilder implement NamedWriteable and add fromXContent parsing

    This change makes ScoreSortBuilder implement NamedWriteable, adds
    equals() and hashCode() and also implements parsing ScoreSortBuilder
    back from xContent. This is needed for the ongoing Search refactoring.