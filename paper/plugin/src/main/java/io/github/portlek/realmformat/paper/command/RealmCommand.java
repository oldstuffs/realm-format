package io.github.portlek.realmformat.paper.command;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

@Log4j2
public final class RealmCommand implements TerminableModule {

  @Override
  public void setup(@NotNull final TerminableConsumer consumer) {}
}
