package io.github.portlek.realmformat.format.format.realm.v1;

import io.github.portlek.realmformat.format.format.realm.RealmFormatWorld;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class RealmFormatWorldV1 implements RealmFormatWorld {

  private byte worldVersion;

  @Override
  public byte version() {
    return 1;
  }
}
