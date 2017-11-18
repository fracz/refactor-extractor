package org.jetbrains.idea.maven.dom;

import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class MavenConstantListConverter extends MavenPropertyResolvingConverter<String> {
  private boolean myStrict;

  protected MavenConstantListConverter() {
    this(true);
  }

  protected MavenConstantListConverter(boolean strict) {
    myStrict = strict;
  }

  @Override
  public String fromResolvedString(@Nullable @NonNls String s, ConvertContext context) {
    if (!myStrict) return s;
    return getValues().contains(s) ? s : null;
  }

  public String toString(@Nullable String s, ConvertContext context) {
    return s;
  }

  @NotNull
  public Collection<String> getVariants(ConvertContext context) {
    return getValues();
  }

  protected abstract List<String> getValues();
}