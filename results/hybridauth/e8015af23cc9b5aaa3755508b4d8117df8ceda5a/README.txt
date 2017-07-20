commit e8015af23cc9b5aaa3755508b4d8117df8ceda5a
Author: miled <mohamed.b.miled@gmail.com>
Date:   Thu Jul 16 23:35:15 2015 +0200

    Fix an issue where Facebook returns an empty email due to recent API changes

    Ref:
    https://developers.facebook.com/blog/post/2015/07/08/graph-api-v2.4/

    "To help improve performance on mobile network connections, we've
    reduced the number of fields that the API returns by default. You should
    now use the ?fields=field1,field2 syntax to declare all the fields you
    want the API to return."