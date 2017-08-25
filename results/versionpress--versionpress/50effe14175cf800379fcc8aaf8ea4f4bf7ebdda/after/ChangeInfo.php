<?php

interface ChangeInfo {
    /**
     * @return CommitMessage
     */
    function getCommitMessage();

    /**
     * @return string
     */
    function getChangeDescription();

    /**
     * @param CommitMessage $commitMessage
     * @return bool
     */
    static function matchesCommitMessage(CommitMessage $commitMessage);

    /**
     * @param CommitMessage $commitMessage
     * @return ChangeInfo
     */
    static function buildFromCommitMessage(CommitMessage $commitMessage);
}