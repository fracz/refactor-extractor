package com.airbnb.epoxy;

import android.support.annotation.LayoutRes;

import java.lang.CharSequence;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;

/**
 * Generated file. Do not modify!
 */
public class ModelForRProcessingTest_ extends ModelForRProcessingTest
    implements GeneratedModel<Object> {

  private OnModelBoundListener<ModelForRProcessingTest_, Object>
      onModelBoundListener_epoxyGeneratedModel;
  private OnModelUnboundListener<ModelForRProcessingTest_, Object>
      onModelUnboundListener_epoxyGeneratedModel;

  public ModelForRProcessingTest_() {
    super();
  }

  @Override
  public void handlePreBind(final EpoxyViewHolder holder, final Object object) {
  }

  @Override
  public void handlePostBind(final EpoxyViewHolder holder, final Object object) {
    if (onModelBoundListener_epoxyGeneratedModel != null) {
      onModelBoundListener_epoxyGeneratedModel.onModelBound(this, object);
    }
  }

  public ModelForRProcessingTest_ onBind(
      OnModelBoundListener<ModelForRProcessingTest_, Object> listener) {
    this.onModelBoundListener_epoxyGeneratedModel = listener;
    return this;
  }

  @Override
  public void unbind(Object object) {
    super.unbind(object);

    if (onModelUnboundListener_epoxyGeneratedModel != null) {
      onModelUnboundListener_epoxyGeneratedModel.onModelUnbound(this, object);
    }
  }

  public ModelForRProcessingTest_ onUnbind(
      OnModelUnboundListener<ModelForRProcessingTest_, Object> listener) {
    this.onModelUnboundListener_epoxyGeneratedModel = listener;
    return this;
  }

  public ModelForRProcessingTest_ value(int value) {
    this.value = value;
    return this;
  }

  public int value() {
    return value;
  }

  @Override
  public ModelForRProcessingTest_ id(long id) {
    super.id(id);
    return this;
  }

  @Override
  public ModelForRProcessingTest_ id(CharSequence key) {
    super.id(key);
    return this;
  }

  @Override
  public ModelForRProcessingTest_ id(CharSequence key, long id) {
    super.id(key, id);
    return this;
  }

  @Override
  public ModelForRProcessingTest_ layout(@LayoutRes int arg0) {
    super.layout(arg0);
    return this;
  }

  @Override
  public ModelForRProcessingTest_ show() {
    super.show();
    return this;
  }

  @Override
  public ModelForRProcessingTest_ show(boolean show) {
    super.show(show);
    return this;
  }

  @Override
  public ModelForRProcessingTest_ hide() {
    super.hide();
    return this;
  }

  @Override
  @LayoutRes
  protected int getDefaultLayout() {
    return R.layout.res;
  }

  @Override
  public ModelForRProcessingTest_ reset() {
    onModelBoundListener_epoxyGeneratedModel = null;
    onModelUnboundListener_epoxyGeneratedModel = null;

    this.value = 0;
    super.reset();
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ModelForRProcessingTest_)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ModelForRProcessingTest_ that = (ModelForRProcessingTest_) o;
    if ((onModelBoundListener_epoxyGeneratedModel == null) != (
        that.onModelBoundListener_epoxyGeneratedModel == null)) {
      return false;
    }
    if ((onModelUnboundListener_epoxyGeneratedModel == null) != (
        that.onModelUnboundListener_epoxyGeneratedModel == null)) {
      return false;
    }

    if (value != that.value) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (onModelBoundListener_epoxyGeneratedModel != null ? 1 : 0);
    result = 31 * result + (onModelUnboundListener_epoxyGeneratedModel != null ? 1 : 0);

    result = 31 * result + value;
    return result;
  }

  @Override
  public String toString() {
    return "ModelForRProcessingTest_{" +
        "value=" + value +
        "}" + super.toString();
  }
}