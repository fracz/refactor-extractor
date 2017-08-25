commit 829f5de0bf08b18f515cf655b0d9f391cc58159c
Author: ever.zet <ever.zet@gmail.com>
Date:   Mon Oct 4 02:43:03 2010 +0300

    refactored Gherkin I18n routine

     - Gherkin now uses Symfony Translation component
     - I18n files now in xliff 1.2 format
     - Nodes now holds only locale name, not translator service