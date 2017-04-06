commit c151f2137a23c36d59aed30153a6d989872a5262
Author: Ryan Ernst <ryan@iernst.net>
Date:   Tue Aug 18 21:32:51 2015 -0700

    Internal: Remove all uses of ImmutableList

    We are in the process of getting rid of guava, and this removes a major
    use. The replacement is mostly Collections.emptyList(), Arrays.asList
    and Collections.unmodifiableList. While it is questionable whether we
    need the last one (as these are usually placed in final members), we can
    continue to refactor later by removing unnecessary wrappings.