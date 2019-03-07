package org.catalyst.biascorrect;

import com.google.inject.AbstractModule;

public class StartupBootstrapModule extends AbstractModule {

    @Override
    public void configure() {
        bind(StartupBootstrap.class).asEagerSingleton();
    }
}
