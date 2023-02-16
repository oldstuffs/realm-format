package io.github.portlek.realmformat.paper.nms;

import io.github.portlek.realmformat.format.realm.RealmFormat;

public interface NmsBackend {
  int dataVersion();

  default byte worldVersion() {
    return RealmFormat.dataVersionToWorldVersion(this.dataVersion());
  }
}
