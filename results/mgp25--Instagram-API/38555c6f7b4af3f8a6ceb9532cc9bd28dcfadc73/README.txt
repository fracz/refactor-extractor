commit 38555c6f7b4af3f8a6ceb9532cc9bd28dcfadc73
Author: Abyr Valg <valga.github@abyrga.ru>
Date:   Tue Jun 6 02:17:01 2017 +0300

    Direct/Realtime update (#1300)

    This is a big update to the Direct chat system, for both HTTP and Realtime methods:

    - Realtime system now re-uses some Model payloads from the core library instead of its own duplicates.

    - Direct chat sending methods now validate all user input.

    - The Realtime client Direct messaging system now falls back to using HTTP Direct sending if the realtime system fails.

    - You can now send Direct item Likes via Realtime.

    - You can now send and receive Links via Direct messaging.

    - You can send and remove reactions to Direct media items.

    - Added support for sending disappearing photos/videos.

    - And some other minor tweaks...

    * use DirectSeenItemPayload instead of Payload/Unseen

    * use DirectSendItemPayload instead of Payload/Ack

    * use ActionBadge instead of Payload/StoryAction

    * add ability to provide client_context to send into direct

    * add validation for direct sending methods

    * improve GUID validator

    * refactor _sendDirectItem()

    * rewrite Realtime sending to support custom client_context

    * rename methods in Realtime

    * send likes via Realtime

    * Add ability to send and receive links in Direct

    * Add ability to send and remove reactions to direct items

    * Replace 'GUID' with 'UUID'

    * Make media_type option required for sendShare()

    * Grammar

    * Move _handleReaction() to the end of file

    * Code style

    * Add support for disappearing media

    * Fix Realtime example after mass-rename

    * StyleCI

    * Use new response/model system

    * Generate auto docs

    * Use internalMetadata for direct story recipients

    * Add some constraints to direct videos

    * Fix constraints for direct videos

    * Add query param to getRankedRecipients()

    * Rename sendShare() to sendPost()

    See https://help.instagram.com/1209246439090858 for naming