commit 05d6d6fddcb782134f56eae081c9fb385cf868d0
Author: Pat Fives <pat@shots.com>
Date:   Tue Dec 8 17:08:43 2015 -0800

    add new emojis added in ios 9.1 including all skin tone emojis and flag emojis, add single unicode emojis to sEmojisMap for easy retrieval, refactor code to correctly handle keycap emojis when a non-spacing combining mark is used, handle modifier emojis (skin tones, flags, etc.) by getting drawable resourceId from unicode name rather than adding them all individually to another map