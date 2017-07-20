<?php

final class DifferentialInlineCommentView extends AphrontView {

  private $inlineComment;
  private $onRight;
  private $buildScaffolding;
  private $handles;
  private $markupEngine;
  private $editable;
  private $preview;
  private $allowReply;

  public function setInlineComment(PhabricatorInlineCommentInterface $comment) {
    $this->inlineComment = $comment;
    return $this;
  }

  public function setOnRight($on_right) {
    $this->onRight = $on_right;
    return $this;
  }

  public function setBuildScaffolding($scaffold) {
    $this->buildScaffolding = $scaffold;
    return $this;
  }

  public function setHandles(array $handles) {
    assert_instances_of($handles, 'PhabricatorObjectHandle');
    $this->handles = $handles;
    return $this;
  }

  public function setMarkupEngine(PhabricatorMarkupEngine $engine) {
    $this->markupEngine = $engine;
    return $this;
  }

  public function setEditable($editable) {
    $this->editable = $editable;
    return $this;
  }

  public function setPreview($preview) {
    $this->preview = $preview;
    return $this;
  }

  public function setAllowReply($allow_reply) {
    $this->allowReply = $allow_reply;
    return $this;
  }

  public function render() {

    $inline = $this->inlineComment;

    $start = $inline->getLineNumber();
    $length = $inline->getLineLength();
    if ($length) {
      $end = $start + $length;
      $line = 'Lines '.number_format($start).'-'.number_format($end);
    } else {
      $line = 'Line '.number_format($start);
    }

    $metadata = array(
      'id' => $inline->getID(),
      'number' => $inline->getLineNumber(),
      'length' => $inline->getLineLength(),
      'on_right' => $this->onRight,
      'original' => $inline->getContent(),
    );

    $sigil = 'differential-inline-comment';
    if ($this->preview) {
      $sigil = $sigil . ' differential-inline-comment-preview';
    }

    $content = $inline->getContent();
    $handles = $this->handles;

    $links = array();

    $is_synthetic = false;
    if ($inline->getSyntheticAuthor()) {
      $is_synthetic = true;
    }

    $is_draft = false;
    if ($inline->isDraft() && !$is_synthetic) {
      $links[] = 'Not Submitted Yet';
      $is_draft = true;
    }

    if (!$this->preview) {
      $links[] = javelin_render_tag(
        'a',
        array(
          'href'  => '#',
          'mustcapture' => true,
          'sigil' => 'differential-inline-prev',
        ),
        'Previous');

      $links[] = javelin_render_tag(
        'a',
        array(
          'href'  => '#',
          'mustcapture' => true,
          'sigil' => 'differential-inline-next',
        ),
        'Next');

      if ($this->allowReply) {

        if (!$is_synthetic) {

          // NOTE: No product reason why you can't reply to these, but the reply
          // mechanism currently sends the inline comment ID to the server, not
          // file/line information, and synthetic comments don't have an inline
          // comment ID.

          $links[] = javelin_render_tag(
            'a',
            array(
              'href'        => '#',
              'mustcapture' => true,
              'sigil'       => 'differential-inline-reply',
            ),
            'Reply');
        }

      }
    }

    $anchor_name = 'inline-'.$inline->getID();

    if ($this->editable && !$this->preview) {
      $links[] = javelin_render_tag(
        'a',
        array(
          'href'        => '#',
          'mustcapture' => true,
          'sigil'       => 'differential-inline-edit',
        ),
        'Edit');
      $links[] = javelin_render_tag(
        'a',
        array(
          'href'        => '#',
          'mustcapture' => true,
          'sigil'       => 'differential-inline-delete',
        ),
        'Delete');
    } else if ($this->preview) {
      $links[] = javelin_render_tag(
        'a',
        array(
          'meta'        => array(
            'anchor' => $anchor_name,
          ),
          'sigil'       => 'differential-inline-preview-jump',
        ),
        'Not Visible');
      $links[] = javelin_render_tag(
        'a',
        array(
          'href'        => '#',
          'mustcapture' => true,
          'sigil'       => 'differential-inline-delete',
        ),
        'Delete');
    }

    if ($links) {
      $links =
        '<span class="differential-inline-comment-links">'.
          implode(' &middot; ', $links).
        '</span>';
    } else {
      $links = null;
    }

    $content = $this->markupEngine->getOutput(
      $inline,
      PhabricatorInlineCommentInterface::MARKUP_FIELD_BODY);

    if ($this->preview) {
      $anchor = null;
    } else {
      $anchor = phutil_render_tag(
        'a',
        array(
          'name'    => $anchor_name,
          'id'      => $anchor_name,
          'class'   => 'differential-inline-comment-anchor',
        ),
        '');
    }

    $classes = array(
      'differential-inline-comment',
    );
    if ($is_draft) {
      $classes[] = 'differential-inline-comment-unsaved-draft';
    }
    if ($is_synthetic) {
      $classes[] = 'differential-inline-comment-synthetic';
    }
    $classes = implode(' ', $classes);

    if ($is_synthetic) {
      $author = $inline->getSyntheticAuthor();
    } else {
      $author = $handles[$inline->getAuthorPHID()]->getName();
    }

    $markup = javelin_render_tag(
      'div',
      array(
        'class' => $classes,
        'sigil' => $sigil,
        'meta'  => $metadata,
      ),
      '<div class="differential-inline-comment-head">'.
        $anchor.
        $links.
        ' <span class="differential-inline-comment-line">'.$line.'</span> '.
        phutil_escape_html($author).
      '</div>'.
      '<div class="differential-inline-comment-content">'.
        '<div class="phabricator-remarkup">'.
          $content.
        '</div>'.
      '</div>');

    return $this->scaffoldMarkup($markup);
  }

  private function scaffoldMarkup($markup) {
    if (!$this->buildScaffolding) {
      return $markup;
    }

    $left_markup = !$this->onRight ? $markup : '';
    $right_markup = $this->onRight ? $markup : '';

    return
      '<table>'.
        '<tr class="inline">'.
          '<th></th>'.
          '<td class="left">'.$left_markup.'</td>'.
          '<th></th>'.
          '<td class="right3" colspan="3">'.$right_markup.'</td>'.
        '</tr>'.
      '</table>';
  }

}