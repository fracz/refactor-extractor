<?php

/*
 * Copyright 2011 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

abstract class DifferentialMail {

  const SUBJECT_PREFIX  = '[Differential]';

  protected $to = array();
  protected $cc = array();

  protected $actorHandle;

  protected $revision;
  protected $comment;
  protected $changesets;
  protected $inlineComments;
  protected $isFirstMailAboutRevision;
  protected $isFirstMailToRecipients;
  protected $heraldTranscriptURI;
  protected $heraldRulesHeader;

  abstract protected function renderSubject();
  abstract protected function renderBody();

  public function setActorHandle($actor_handle) {
    $this->actorHandle = $actor_handle;
    return $this;
  }

  public function getActorHandle() {
    return $this->actorHandle;
  }

  protected function getActorName() {
    $handle = $this->getActorHandle();
    if ($handle) {
      return $handle->getName();
    }
    return '???';
  }

  public function setXHeraldRulesHeader($header) {
    $this->heraldRulesHeader = $header;
    return $this;
  }

  public function send() {
    $to_phids = $this->getToPHIDs();
    if (!$to_phids) {
      throw new Exception('No "To:" users provided!');
    }

    $message_id = $this->getMessageID();

    $cc_phids = $this->getCCPHIDs();
    $subject  = $this->buildSubject();
    $body     = $this->buildBody();

    $mail = new PhabricatorMetaMTAMail();
    $handle = $this->getActorHandle();
    if ($handle) {
      $mail->setFrom($handle->getPHID());
      $mail->setReplyTo($this->getReplyHandlerEmailAddress());
    } else {
      $mail->setFrom($this->getReplyHandlerEmailAddress());
    }

    $mail
      ->addTos($to_phids)
      ->addCCs($cc_phids)
      ->setSubject($subject)
      ->setBody($body)
      ->setIsHTML($this->shouldMarkMailAsHTML())
      ->addHeader('Thread-Topic', $this->getRevision()->getTitle())
      ->addHeader('Thread-Index', $this->generateThreadIndex());

    if ($this->isFirstMailAboutRevision()) {
      $mail->addHeader('Message-ID',  $message_id);
    } else {
      $mail->addHeader('In-Reply-To', $message_id);
      $mail->addHeader('References',  $message_id);
    }

    if ($this->heraldRulesHeader) {
      $mail->addHeader('X-Herald-Rules', $this->heraldRulesHeader);
    }

    $mail->setRelatedPHID($this->getRevision()->getPHID());

    // Save this to the MetaMTA queue for later delivery to the MTA.
    $mail->save();
  }

  protected function buildSubject() {
    return self::SUBJECT_PREFIX.' '.$this->renderSubject();
  }

  protected function shouldMarkMailAsHTML() {
    return false;
  }

  protected function buildBody() {

    $actions = array();
    $body = $this->renderBody();
    $body .= <<<EOTEXT

ACTIONS
  Reply to comment, or !accept, !reject, !abandon, !resign, or !showdiff.

EOTEXT;

    if ($this->getHeraldTranscriptURI() && $this->isFirstMailToRecipients()) {
      $xscript_uri = $this->getHeraldTranscriptURI();
      $body .= <<<EOTEXT

MANAGE HERALD RULES
  http://todo.com/herald/

WHY DID I GET THIS EMAIL?
  {$xscript_uri}

Tip: use the X-Herald-Rules header to filter Herald messages in your client.

EOTEXT;
    }

    return $body;
  }

  protected function getReplyHandlerEmailAddress() {
    // TODO
    $phid = $this->getRevision()->getPHID();
    $server = 'todo.example.com';
    return "differential+{$phid}@{$server}";
  }

  protected function formatText($text) {
    $text = explode("\n", $text);
    foreach ($text as &$line) {
      $line = rtrim('  '.$line);
    }
    unset($line);
    return implode("\n", $text);
  }

  public function setToPHIDs(array $to) {
    $this->to = $this->filterContactPHIDs($to);
    return $this;
  }

  public function setCCPHIDs(array $cc) {
    $this->cc = $this->filterContactPHIDs($cc);
    return $this;
  }

  protected function filterContactPHIDs(array $phids) {
    return $phids;

    // TODO: actually do this?

    // Differential revisions use Subscriptions for CCs, so any arbitrary
    // PHID can end up CC'd to them. Only try to actually send email PHIDs
    // which have ToolsHandle types that are marked emailable. If we don't
    // filter here, sending the email will fail.
/*
    $handles = array();
    prep(new ToolsHandleData($phids, $handles));
    foreach ($handles as $phid => $handle) {
      if (!$handle->isEmailable()) {
        unset($handles[$phid]);
      }
    }
    return array_keys($handles);
*/
  }

  protected function getToPHIDs() {
    return $this->to;
  }

  protected function getCCPHIDs() {
    return $this->cc;
  }

  public function setRevision($revision) {
    $this->revision = $revision;
    return $this;
  }

  public function getRevision() {
    return $this->revision;
  }

  protected function getMessageID() {
    $phid = $this->getRevision()->getPHID();
    // TODO
    return "<differential-rev-{$phid}-req@TODO.com>";
  }

  public function setComment($comment) {
    $this->comment = $comment;
    return $this;
  }

  public function getComment() {
    return $this->comment;
  }

  public function setChangesets($changesets) {
    $this->changesets = $changesets;
    return $this;
  }

  public function getChangesets() {
    return $this->changesets;
  }

  public function setInlineComments(array $inline_comments) {
    $this->inlineComments = $inline_comments;
    return $this;
  }

  public function getInlineComments() {
    return $this->inlineComments;
  }

  public function renderRevisionDetailLink() {
    $uri = $this->getRevisionURI();
    return "REVISION DETAIL\n  {$uri}";
  }

  public function getRevisionURI() {
    return PhabricatorEnv::getURI('/D'.$this->getRevision()->getID());
  }

  public function setIsFirstMailToRecipients($first) {
    $this->isFirstMailToRecipients = $first;
    return $this;
  }

  public function isFirstMailToRecipients() {
    return $this->isFirstMailToRecipients;
  }

  public function setIsFirstMailAboutRevision($first) {
    $this->isFirstMailAboutRevision = $first;
    return $this;
  }

  public function isFirstMailAboutRevision() {
    return $this->isFirstMailAboutRevision;
  }

  protected function generateThreadIndex() {
    // When threading, Outlook ignores the 'References' and 'In-Reply-To'
    // headers that most clients use. Instead, it uses a custom 'Thread-Index'
    // header. The format of this header is something like this (from
    // camel-exchange-folder.c in Evolution Exchange):

    /* A new post to a folder gets a 27-byte-long thread index. (The value
     * is apparently unique but meaningless.) Each reply to a post gets a
     * 32-byte-long thread index whose first 27 bytes are the same as the
     * parent's thread index. Each reply to any of those gets a
     * 37-byte-long thread index, etc. The Thread-Index header contains a
     * base64 representation of this value.
     */

    // The specific implementation uses a 27-byte header for the first email
    // a recipient receives, and a random 5-byte suffix (32 bytes total)
    // thereafter. This means that all the replies are (incorrectly) siblings,
    // but it would be very difficult to keep track of the entire tree and this
    // gets us reasonable client behavior.

    $base = substr(md5($this->getRevision()->getPHID()), 0, 27);
    if (!$this->isFirstMailAboutRevision()) {
      // not totally sure, but it seems like outlook orders replies by
      // thread-index rather than timestamp, so to get these to show up in the
      // right order we use the time as the last 4 bytes.
      $base .= ' ' . pack("N", time());
    }
    return base64_encode($base);
  }

  public function setHeraldTranscriptURI($herald_transcript_uri) {
    $this->heraldTranscriptURI = $herald_transcript_uri;
    return $this;
  }

  public function getHeraldTranscriptURI() {
    return $this->heraldTranscriptURI;
  }

}