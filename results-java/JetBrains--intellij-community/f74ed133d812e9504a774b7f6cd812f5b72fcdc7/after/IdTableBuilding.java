package com.intellij.psi.impl.cache.impl.idCache;

import com.intellij.codeHighlighting.CopyCreatorLexer;
import com.intellij.ide.highlighter.custom.impl.CustomFileType;
import com.intellij.ide.startup.FileContent;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.StdLanguages;
import com.intellij.lang.cacheBuilder.CacheBuilderRegistry;
import com.intellij.lang.cacheBuilder.WordOccurrence;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.properties.parsing.PropertiesLexer;
import com.intellij.lexer.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.CustomHighlighterTokenType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLock;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.cache.impl.CacheManagerImpl;
import com.intellij.psi.impl.source.tree.ElementType;
import com.intellij.psi.search.IndexPattern;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.tree.java.IJavaElementType;
import com.intellij.util.Processor;
import com.intellij.util.text.CharArrayCharSequence;
import gnu.trove.TIntIntHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdTableBuilding {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.cache.impl.idCache.IdTableBuilding");

  private static final int FILE_SIZE_LIMIT = 1000000; // ignore files of size > 1Mb

  private IdTableBuilding() {
  }

  public static void processPossibleComplexFileName(char[] chars, int startOffset, int endOffset, TIntIntHashMap table) {
    int offset = findCharsWithinRange(chars, startOffset, endOffset, "/\\");
    offset = Math.min(offset, endOffset);
    int start = startOffset;
    while(start < endOffset) {
      IdCacheUtil.addOccurrence(table, chars, start, offset,UsageSearchContext.IN_FOREIGN_LANGUAGES);
      start = offset + 1;
      offset = Math.min(endOffset, findCharsWithinRange(chars, start, endOffset, "/\\"));
    }
  }

  private static int findCharsWithinRange(char[] chars, int startOffset, int endOffset, String charsToFind) {
    while(startOffset < endOffset) {
      if (charsToFind.indexOf(chars[startOffset]) != -1) return startOffset;
      ++startOffset;
    }

    return startOffset;
  }

  public static class Result {
    final Runnable runnable;
    final TIntIntHashMap wordsMap;
    final int[] todoCounts;

    private Result(Runnable runnable, TIntIntHashMap wordsMap, int[] todoCounts) {
      this.runnable = runnable;
      this.wordsMap = wordsMap;
      this.todoCounts = todoCounts;
    }
  }

  public interface IdCacheBuilder {
    void build(char[] chars, int length, TIntIntHashMap wordsTable, IndexPattern[] todoPatterns, int[] todoCounts, final PsiManager manager
    );
  }

  public interface ScanWordProcessor {
    void run (char[] chars, int start, int end);
  }

  static class TextIdCacheBuilder implements IdCacheBuilder {
    public void build(char[] chars,
                      int length,
                      TIntIntHashMap wordsTable,
                      IndexPattern[] todoPatterns,
                      int[] todoCounts,
                      final PsiManager manager) {
      scanWords(wordsTable, chars, 0, length, UsageSearchContext.IN_PLAIN_TEXT);

      if (todoCounts != null) {
        for (int index = 0; index < todoPatterns.length; index++) {
          Pattern pattern = todoPatterns[index].getPattern();
          if (pattern != null) {
            CharSequence input = new CharArrayCharSequence(chars, 0, length);
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
              if (matcher.start() != matcher.end()) {
                todoCounts[index]++;
              }
            }
          }
        }
      }
    }
  }

  private static class PropertiesIdCacheBuilder implements IdCacheBuilder {
    public void build(char[] chars,
                      int length,
                      TIntIntHashMap wordsTable,
                      IndexPattern[] todoPatterns,
                      int[] todoCounts,
                      final PsiManager manager) {
      Lexer lexer = new PropertiesFilterLexer(new PropertiesLexer(), wordsTable, todoCounts);
      lexer = new FilterLexer(lexer, new FilterLexer.SetFilter(ElementType.WHITE_SPACE_OR_COMMENT_BIT_SET));
      lexer.start(chars, 0, length);
      while (lexer.getTokenType() != null) lexer.advance();
    }
  }

  static class JavaIdCacheBuilder implements IdCacheBuilder {
    protected Lexer createLexer() {
      return new JavaLexer(LanguageLevel.JDK_1_3);
    }

    public void build(char[] chars,
                      int length,
                      TIntIntHashMap wordsTable,
                      IndexPattern[] todoPatterns,
                      int[] todoCounts,
                      final PsiManager manager) {
      Lexer lexer = createLexer();
      JavaFilterLexer filterLexer = new JavaFilterLexer(lexer, wordsTable, todoCounts);
      lexer = new FilterLexer(filterLexer, new FilterLexer.SetFilter(ElementType.WHITE_SPACE_OR_COMMENT_BIT_SET));
      lexer.start(chars, 0, length);
      while (lexer.getTokenType() != null) lexer.advance();
    }
  }


  static class XmlIdCacheBuilder implements IdCacheBuilder {
    public void build(char[] chars,
                      int length,
                      TIntIntHashMap wordsTable,
                      IndexPattern[] todoPatterns,
                      int[] todoCounts,
                      final PsiManager manager) {
      BaseFilterLexer filterLexer = createLexer(wordsTable, todoCounts);
      filterLexer.start(chars, 0, length);
      while (filterLexer.getTokenType() != null) {
        filterLexer.advance();
      }
    }

    protected BaseFilterLexer createLexer(TIntIntHashMap wordsTable, int[] todoCounts) {
      Lexer lexer = new XmlLexer();
      return new XmlFilterLexer(lexer, wordsTable, todoCounts);
    }
  }

  static class HtmlIdCacheBuilder extends XmlIdCacheBuilder {
    protected BaseFilterLexer createLexer(TIntIntHashMap wordsTable, int[] todoCounts) {
      return new XHtmlFilterLexer(new HtmlHighlightingLexer(), wordsTable, todoCounts);
    }
  }

  static class XHtmlIdCacheBuilder extends XmlIdCacheBuilder {
    protected BaseFilterLexer createLexer(TIntIntHashMap wordsTable, int[] todoCounts) {
      return new XHtmlFilterLexer(new XHtmlHighlightingLexer(), wordsTable, todoCounts);
    }
  }

  static class JspxIdCacheBuilder implements IdCacheBuilder {
    public void build(char[] chars,
                      int length,
                      TIntIntHashMap wordsTable,
                      IndexPattern[] todoPatterns,
                      int[] todoCounts,
                      final PsiManager manager) {
      Lexer lexer = new JspxHighlightingLexer();
      JspxFilterLexer filterLexer = new JspxFilterLexer(lexer, wordsTable, todoCounts);
      lexer = new FilterLexer(filterLexer, TOKEN_FILTER);
      lexer.start(chars, 0, length);
      while (lexer.getTokenType() != null) lexer.advance();
    }
  }

  static class EmptyBuilder implements IdCacheBuilder {
    public void build(char[] chars,
                      int length,
                      TIntIntHashMap wordsTable,
                      IndexPattern[] todoPatterns,
                      int[] todoCounts,
                      final PsiManager manager) {
      // Do nothing. This class is used to skip certain files from building caches for them.
    }
  }

  private static final HashMap<FileType,IdCacheBuilder> cacheBuilders = new HashMap<FileType, IdCacheBuilder>();

  public static void registerCacheBuilder(FileType fileType,IdCacheBuilder idCacheBuilder) {
    cacheBuilders.put(fileType, idCacheBuilder);
  }

  static {
    registerCacheBuilder(StdFileTypes.JAVA,new JavaIdCacheBuilder());
    registerCacheBuilder(StdFileTypes.XML,new XmlIdCacheBuilder());
    registerCacheBuilder(StdFileTypes.DTD,new XmlIdCacheBuilder());

    registerCacheBuilder(StdFileTypes.HTML,new HtmlIdCacheBuilder());
    registerCacheBuilder(StdFileTypes.XHTML,new XHtmlIdCacheBuilder());
    registerCacheBuilder(StdFileTypes.JSPX,new JspxIdCacheBuilder());
    registerCacheBuilder(StdFileTypes.PLAIN_TEXT,new TextIdCacheBuilder());
    registerCacheBuilder(StdFileTypes.PROPERTIES, new PropertiesIdCacheBuilder());

    registerCacheBuilder(StdFileTypes.IDEA_MODULE, new EmptyBuilder());
    registerCacheBuilder(StdFileTypes.IDEA_WORKSPACE, new EmptyBuilder());
    registerCacheBuilder(StdFileTypes.IDEA_PROJECT, new EmptyBuilder());
  }

  public static IdCacheBuilder getCacheBuilder(FileType fileType, final Project project, final VirtualFile virtualFile) {
    final IdCacheBuilder idCacheBuilder = cacheBuilders.get(fileType);

    if (idCacheBuilder != null) return idCacheBuilder;

    final WordsScanner customWordsScanner = CacheBuilderRegistry.getInstance().getCacheBuilder(fileType);
    if (customWordsScanner != null) {
      return new WordsScannerIdCacheBuilderAdapter(customWordsScanner, null, null);
    }

    final SyntaxHighlighter highlighter = fileType.getHighlighter(project, virtualFile);
    final Lexer highlightingLexer = highlighter != null ? highlighter.getHighlightingLexer() : null;
    if (fileType instanceof LanguageFileType) {
      final Language lang = ((LanguageFileType)fileType).getLanguage();
      final FindUsagesProvider findUsagesProvider = lang.getFindUsagesProvider();
      //noinspection ConstantConditions
      final WordsScanner scanner = findUsagesProvider == null ? null : findUsagesProvider.getWordsScanner();
      if (scanner != null) {
        final ParserDefinition parserDef = lang.getParserDefinition();
        final TokenSet commentTokens = parserDef != null ? parserDef.getCommentTokens() : null;
        return new WordsScannerIdCacheBuilderAdapter(scanner, highlightingLexer, commentTokens);
      }
    }

    if (fileType instanceof CustomFileType) {
      final TokenSet commentTokens = TokenSet.create(CustomHighlighterTokenType.LINE_COMMENT,
                                                     CustomHighlighterTokenType.MULTI_LINE_COMMENT);

      return new WordsScannerIdCacheBuilderAdapter(((CustomFileType)fileType).getWordsScanner(),
                                                   highlightingLexer, commentTokens);
    }

    return null;
  }

  private static class WordsScannerIdCacheBuilderAdapter implements IdCacheBuilder {
    private WordsScanner myScanner;
    @Nullable private final Lexer myHighlightingLexer;
    @Nullable private final TokenSet myCommentTokens;

    public WordsScannerIdCacheBuilderAdapter(@NotNull final WordsScanner scanner,
                                             @Nullable final Lexer highlightingLexer,
                                             @Nullable final TokenSet commentTokens) {
      myScanner = scanner;
      myHighlightingLexer = highlightingLexer;
      myCommentTokens = commentTokens;
    }

    public void build(char[] chars,
                      int length,
                      final TIntIntHashMap wordsTable,
                      final IndexPattern[] todoPatterns,
                      final int[] todoCounts,
                      final PsiManager manager) {
      myScanner.processWords(new CharArrayCharSequence(chars, 0, length), new Processor<WordOccurrence>() {
        public boolean process(final WordOccurrence t) {
          IdCacheUtil.addOccurrence(wordsTable, t.getText(), convertToMask(t.getKind()));
          return true;
        }

        private int convertToMask(final WordOccurrence.Kind kind) {
          if (kind == null) return UsageSearchContext.ANY;
          if (kind == WordOccurrence.Kind.CODE) return UsageSearchContext.IN_CODE;
          if (kind == WordOccurrence.Kind.COMMENTS) return UsageSearchContext.IN_COMMENTS;
          if (kind == WordOccurrence.Kind.LITERALS) return UsageSearchContext.IN_STRINGS;
          if (kind == WordOccurrence.Kind.FOREIGN_LANGUAGE) return UsageSearchContext.IN_FOREIGN_LANGUAGES;
          return 0;
        }
      });

      if (myHighlightingLexer != null && myCommentTokens != null && todoCounts != null) {
        BaseFilterLexer filterLexer = new BaseFilterLexer(myHighlightingLexer, wordsTable, todoCounts) {
          private Language myJavaLanguage = StdLanguages.JAVA;
          private Language myXmlLanguage = StdLanguages.XML;
          private Language myELLanguage = StdLanguages.EL;
          private Language myAnyLanguage = Language.ANY;

          public void advance() {
            IElementType tokenType = myOriginalLexer.getTokenType();
            if(tokenType instanceof CopyCreatorLexer.HighlightingCopyElementType){
              final CopyCreatorLexer.HighlightingCopyElementType scriptletJavaElementTypeToken = (CopyCreatorLexer.HighlightingCopyElementType)tokenType;
              tokenType = scriptletJavaElementTypeToken.getBase();
            }

            if (myCommentTokens.contains(tokenType)) {
              advanceTodoItemCounts(getBuffer(), getTokenStart(), getTokenEnd());
            } else if (tokenType.getLanguage() != myXmlLanguage &&
                       tokenType.getLanguage() != myJavaLanguage &&
                       tokenType.getLanguage() != myELLanguage &&
                       tokenType.getLanguage() != myAnyLanguage
                      ) {
              if (IdCacheUtil.isInComments(tokenType)) {
                advanceTodoItemCounts(getBuffer(), getTokenStart(), getTokenEnd());
              }
            }
            myOriginalLexer.advance();
          }
        };
        filterLexer.start(chars);
        while (filterLexer.getTokenType() != null) filterLexer.advance();
      }
    }
  }

  public static Result getBuildingRunnable(final PsiManagerImpl manager, FileContent fileContent, final boolean buildTodos) {
    if (LOG.isDebugEnabled()){
      LOG.debug(
        "enter: getBuildingRunnable(file='" + fileContent.getVirtualFile() + "' buildTodos='" + buildTodos + "' )"
      );
      //LOG.debug(new Throwable());
    }

    final VirtualFile virtualFile = fileContent.getVirtualFile();
    LOG.assertTrue(virtualFile.isValid());
    if (virtualFile.getLength() > FILE_SIZE_LIMIT) return null;
    final FileType fileType = FileTypeManager.getInstance().getFileTypeByFile(virtualFile);
    if (fileType.isBinary()) return null;
    if (StdFileTypes.CLASS.equals(fileType)) return null;

    final TIntIntHashMap wordsTable = new TIntIntHashMap();

    final int[] todoCounts;
    final IndexPattern[] todoPatterns = IdCacheUtil.getIndexPatterns();
    if (buildTodos && CacheManagerImpl.canContainTodoItems(fileContent.getVirtualFile())){
      int patternCount = todoPatterns.length;
      todoCounts = patternCount > 0 ? new int[patternCount] : null;
    }
    else{
      todoCounts = null;
    }

    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    final char[] chars;
    final int textLength;
    if (virtualFile.getModificationStamp() != document.getModificationStamp()) {
      final PsiFile psiFile = manager.getFile(fileContent);
      if (psiFile == null) return null;

      chars = psiFile.textToCharArray();
      textLength = psiFile.getTextLength();
    }
    else {
      chars = document.getChars();
      textLength = document.getTextLength();
    }

    final IdCacheBuilder cacheBuilder = getCacheBuilder(fileType, manager.getProject(), virtualFile);

    if (cacheBuilder==null) return null;

    Runnable runnable = new Runnable() {
      public void run() {
        synchronized (PsiLock.LOCK) {
          cacheBuilder.build(chars, textLength, wordsTable, todoPatterns, todoCounts, manager);
        }
      }
    };

    return new Result(runnable, wordsTable, todoCounts);
  }

  private static final FilterLexer.Filter TOKEN_FILTER = new FilterLexer.Filter() {
    public boolean reject(IElementType type) {
      return !(type instanceof IJavaElementType) || ElementType.WHITE_SPACE_OR_COMMENT_BIT_SET.contains(type);
    }
  };

  public static void scanWords(final ScanWordProcessor processor, final char[] chars, final int startOffset, final int endOffset) {
    int index = startOffset;
    ScanWordsLoop:
      while(true){
        while(true){
          if (index == endOffset) break ScanWordsLoop;
          char c = chars[index];
          if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (Character.isJavaIdentifierStart(c) && c != '$')) break;
          index++;
        }
        int index1 = index;
        while(true){
          index++;
          if (index == endOffset) break;
          char c = chars[index];
          if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) continue;
          if (!Character.isJavaIdentifierPart(c) || c == '$') break;
        }
        if (index - index1 > 100) continue; // Strange limit but we should have some!

        processor.run(chars, index1, index);
      }
  }

  public static void scanWords(final TIntIntHashMap table,
                               final char[] chars,
                               final int start,
                               final int end,
                               final int occurrenceMask) {
    scanWords(new ScanWordProcessor(){
      public void run(final char[] chars, final int start, final int end) {
        registerOccurence(chars, start, end, table, occurrenceMask);
      }
    }, chars, start, end);
  }

  private static void registerOccurence(final char[] chars,
                                        final int start,
                                        final int end,
                                        final TIntIntHashMap table,
                                        final int occurrenceMask) {
    IdCacheUtil.addOccurrence(table, chars, start, end, occurrenceMask);
  }
}