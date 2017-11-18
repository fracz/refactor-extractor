commit bac02b92c70e4f82f19596fd6261fd38dd003039
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Sun Jan 15 19:11:19 2017 +0300

    git tests: better adjust notification content for assertions

    Trim each line individually.
    Treat <hr/> as line break to avoid fighting with presense or absense of line breaks around <hr/>.

    Also use Streams to improve readability a bit.