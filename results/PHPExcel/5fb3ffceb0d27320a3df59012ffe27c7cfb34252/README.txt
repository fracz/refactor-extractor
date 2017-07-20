commit 5fb3ffceb0d27320a3df59012ffe27c7cfb34252
Author: Mark Baker <mark@lange.demon.co.uk>
Date:   Sun Dec 4 11:24:59 2011 +0000

    Performance improvement for readers that reduces overheads when setting titles in multi-worksheet workbooks, by avoiding re-iterating through all worksheet/cells whenever a sheet title is set

    git-svn-id: https://phpexcel.svn.codeplex.com/svn/trunk@83603 2327b42d-5241-43d6-9e2a-de5ac946f064