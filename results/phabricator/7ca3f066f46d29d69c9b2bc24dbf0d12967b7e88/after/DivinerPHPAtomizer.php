<?php

final class DivinerPHPAtomizer extends DivinerAtomizer {

  protected function executeAtomize($file_name, $file_data) {
    $future = xhpast_get_parser_future($file_data);
    $tree = XHPASTTree::newFromDataAndResolvedExecFuture(
      $file_data,
      $future->resolve());

    $atoms = array();

    $root = $tree->getRootNode();

    $func_decl = $root->selectDescendantsOfType('n_FUNCTION_DECLARATION');
    foreach ($func_decl as $func) {

      $name = $func->getChildByIndex(2);

      $atom = id(new DivinerAtom())
        ->setType('function')
        ->setName($name->getConcreteString())
        ->setLine($func->getLineNumber())
        ->setFile($file_name);

      $this->findAtomDocblock($atom, $func);

      $this->parseParams($atom, $func);
      $this->parseReturnType($atom, $func);

      $atoms[] = $atom;
    }

    return $atoms;
  }

  private function parseParams(DivinerAtom $atom, AASTNode $func) {
    $params = $func
      ->getChildByIndex(3, 'n_DECLARATAION_PARAMETER_LIST')
      ->selectDescendantsOfType('n_DECLARATION_PARAMETER');

    $param_spec = array();

    if ($atom->getDocblockRaw()) {
      $metadata = $atom->getDocblockMeta();
    } else {
      $metadata = array();
    }

    $docs = idx($metadata, 'param');
    if ($docs) {
      $docs = explode("\n", $docs);
      $docs = array_filter($docs);
    } else {
      $docs = array();
    }

    if (count($docs)) {
      if (count($docs) < count($params)) {
        $atom->addWarning(
          pht(
            'This call takes %d parameters, but only %d are documented.',
            count($params),
            count($docs)));
      }
    }

    foreach ($params as $param) {
      $name = $param->getChildByIndex(1)->getConcreteString();
      $dict = array(
        'type'    => $param->getChildByIndex(0)->getConcreteString(),
        'default' => $param->getChildByIndex(2)->getConcreteString(),
      );

      if ($docs) {
        $doc = array_shift($docs);
        if ($doc) {
          $dict += $this->parseParamDoc($atom, $doc, $name);
        }
      }

      $param_spec[] = array(
        'name' => $name,
      ) + $dict;
    }

    if ($docs) {
      foreach ($docs as $doc) {
        if ($doc) {
          $param_spec[] = $this->parseParamDoc($atom, $doc, null);
        }
      }
    }

    // TODO: Find `assert_instances_of()` calls in the function body and
    // add their type information here. See T1089.

    $atom->setProperty('parameters', $param_spec);
  }


  private function findAtomDocblock(DivinerAtom $atom, XHPASTNode $node) {
    $token = $node->getDocblockToken();
    if ($token) {
      $atom->setDocblockRaw($token->getValue());
      return true;
    } else {
      $tokens = $node->getTokens();
      if ($tokens) {
        $prev = head($tokens);
        while ($prev = $prev->getPrevToken()) {
          if ($prev->isAnyWhitespace()) {
            continue;
          }
          break;
        }

        if ($prev && $prev->isComment()) {
          $value = $prev->getValue();
          $matches = null;
          if (preg_match('/@(return|param|task|author)/', $value, $matches)) {
            $atom->addWarning(
              pht(
                'Atom "%s" is preceded by a comment containing "@%s", but the '.
                'comment is not a documentation comment. Documentation '.
                'comments must begin with "/**", followed by a newline. Did '.
                'you mean to use a documentation comment? (As the comment is '.
                'not a documentation comment, it will be ignored.)',
                $atom->getName(),
                $matches[1]));
          }
        }
      }

      $atom->setDocblockRaw('');
      return false;
    }
  }

  protected function parseParamDoc(DivinerAtom $atom, $doc, $name) {
    $dict = array();
    $split = preg_split('/\s+/', trim($doc), $limit = 2);
    if (!empty($split[0])) {
      $dict['doctype'] = $split[0];
    }

    if (!empty($split[1])) {
      $docs = $split[1];

      // If the parameter is documented like "@param int $num Blah blah ..",
      // get rid of the `$num` part (which Diviner considers optional). If it
      // is present and different from the declared name, raise a warning.
      $matches = null;
      if (preg_match('/^(\\$\S+)\s+/', $docs, $matches)) {
        if ($name !== null) {
          if ($matches[1] !== $name) {
            $atom->addWarning(
              pht(
                'Parameter "%s" is named "%s" in the documentation. The '.
                'documentation may be out of date.',
                $name,
                $matches[1]));
          }
        }
        $docs = substr($docs, strlen($matches[0]));
      }

      $dict['docs'] = $docs;
    }

    return $dict;
  }

  private function parseReturnType(DivinerAtom $atom, XHPASTNode $decl) {
    $return_spec = array();

    $metadata = $atom->getDocblockMeta();
    $return = idx($metadata, 'return');

    if (!$return) {
      $return = idx($metadata, 'returns');
      if ($return) {
        $atom->addWarning(
          pht('Documentation uses `@returns`, but should use `@return`.'));
      }
    }

    if ($atom->getName() == '__construct' && $atom->getType() == 'method') {
      $return_spec = array(
        'doctype' => 'this',
        'docs' => '//Implicit.//',
      );

      if ($return) {
        $atom->addWarning(
          'Method __construct() has explicitly documented @return. The '.
          '__construct() method always returns $this. Diviner documents '.
          'this implicitly.');
      }
    } else if ($return) {
      $split = preg_split('/\s+/', trim($return), $limit = 2);
      if (!empty($split[0])) {
        $type = $split[0];
      }

      if ($decl->getChildByIndex(1)->getTypeName() == 'n_REFERENCE') {
        $type = $type.' &';
      }

      $docs = null;
      if (!empty($split[1])) {
        $docs = $split[1];
      }

      $return_spec = array(
        'doctype' => $type,
        'docs'    => $docs,
      );
    } else {
      $return_spec = array(
        'type' => 'wild',
      );
    }

    $atom->setProperty('return', $return_spec);
  }

}
