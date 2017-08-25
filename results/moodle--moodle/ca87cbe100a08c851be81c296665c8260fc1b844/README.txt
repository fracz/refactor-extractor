commit ca87cbe100a08c851be81c296665c8260fc1b844
Author: stronk7 <stronk7>
Date:   Sun May 9 18:34:33 2004 +0000

    Some improvements in RSS:
    - Article's author is showed if present.
    - In forum posts feeds, show post->subject instead of discussion->name
    - Description contents in every article are passed to format_text() to
      show contents like the rest of Moodle.