<?php

/**
 * Contains the actual logic for post-type tests (posts tests, pages tests etc.) as a set of methods.
 * The actual test classes inherit from this and call the public methods of this class.
 *
 * Note that there are dependencies between tests - for example, the runUndoTrashTest() function expects
 * a state created by runTrashPostTest(). These dependencies are denoted in the actual actual test classes
 * using the @ depends annotation.
 *
 * Note2: helper test methods are called runXyzTest(), not testXyz() because otherwise PHPUnit would consider
 * them real tests.
 */
class PostTypeTestCase extends \SeleniumTestCase {

    /**
     * @return string
     */
    function getPostType() {
        return null; // Override in subclasses
    }


    public function runAddPostTest() {
        $commitAsserter = new CommitAsserter($this->gitRepository);

        $this->url($this->getPostTypeScreenUrl());
        $this->prepareTestPost();

        $this->byCssSelector('form#post #publish')->click();

        $this->waitForElement('#message.updated');

        $commitAsserter->assertNumCommits(1);
        $commitAsserter->assertCommitAction("post/create");
        $commitAsserter->assertCommitTag("VP-Post-Type",  $this->getPostType());
    }

    public function runUpdatePostTest() {
        $commitAsserter = new CommitAsserter($this->gitRepository);

        $this->setTinyMCEContent("Updated content");
        $this->byCssSelector('form#post #publish')->click();
        $this->waitForElement('#message.updated');

        $commitAsserter->assertNumCommits(1);
        $commitAsserter->assertCommitAction("post/edit");
        $commitAsserter->assertCommitTag("VP-Post-Type",  $this->getPostType());

    }

    public function runUpdatePostViaQuickEditTest() {
        $commitAsserter = new CommitAsserter($this->gitRepository);

        $this->url($this->getPostTypeScreenUrl());
        $this->executeScript("jQuery('#the-list tr:first-child .row-actions .editinline').click()");
        usleep(100*1000);
        $titleField = $this->byCssSelector('#the-list tr.inline-edit-row input.ptitle');
        $titleField->clear();
        $titleField->value("Quick-edited post title");
        $this->byCssSelector('#the-list tr.inline-edit-row a.save')->click();
        usleep(1000*1000);

        $commitAsserter->assertNumCommits(1);
        $commitAsserter->assertCommitsAreEquivalent();
        $commitAsserter->assertCommitTag("VP-Post-Type",  $this->getPostType());
    }

    public function runTrashPostTest() {
        $commitAsserter = new CommitAsserter($this->gitRepository);

        $this->trashPost();

        $commitAsserter->assertNumCommits(1);
        $commitAsserter->assertCommitAction("post/trash");
        $commitAsserter->assertCommitTag("VP-Post-Type",  $this->getPostType());
    }

    public function runUndoTrashTest() {
        $commitAsserter = new CommitAsserter($this->gitRepository);

        $undoLink = $this->byCssSelector('#message.updated a');
        $undoLink->click();

        $this->assertElementExists('#message.updated'); // "1 post restored from the Trash"

        $commitAsserter->assertNumCommits(1);
        $commitAsserter->assertCommitAction("post/untrash");
        $commitAsserter->assertCommitTag("VP-Post-Type",  $this->getPostType());

    }

    public function runDeletePostTest() {
        $this->trashPost();

        $commitAsserter = new CommitAsserter($this->gitRepository);

        $this->byCssSelector('.trash a')->click();
        $this->deletePostPermanently();

        $commitAsserter->assertNumCommits(1);
        $commitAsserter->assertCommitAction("post/delete");
        $commitAsserter->assertCommitTag("VP-Post-Type",  $this->getPostType());

    }

    public function runDraftTest() {
        $commitAsserter = new CommitAsserter($this->gitRepository);

        $this->prepareTestPost();
        $this->byCssSelector('form#post #save-post')->click();
        $this->waitForElement('#message.updated');

        $commitAsserter->assertNumCommits(1);
        $commitAsserter->assertCommitAction("post/draft");
        $commitAsserter->assertCommitTag("VP-Post-Type",  $this->getPostType());

    }

    public function runPreviewDraftTest() {
        $commitAsserter = new CommitAsserter($this->gitRepository);

        $this->setTinyMCEContent("Updated content");

        $previewLink = $this->byCssSelector('form#post #post-preview');
        $previewWindowId = $previewLink->attribute('target');
        $previewLink->click();
        $this->window($previewWindowId);
        $this->closeWindow();
        $this->window('');

        $commitAsserter->assertNumCommits(0);

    }

    public function runPublishDraftTest() {
        $commitAsserter = new CommitAsserter($this->gitRepository);

        $this->byCssSelector('form#post input#publish')->click();
        $this->waitForElement('#message.updated');

        $commitAsserter->assertNumCommits(1);
        $commitAsserter->assertCommitAction("post/publish");
        $commitAsserter->assertCommitTag("VP-Post-Type",  $this->getPostType());

    }



    //--------------------------
    // Helper methods
    //--------------------------

    /**
     * @return string
     */
    private function getPostTypeScreenUrl() {
        return 'wp-admin/edit.php?post_type=' . $this->getPostType();
    }

    /**
     * Trashes post. Waits for the operation to complete.
     */
    private function trashPost() {
        $this->executeScript("jQuery('#the-list tr:first-child .row-actions .submitdelete')[0].click()");
        $this->waitForElement('#message.updated');
    }

    /**
     * Deletes post permanently. Wait for the operation to complete.
     */
    private function deletePostPermanently() {
        // The CSS selector for 'Delete Permanently' is actually exactly the same as when trashing
        // the post, so is the update message, so we just use that method internally
        $this->trashPost();
    }

    /**
     * From the main page for given post type, clicks "Add new" and fills in the post title and content
     */
    private function prepareTestPost() {
        $this->byCssSelector('.edit-php #wpbody-content .wrap a.add-new-h2')->click();
        $this->byCssSelector('form#post input#title')->value("Test " . $this->getPostType());
        $this->setTinyMCEContent("Test content");
    }


}