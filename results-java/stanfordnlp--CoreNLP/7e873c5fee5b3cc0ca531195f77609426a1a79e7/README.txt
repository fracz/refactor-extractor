commit 7e873c5fee5b3cc0ca531195f77609426a1a79e7
Author: Christopher Manning <manning@cs.stanford.edu>
Date:   Sun Jul 5 11:15:05 2015 -0700

    Some small improvements to tokenization and sentence splitting. 1. Recognize common audio/video file extensions like .mp3, .mp4 2. Better handling of (well-edited) ellipsis dots: will now treat 4 followed by space and letter as end of sentence. Right for well-edited text. 3. Knows about f*ck and similar spellings. 4. Recognizes sentence start after acronym for a few more words like 'Why'