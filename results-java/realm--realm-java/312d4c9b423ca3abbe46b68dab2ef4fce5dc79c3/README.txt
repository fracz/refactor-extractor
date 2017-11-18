commit 312d4c9b423ca3abbe46b68dab2ef4fce5dc79c3
Author: Chen Mulong <chenmulong@gmail.com>
Date:   Thu Nov 5 22:12:30 2015 +0800

    Add counter for Realm instance for all threads.

    To close #1728
    The only usage of the Realm instance counter for now is to clean up
    validatedRealmFiles.
    We do need to refactor the whole counter/cache part.