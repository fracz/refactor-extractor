commit e0bde33e0d2b3ffb7918a5818a387773d4aa3e71
Author: Alon Albert <aalbert@google.com>
Date:   Thu Sep 22 14:26:16 2011 -0700

    Add Detailed Sync Stats Section
    Also improve history formatting
    Here's a smaple of what it looks like:

    Detailed Statistics (Recent history):  20  40s
    -------------------------------------------------------------------------------------
      com.android.contacts                                        : 9/45%      11/27%
        aagmtest1@gmail.com/com.google                            :   3/15%      4/11%
        aagmtest2@gmail.com/com.google                            :   3/15%      3/9%
        aalbert@google.com/com.google                             :   3/15%      2/6%
    -------------------------------------------------------------------------------------
      gmail-ls                                                    : 6/30%      22/56%
    -------------------------------------------------------------------------------------
      com.android.calendar                                        : 3/15%      5/14%
        aagmtest1@gmail.com/com.google                            :   1/5%       4/12%
        aagmtest2@gmail.com/com.google                            :   1/5%       0/0%
        aalbert@google.com/com.google                             :   1/5%       0/0%
    -------------------------------------------------------------------------------------
      com.google.android.apps.plus.content.EsGooglePhotoProvider  : 2/10%      0/1%
        aagmtest1@gmail.com/com.google                            :   1/5%       0/1%
        aagmtest2@gmail.com/com.google                            :   1/5%       0/0%
    -------------------------------------------------------------------------------------

    Recent Sync History
      #1  : 2011-09-23 15:18:35   SERVER    0.8s  aalbert@google.com/com.google   gmail-ls
      #2  : 2011-09-23 15:17:56   SERVER    1.3s  00:38
      #3  : 2011-09-23 15:17:52   SERVER    4.6s  00:04
      #4  : 2011-09-23 15:17:45   SERVER    1.7s  00:06
      #5  : 2011-09-23 15:16:51    LOCAL    0.8s  aagmtest2@gmail.com/com.google  com.android.contacts
      #6  : 2011-09-23 15:16:51    LOCAL    0.7s  aalbert@google.com/com.google   com.android.contacts
      #7  : 2011-09-23 15:16:50    LOCAL    0.7s  aagmtest1@gmail.com/com.google  com.android.contacts
      #8  : 2011-09-23 15:15:35    LOCAL    0.7s  aalbert@google.com/com.google   com.android.contacts
      #9  : 2011-09-23 15:15:34    LOCAL    0.8s  aagmtest2@gmail.com/com.google  com.android.contacts
      #10 : 2011-09-23 15:15:33    LOCAL    1.9s  aagmtest1@gmail.com/com.google  com.android.contacts
      #11 : 2011-09-23 15:14:37    LOCAL    1.3s  aalbert@google.com/com.google   com.android.contacts
      #12 : 2011-09-23 15:14:35    LOCAL    2.0s  aagmtest1@gmail.com/com.google  com.android.contacts
      #13 : 2011-09-23 15:14:32    LOCAL    2.2s  aagmtest2@gmail.com/com.google  com.android.contacts
      #14 : 2011-09-23 15:13:41   SERVER    1.3s  aalbert@google.com/com.google   gmail-ls
      #15 : 2011-09-23 15:13:34    LOCAL    0.3s  aalbert@google.com/com.google   com.android.calendar
      #16 : 2011-09-23 15:13:34    LOCAL    0.4s  aagmtest2@gmail.com/com.google  com.android.calendar
      #17 : 2011-09-23 15:13:33   SERVER    0.1s  aagmtest2@gmail.com/com.google  com.google.android.apps.plus.content.EsGooglePhotoProvider
      #18 : 2011-09-23 15:13:33   SERVER    0.5s  aagmtest1@gmail.com/com.google  com.google.android.apps.plus.content.EsGooglePhotoProvider
      #19 : 2011-09-23 15:13:29    LOCAL    4.9s  aagmtest1@gmail.com/com.google  com.android.calendar
      #20 : 2011-09-23 15:13:28   SERVER   13.1s  aalbert@google.com/com.google   gmail-ls

    Change-Id: Idc904e2e18a373b6d2d10af65b02683c11fd8d90