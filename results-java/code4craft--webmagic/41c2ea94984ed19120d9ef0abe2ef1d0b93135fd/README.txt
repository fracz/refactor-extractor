commit 41c2ea94984ed19120d9ef0abe2ef1d0b93135fd
Author: yihua.huang <code4crafter@gmail.com>
Date:   Tue May 27 17:34:19 2014 +0800

    refactor of selectable cont' #113
    1. remove lazy init of Html
    2. rename strings to sourceTexts for better meaning
    3. make getSourceTexts abstract and DO NOT always store strings
    4. instead store parsed elements of document in HtmlNode