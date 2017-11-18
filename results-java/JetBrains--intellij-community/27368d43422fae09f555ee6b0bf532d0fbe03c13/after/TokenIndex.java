package com.intellij.tokenindex;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.structuralsearch.StructuralSearchUtil;
import com.intellij.util.containers.HashMap;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * @author Eugene.Kudelevsky
 */
public class TokenIndex extends FileBasedIndexExtension<TokenIndexKey, List<Token>> {
  private static final int FILE_BLOCK_SIZE = 100;

  public static final ID<TokenIndexKey, List<Token>> ID = new ID<TokenIndexKey, List<Token>>("token.index") {
  };

  private static final int VERSION = 1;

  private final KeyDescriptor<TokenIndexKey> myKeyDescriptor = new TokenIndexKeyDescriptor();

  private static final int ANONYM_TOKEN_ID = 0;
  private static final int TEXT_TOKEN_ID = 1;
  private static final int MARKER_TOKEN_ID = 2;

  private final DataExternalizer<List<Token>> myDataExternalizer = new DataExternalizer<List<Token>>() {
    public void save(DataOutput out, List<Token> value) throws IOException {
      out.writeInt(value.size());
      for (Token token : value) {
        if (token instanceof AnonymToken) {
          out.writeByte(ANONYM_TOKEN_ID);
          out.writeInt(token.getOffset());
          out.writeByte(((AnonymToken)token).getType());
        }
        else if (token instanceof TextToken) {
          out.writeByte(TEXT_TOKEN_ID);
          out.writeInt(token.getOffset());
          out.writeInt(((TextToken)token).getHash());
        }
        else if (token instanceof PathMarkerToken) {
          out.writeByte(MARKER_TOKEN_ID);
          out.writeUTF(((PathMarkerToken)token).getPath());
        }
        else {
          assert false : "Unsupported token type " + token.getClass();
        }
      }
    }

    public List<Token> read(DataInput in) throws IOException {
      List<Token> result = new ArrayList<Token>();
      int n = in.readInt();
      for (int i = 0; i < n; i++) {
        byte tokenTypeId = in.readByte();
        switch (tokenTypeId) {
          case ANONYM_TOKEN_ID: {
            int offset = in.readInt();
            byte anonymTokenTypeValue = in.readByte();
            result.add(new AnonymToken(anonymTokenTypeValue, offset));
            break;
          }
          case TEXT_TOKEN_ID: {
            int offset = in.readInt();
            int hash = in.readInt();
            result.add(new TextToken(hash, offset));
            break;
          }
          case MARKER_TOKEN_ID: {
            String path = in.readUTF();
            result.add(new PathMarkerToken(path));
            break;
          }
        }
      }
      return result;
    }
  };

  @Override
  public ID<TokenIndexKey, List<Token>> getName() {
    return ID;
  }

  private static int getBlockId(String filePath) {
    int h = filePath.hashCode();
    if (h < 0) {
      h = -h;
    }
    return h % FILE_BLOCK_SIZE;
  }

  @Override
  public DataIndexer<TokenIndexKey, List<Token>, FileContent> getIndexer() {
    return new DataIndexer<TokenIndexKey, List<Token>, FileContent>() {
      @NotNull
      public Map<TokenIndexKey, List<Token>> map(FileContent inputData) {
        PsiFile psiFile = inputData.getPsiFile();
        FileViewProvider viewProvider = psiFile.getViewProvider();
        Map<TokenIndexKey, List<Token>> result = new HashMap<TokenIndexKey, List<Token>>(1);
        for (Language language : viewProvider.getLanguages()) {
          Tokenizer tokenizer = StructuralSearchUtil.getTokenizerForLanguage(language);
          if (tokenizer != null) {
            PsiFile f = viewProvider.getPsi(language);
            if (f != null) {
              List<Token> tokens = tokenizer.tokenize(Arrays.asList(f));
              if (tokens.size() > 0) {
                String path = inputData.getFile().getPath();
                tokens.add(new PathMarkerToken(path));
                TokenIndexKey key = new TokenIndexKey(language.getID(), getBlockId(path));
                result.put(key, tokens);
              }
            }
          }
        }
        return result;
      }
    };
  }

  @Override
  public KeyDescriptor<TokenIndexKey> getKeyDescriptor() {
    return myKeyDescriptor;
  }

  @Override
  public DataExternalizer<List<Token>> getValueExternalizer() {
    return myDataExternalizer;
  }

  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new FileBasedIndex.InputFilter() {
      public boolean acceptInput(VirtualFile file) {
        return file.getFileType() instanceof LanguageFileType;
      }
    };
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Override
  public int getCacheSize() {
    return 1;
  }

  public static boolean supports(Language language) {
    return StructuralSearchUtil.getTokenizerForLanguage(language) != null;
  }
}