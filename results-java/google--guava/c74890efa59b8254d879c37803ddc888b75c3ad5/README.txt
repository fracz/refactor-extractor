commit c74890efa59b8254d879c37803ddc888b75c3ad5
Author: kevinb@google.com <kevinb@google.com@8138a162-5c33-11de-8abc-d1c337b90d21>
Date:   Tue Aug 31 23:46:58 2010 +0000

    Next batch of pent-up fixes.

    Visible changes:
    - Add ImmutableSortedSet.copyOf(Collection) which is safe for concurrent
      modification.
    - Make ImmutableSet.copyOf(Collection) public.
    - Various Futures javadoc improvements (also use <V> and <X> over <T> and <E>).
    - Revamp javadoc for Function, Predicate and related classes. Highlight the
      issue about consistency with equals, as this problem has been noticed with
      utilities like Sets.filter() (issue 363).

    Less-visible changes:
    - Add a few missing @Nullable annoations.
    - In Iterables.concat impl, create an Iterator<Iterator> directly rather than via transform().

    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=


    git-svn-id: https://guava-libraries.googlecode.com/svn/trunk@97 8138a162-5c33-11de-8abc-d1c337b90d21