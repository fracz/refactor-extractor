<?php

namespace InstagramAPI\Realtime\Event\Payload;

class StoryAction extends \InstagramAPI\AutoPropertyHandler
{
    const DELIVERED = 'raven_delivered';
    const SENT = 'raven_sent';
    const OPENED = 'raven_opened';
    const SCREENSHOT = 'raven_screenshot';
    const REPLAYED = 'raven_replayed';
    const CANNOT_DELIVER = 'raven_cannot_deliver';
    const SENDING = 'raven_sending';
    const BLOCKED = 'raven_blocked';
    const UNKNOWN = 'raven_unknown';
    const SUGGESTED = 'raven_suggested';

    public $action_type;
    public $action_timestamp;
    public $action_count;
}