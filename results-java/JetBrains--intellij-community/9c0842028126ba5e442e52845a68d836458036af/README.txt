commit 9c0842028126ba5e442e52845a68d836458036af
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Wed Sep 28 18:12:02 2011 +0400

    GitLogParser refactoring & optimization

    1. GitLogParser.parseOneRecord: reimplemented via regexp; throw a RuntimeException if format doesn't match.
    2. Use GitLogStatusInfo instead of List<List<String>> to save information about a single status line for the further parsing in GitLogRecord.parseChanges().