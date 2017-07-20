<?php

/**
 * Resolves references (like short commit names, branch names, tag names, etc.)
 * into canonical, stable commit identifiers. This query works for all
 * repository types.
 *
 * This query will always resolve refs which can be resolved, but may need to
 * perform VCS operations. A faster (but less complete) counterpart query is
 * available in @{class:DiffusionCachedResolveRefsQuery}; that query can
 * resolve most refs without VCS operations.
 */
final class DiffusionLowLevelResolveRefsQuery
  extends DiffusionLowLevelQuery {

  private $refs;

  public function withRefs(array $refs) {
    $this->refs = $refs;
    return $this;
  }

  protected function executeQuery() {
    if (!$this->refs) {
      return array();
    }

    switch ($this->getRepository()->getVersionControlSystem()) {
      case PhabricatorRepositoryType::REPOSITORY_TYPE_GIT:
        $result = $this->resolveGitRefs();
        break;
      case PhabricatorRepositoryType::REPOSITORY_TYPE_MERCURIAL:
        $result = $this->resolveMercurialRefs();
        break;
      case PhabricatorRepositoryType::REPOSITORY_TYPE_SVN:
        $result = $this->resolveSubversionRefs();
        break;
      default:
        throw new Exception('Unsupported repository type!');
    }

    return $result;
  }

  private function resolveGitRefs() {
    $repository = $this->getRepository();

    // TODO: When refs are ambiguous (for example, tags and branches with
    // the same name) this will only resolve one of them.
    $future = $repository->getLocalCommandFuture('cat-file --batch-check');
    $future->write(implode("\n", $this->refs));
    list($stdout) = $future->resolvex();

    $lines = explode("\n", rtrim($stdout, "\n"));
    if (count($lines) !== count($this->refs)) {
      throw new Exception('Unexpected line count from `git cat-file`!');
    }

    $hits = array();
    $tags = array();

    $lines = array_combine($this->refs, $lines);
    foreach ($lines as $ref => $line) {
      $parts = explode(' ', $line);
      if (count($parts) < 2) {
        throw new Exception("Failed to parse `git cat-file` output: {$line}");
      }
      list($identifier, $type) = $parts;

      if ($type == 'missing') {
        // This is either an ambiguous reference which resolves to several
        // objects, or an invalid reference. For now, always treat it as
        // invalid. It would be nice to resolve all possibilities for
        // ambiguous references at some point, although the strategy for doing
        // so isn't clear to me.
        continue;
      }

      switch ($type) {
        case 'commit':
          break;
        case 'tag':
          $tags[] = $identifier;
          break;
        default:
          throw new Exception(
            "Unexpected object type from `git cat-file`: {$line}");
      }

      $hits[] = array(
        'ref' => $ref,
        'type' => $type,
        'identifier' => $identifier,
      );
    }

    $tag_map = array();
    if ($tags) {
      // If some of the refs were tags, just load every tag in order to figure
      // out which commits they map to. This might be somewhat inefficient in
      // repositories with a huge number of tags.
      $tag_refs = id(new DiffusionLowLevelGitRefQuery())
        ->setRepository($repository)
        ->withIsTag(true)
        ->executeQuery();
      foreach ($tag_refs as $tag_ref) {
        $tag_map[$tag_ref->getShortName()] = $tag_ref->getCommitIdentifier();
      }
    }

    $results = array();
    foreach ($hits as $hit) {
      $type = $hit['type'];
      $ref = $hit['ref'];

      $alternate = null;
      if ($type == 'tag') {
        $alternate = $identifier;
        $identifier = idx($tag_map, $ref);
        if (!$identifier) {
          throw new Exception("Failed to look up tag '{$ref}'!");
        }
      }

      $result = array(
        'type' => $type,
        'identifier' => $identifier,
      );

      if ($alternate !== null) {
        $result['alternate'] = $alternate;
      }

      $results[$ref][] = $result;
    }

    return $results;
  }

  private function resolveMercurialRefs() {
    $repository = $this->getRepository();

    // First, pull all of the branch heads in the repository. Doing this in
    // bulk is much faster than querying each individual head if we're
    // checking even a small number of refs.
    $futures = array();
    $futures['all'] = $repository->getLocalCommandFuture(
      'log --template=%s --rev %s',
      '{node} {branch}\\n',
      hgsprintf('head()'));
    $futures['open'] = $repository->getLocalCommandFuture(
      'log --template=%s --rev %s',
      '{node} {branch}\\n',
      hgsprintf('head() and not closed()'));


    $map = array();
    foreach (new FutureIterator($futures) as $key => $future) {
      list($stdout) = $future->resolvex();
      $lines = phutil_split_lines($stdout, $retain_endings = false);
      foreach ($lines as $idx => $line) {
        list($node, $branch) = explode(' ', $line, 2);
        $map[$branch]['nodes'][] = $node;
        if ($key == 'open') {
          $map[$branch]['open'] = true;
        }
      }
    }

    $results = array();
    $unresolved = $this->refs;
    foreach ($unresolved as $key => $ref) {
      if (!isset($map[$ref])) {
        continue;
      }

      $is_closed = !idx($map[$ref], 'open', false);
      foreach ($map[$ref]['nodes'] as $node) {
        $results[$ref][$node] = array(
          'type' => 'branch',
          'identifier' => $node,
          'closed' => $is_closed,
        );
      }

      unset($unresolved[$key]);
    }

    // Strip the node keys off the result list.
    foreach ($results as $ref => $result_list) {
      $results[$ref] = array_values($result_list);
    }

    if (!$unresolved) {
      return $results;
    }

    // If we still have unresolved refs (which might be things like "tip"),
    // try to resolve them individually.

    $futures = array();
    foreach ($unresolved as $ref) {
      $futures[$ref] = $repository->getLocalCommandFuture(
        'log --template=%s --rev %s',
        '{node}',
        hgsprintf('%s', $ref));
    }

    foreach (new FutureIterator($futures) as $ref => $future) {
      try {
        list($stdout) = $future->resolvex();
      } catch (CommandException $ex) {
        if (preg_match('/ambiguous identifier/', $ex->getStdErr())) {
          // This indicates that the ref ambiguously matched several things.
          // Eventually, it would be nice to return all of them, but it is
          // unclear how to best do that. For now, treat it as a miss instead.
          continue;
        }
        throw $ex;
      }

      // It doesn't look like we can figure out the type (commit/branch/rev)
      // from this output very easily. For now, just call everything a commit.
      $type = 'commit';

      $results[$ref][] = array(
        'type' => $type,
        'identifier' => trim($stdout),
      );
    }

    return $results;
  }

  private function resolveSubversionRefs() {
    // We don't have any VCS logic for Subversion, so just use the cached
    // query.
    return id(new DiffusionCachedResolveRefsQuery())
      ->setRepository($this->getRepository())
      ->withRefs($this->refs)
      ->execute();
  }

}