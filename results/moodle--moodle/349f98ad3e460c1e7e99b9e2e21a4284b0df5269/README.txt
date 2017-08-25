commit 349f98ad3e460c1e7e99b9e2e21a4284b0df5269
Author: Petr Skoda <petr.skoda@totaralms.com>
Date:   Tue Jun 10 17:21:40 2014 +1200

    MDL-45941 implement support for message sending from DB transactions and fix other problems

    This patch includes following fixes:
     - messages may be now sent when database transactions active
     - consistent return status on failure from message_send(), false is
       returned only when message not created in message(_read)? table,
       processor failure results in debugging message only and messages
       are not marked as read
     - message_sent is triggered always with id from message table
     - logic for marking messages as viewed was standardised
     - message_viewed event is triggered consistently
     - improved performance when fetching user preferences
     - full unit tests coverage for send_message() function
     - fixed multiple other smaller issues discovered by unit tests