/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.groovy.lang.lexer;

import com.intellij.psi.tree.TokenSet;

/**
 * Utility classdef, tha contains various useful TokenSets
 *
 * @author Ilya Sergey
 */
public abstract class TokenSets implements GroovyTokenTypes {

  public static TokenSet COMMENTS_TOKEN_SET = TokenSet.create(
          mSL_COMMENT,
          mML_COMMENT
  );

  public static TokenSet SEPARATORS = TokenSet.create(
          mNLS,
          mSEMI
  );


  public static TokenSet WHITE_SPACE_TOKEN_SET = TokenSet.create(
          mWS
  );

  public static TokenSet SUSPICIOUS_EXPRESSION_STATEMENT_START_TOKEN_SET = TokenSet.create(
          mMINUS,
          mPLUS,
          mLBRACK,
          mLPAREN,
          mLCURLY
  );


  public static final TokenSet CONSTANTS = TokenSet.create(
          mNUM_INT,
          kTRUE,
          kFALSE,
          kNULL,
          mSTRING_LITERAL,
          mGSTRING_LITERAL,
          mREGEX_LITERAL
  );

    public static final TokenSet WRONG_CONSTANTS = TokenSet.create(
          mWRONG_GSTRING_LITERAL,
          mWRONG_STRING_LITERAL,
          mWRONG_REGEX_LITERAL
  );

  public static final TokenSet BUILT_IN_TYPE = TokenSet.create(
          kVOID,
          kBOOLEAN,
          kBYTE,
          kCHAR,
          kSHORT,
          kINT,
          kFLOAT,
          kLONG,
          kDOUBLE,
          kANY
  );

  public static TokenSet KEYWORD_PROPERTY_NAMES = TokenSet.orSet(TokenSet.create(
          kCLASS,
          kIN,
          kAS,
          kDEF,
          kIF,
          kELSE,
          kFOR,
          kWHILE,
          kSWITCH,
          kTRY,
          kCATCH,
          kFINALLY
  ), BUILT_IN_TYPE);

  public static TokenSet MODIFIERS = TokenSet.create(
      kPRIVATE,
      kPUBLIC,
      kPROTECTED,
      kSTATIC,
      kTRANSIENT,
      kFINAL,
      kABSTRACT,
      kNATIVE,
      kTHREADSAFE,
      kSYNCHRONIZED,
      kVOLATILE,
      kSTRICTFP
  );

  public static TokenSet STRING_LITERALS = TokenSet.create(
      mSTRING_LITERAL,
      mGSTRING_LITERAL
  );
}