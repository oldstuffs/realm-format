package io.github.portlek.realmformat.paper.module;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

@Log4j2
public final class RealmFormatCommandModule implements TerminableModule {

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {}
}
