<?php

/**
 * Creates commits using the `GitStatic` class. By default, it detects the change from the `$mirror` object
 * but it can also be forced by calling the `forceChangeInfo()` method.
 */
class Committer
{

    /**
     * @var Mirror
     */
    private $mirror;

    /**
     * If this is set, takes precedence over changes detected in the `$mirror`.
     *
     * @var  ChangeInfo
     */
    private $forcedChangeInfo;
    /**
     * @var GitRepository
     */
    private $repository;
    /** @var  bool */
    private $commitDisabled;
    /**
     * @var StorageFactory
     */
    private $storageFactory;

    public function __construct(Mirror $mirror, GitRepository $repository, StorageFactory $storageFactory)
    {
        $this->mirror = $mirror;
        $this->repository = $repository;
        $this->storageFactory = $storageFactory;
    }

    /**
     * Checks if there is any change in the `$mirror` and commits it. If there was a forced
     * change set, it takes precedence.
     */
    public function commit()
    {
        if($this->commitDisabled) return;

        if ($this->forcedChangeInfo) {
            FileSystem::remove(get_home_path() . 'versionpress.maintenance'); // todo: this shouldn't be here...
            $changeInfo = $this->forcedChangeInfo;
            $this->forcedChangeInfo = null;
        } elseif ($this->mirror->wasAffected() && $this->shouldCommit()) {
            $changeList = $this->mirror->getChangeList();
            $changeInfo = count($changeList) > 1 ? new CompositeChangeInfo($changeList) : $changeList[0];
        } else {
            return;
        }

        if (is_user_logged_in() && is_admin()) {
            $currentUser = wp_get_current_user();
            $authorName = $currentUser->display_name;
            $authorEmail = $currentUser->user_email;
        } else {
            $authorName = "Non-admin action";
            $authorEmail = "nonadmin@example.com";
        }

        $this->addRelatedFiles($changeInfo);
        $this->repository->commit($changeInfo->getCommitMessage(), $authorName, $authorEmail);
    }

    /**
     * Forces change info to be committed in the next call to `commit()`
     *
     * @param ChangeInfo $changeInfo
     */
    public function forceChangeInfo(ChangeInfo $changeInfo)
    {
        $this->forcedChangeInfo = $changeInfo;
    }

    /**
     * All `commit()` calls are ignored after calling this method.
     */
    public function disableCommit()
    {
        $this->commitDisabled = true;
    }

    /**
     * Returns false in the mid-step of WP update.
     * The update runs an async HTTP request, so there is created a maintenance file that indicates
     * that the update is still running. Without this, there will be two commits for WP update.
     *
     * @return bool
     */
    private function shouldCommit()
    {
        if ($this->dbWasUpdated() && $this->existsMaintenanceFile())
            return false;
        return true;
    }

    private function dbWasUpdated()
    {
        $changes = $this->mirror->getChangeList();
        foreach ($changes as $change) {
            if ($change instanceof EntityChangeInfo &&
                $change->getObjectType() == 'option' &&
                $change->getEntityId() == 'db_version'
            )
                return true;
        }
        return false;
    }

    private function existsMaintenanceFile()
    {
        $maintenanceFilePattern = get_home_path() . '*.maintenance';
        return count(glob($maintenanceFilePattern)) > 0;
    }

    /**
     * @param TrackedChangeInfo $changeInfo
     */
    private function addRelatedFiles($changeInfo) {
        if ($changeInfo instanceof CompositeChangeInfo) {
            /** @var TrackedChangeInfo $subChangeInfo */
            foreach ($changeInfo->getChangeInfoList() as $subChangeInfo) {
                $this->addRelatedFiles($subChangeInfo);
            }
            return;
        }

        $changes = $changeInfo->getChangedFiles();

        foreach ($changes as $actionType => $changesForGivenAction) {
            foreach ($changesForGivenAction as $change) {
                if ($change["type"] === "storage-file") {
                    $entityType = $change["entity"];
                    $entityId = $change["id"];
                    $path = $this->storageFactory->getStorage($entityType)->getEntityFilename($entityId);
                } elseif ($change["type"] === "path") {
                    $path = $change["path"];
                } else {
                    continue;
                }

                if ($actionType === "add") {
                    $this->repository->add($path);
                } elseif ($actionType === "delete") {
                    $this->repository->rm($path);
                }
            }
        }
    }
}