commit aae7faa88e55db81d3fa61a6fdb91fe14558efbd
Author: javanna <cavannaluca@gmail.com>
Date:   Thu Sep 24 17:21:34 2015 +0200

    Query refactoring: minor cleanups before we merge into master

    Remove ParseFieldMatcher duplication query in both contexts. QueryParseContext is still contained in QueryShardContext, as parsing still happens in the shards here and there. Most of the norelease comments have been removed simply because the scope of the refactoring has become smaller. Some could only be removed once everything, the whole search request, gets parsed on the coordinating node. We will get there eventually.