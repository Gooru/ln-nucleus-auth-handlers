package org.gooru.nucleus.auth.handlers.bootstrap.startup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gooru.nucleus.auth.handlers.app.components.AppConfiguration;
import org.gooru.nucleus.auth.handlers.app.components.AppHttpClient;
import org.gooru.nucleus.auth.handlers.app.components.DataSourceRegistry;
import org.gooru.nucleus.auth.handlers.app.components.RedisClient;
import org.gooru.nucleus.auth.handlers.app.components.UtilityManager;

public class Initializers implements Iterable<Initializer> {
    private final Iterator<Initializer> internalIterator;

    public Initializers() {
        final List<Initializer> initializers = new ArrayList<>();
        initializers.add(DataSourceRegistry.getInstance());
        initializers.add(RedisClient.instance());
        initializers.add(UtilityManager.getInstance());
        initializers.add(AppHttpClient.getInstance());
        initializers.add(AppConfiguration.getInstance());
        internalIterator = initializers.iterator();
    }

    @Override
    public Iterator<Initializer> iterator() {
        return new Iterator<Initializer>() {

            @Override
            public boolean hasNext() {
                return internalIterator.hasNext();
            }

            @Override
            public Initializer next() {
                return internalIterator.next();
            }

        };
    }

}
