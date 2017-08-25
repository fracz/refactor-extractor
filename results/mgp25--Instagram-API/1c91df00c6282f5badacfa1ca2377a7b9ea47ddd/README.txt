commit 1c91df00c6282f5badacfa1ca2377a7b9ea47ddd
Author: SteveJobzniak <SteveJobzniak@users.noreply.github.com>
Date:   Wed May 17 19:53:36 2017 +0200

    Instagram: Big improvements to backup() feature

    Closes #1256.

    - The backup() function now supports downloading album media (it used to
      crash when it found an album on your timeline).

    - The filenames are now much, much better. Previously, they were just an
      item ID such as "12309213949923293_21312312.jpg". Now you get more
      information, which helps you understand the item and also helps you
      with sorting it chronologically. The items are now named in this
      format: "2017-05-17 at 17.06.41 +0000 [12309213949923293_21312312].jpg".
      The "+0000" is the timezone offset your PHP installation was using
      when converting the media's upload-timestamp to a readable date. So it
      may say something like "-0730" or "+0300" for you, depending on what
      you've set your date_default_timezone_set() to in PHP. By keeping the
      timezone in the filename, you can always be sure when a media was
      really uploaded.

    - Album items are nicely named with their album's media ID and then a
      dash and then the subitem's offset within the album, such as:
      "2017-05-17 at 17.06.41 +0000 [12309213949923293_21312312-01].jpg" and
      "2017-05-17 at 17.06.41 +0000 [12309213949923293_21312312-02].mp4" and
      "2017-05-17 at 17.06.41 +0000 [12309213949923293_21312312-03].jpg"...

    - The "file modification time" of all downloaded files is now set to the
      timestamp of when the file was uploaded to Instagram. This is just an
      extra bonus to help you get a clean backup collection. You can now
      easily look at the modification time to know when it was uploaded. :)