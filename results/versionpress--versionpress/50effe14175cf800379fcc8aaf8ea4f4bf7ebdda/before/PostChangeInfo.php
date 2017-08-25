<?php

class PostChangeInfo extends EntityChangeInfo {

    const POST_TITLE_TAG = "VP-Post-Title";
    const POST_TYPE_TAG = "VP-Post-Type";

    /**
     * @var string
     */
    private $postType;
    /**
     * @var string
     */
    private $postTitle;

    public function __construct($action, $entityId, $postType, $postTitle) {
        parent::__construct("post", $action, $entityId);
        $this->postType = $postType;
        $this->postTitle = $postTitle;
    }

    /**
     * @param CommitMessage $commitMessage
     * @return bool
     */
    public static function matchesCommitMessage(CommitMessage $commitMessage) {
        return parent::matchesCommitMessage($commitMessage) && ChangeInfoHelpers::actionTagStartsWith($commitMessage, "post");
    }

    /**
     * @param CommitMessage $commitMessage
     * @return ChangeInfo
     */
    public static function buildFromCommitMessage(CommitMessage $commitMessage)  {
        $tags = $commitMessage->getVersionPressTags();
        $actionTag = $tags[ChangeInfo::ACTION_TAG];
        list($_, $action, $entityId) = explode("/", $actionTag, 3);
        $titleTag = isset($tags[self::POST_TITLE_TAG]) ? $tags[self::POST_TITLE_TAG] : $entityId;
        $type = isset($tags[self::POST_TYPE_TAG]) ? $tags[self::POST_TYPE_TAG] : "post";
        return new self($action, $entityId, $type, $titleTag);
    }

    /**
     * @return string
     */
    public function getChangeDescription() {
        switch($this->getAction()) {
            case "create":
                return "Created {$this->postType} '{$this->postTitle}'";
            case "trash":
                return NStrings::capitalize($this->postType) . " '{$this->postTitle}' moved to trash";
            case "untrash":
                return NStrings::capitalize($this->postType) . " '{$this->postTitle}' moved from trash";
            case "delete":
                return "Deleted {$this->postType} '{$this->postTitle}'";
        }
        return "Edited {$this->postType} '{$this->postTitle}'";
    }

    protected function getCustomTags() {
        return array(
            self::POST_TITLE_TAG => $this->postTitle,
            self::POST_TYPE_TAG => $this->postType
        );
    }

    protected function getCommitMessageHead() {
        if ($this->getAction() === 'trash' || $this->getAction() === 'untrash') {
            $preposition = $this->getAction() === 'trash' ? 'to' : 'from';
            $shortEntityId = substr($this->getEntityId(), 0, 4);
            return sprintf(NStrings::capitalize($this->postType) . " '%s' moved %s the trash", $shortEntityId, $preposition);
        }

        return parent::getCommitMessageHead();
    }


}