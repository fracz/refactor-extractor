commit 6e5f40eadc02f204bbdb82b28913384f6a77b768
Author: dongsheng <dongsheng>
Date:   Thu Jul 24 03:15:03 2008 +0000

    MDL-14650, create a new table `chat_messages_current` to save latest 8 hours messages, could improve performance. The expired message will be deleted in chat_cron function