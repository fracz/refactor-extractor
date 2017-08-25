commit 007f4f69872f0e8f9d848229078e488f620eaca1
Author: Nils Adermann <naderman@naderman.de>
Date:   Thu Jul 27 19:02:47 2006 +0000

    - fixed the age calculation (note: turn on your brain before commiting something like this the next time) [Bug #3337]
    - removed the split_words array, introduced an enforced search_query
    - the forum used for global topics in the search is now a forum, and no longer a category [Bug #2561]
    - Bug #3404
    - allow accessing reports by report_id, in contrast to mcp_queue this cannot just use the post id, since there can be multiple closed reports per post, so closed reports have to be accessed by report id, open reports, can optionally be accessed by report id or post id [Bug #3149]
    - only attempt to unflag reported topics on closing a report when there are any without other reported posts [Bug #3057]
    - updated fulltext_mysql to use the the search_query string
    - overwrote the old fulltext_native with our improved version since it consumes too much time to maintain boths and we would switch to the improved version later anyway
    - always show a link to search a user's posts even if the postcount is zero since he  might only have posted in forums which do not count posts [Bug #3267]


    git-svn-id: file:///svn/phpbb/trunk@6211 89ea8834-ac86-4346-8a33-228a782c2dd0