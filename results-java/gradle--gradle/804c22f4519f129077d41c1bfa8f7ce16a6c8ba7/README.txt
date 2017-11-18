commit 804c22f4519f129077d41c1bfa8f7ce16a6c8ba7
Author: Luke Daley <ld@ldaley.com>
Date:   Thu Jul 18 11:21:55 2013 -0600

    Change CopySpecContentVisitor to use functions instead of being a stateful visitor.

    This reduces the margin of error and reduces the temptation to hold on to state when it's no longer needed.

    A following change will improve the names to reflect the new approach.