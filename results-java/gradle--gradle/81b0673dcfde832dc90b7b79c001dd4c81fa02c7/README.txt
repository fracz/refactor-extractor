commit 81b0673dcfde832dc90b7b79c001dd4c81fa02c7
Author: Rene Groeschke <rene@breskeby.com>
Date:   Thu May 2 11:16:59 2013 +0200

    Some refactorings for placeholder actions in TaskContainerInternal.

    - REVIEW-2177: - Use Runnable instead of Action<Project>
                   - Add more unit code coverage for placeholder actions in DefaultTaskContainer

    - REVIEW-2178: - don't call of DefaultTaskContainer#findByName twice for existing tasks.