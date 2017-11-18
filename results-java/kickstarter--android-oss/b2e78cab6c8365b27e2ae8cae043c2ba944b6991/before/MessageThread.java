package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class MessageThread implements Parcelable {
  public abstract @Nullable Boolean backing();
  public abstract boolean closed();
  public abstract long id();
  public abstract Message lastMessage();
  public abstract User participant();
  public abstract Project project();
  public abstract int unreadMessagesCount();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder backing(Boolean __);
    public abstract Builder closed(boolean __);
    public abstract Builder id(long __);
    public abstract Builder lastMessage(Message __);
    public abstract Builder participant(User __);
    public abstract Builder project(Project __);
    public abstract Builder unreadMessagesCount(int __);
    public abstract MessageThread build();
  }

  public static Builder builder() {
    return new AutoParcel_MessageThread.Builder();
  }

  public abstract Builder toBuilder();
}