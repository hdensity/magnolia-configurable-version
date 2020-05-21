/*
 * MIT License
 *
 * Copyright (c) 2020 Sam Schmit-Van Werweke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package it.schm.magnolia.version.beans;

import info.magnolia.cms.beans.config.VersionConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.spi.Event;

import javax.inject.Singleton;
import javax.jcr.RepositoryException;

import java.util.Optional;

import static info.magnolia.context.MgnlContext.VoidOp;
import static info.magnolia.context.MgnlContext.doInSystemContext;
import static info.magnolia.jcr.util.PropertyUtil.getBoolean;
import static info.magnolia.jcr.util.PropertyUtil.getLong;
import static info.magnolia.jcr.util.SessionUtil.getNode;
import static info.magnolia.observation.WorkspaceEventListenerRegistration.observe;

/**
 * Configurable versioning configuration class, which allows you to define if versions are created
 * and how many version are kept.
 *
 * By default, this implementation recreates the defaults of the base {@link VersionConfig}.
 */
@Slf4j
@Singleton
public class ConfigurableVersionConfig extends VersionConfig {

    public static final String CONFIG_PATH = "/server/version";

    private boolean active = true;
    private long maxVersions = 3L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        this.load();
        this.registerEventListener();
    }

    /**
     * Loads the bean configuration from the JCR 'config' workspace.
     */
    @Override
    public void load() {
        doInSystemContext(new VoidOp() {
            @Override
            public void doExec() {
                Optional.ofNullable(getNode("config", CONFIG_PATH))
                        .ifPresent(node -> {
                            active = getBoolean(node, ACTIVE, true);
                            maxVersions = getLong(node, MAX_VERSION_INDEX, 3L);
                        });
            }
        });
    }

    /**
     * Registers an event listener to react to JCR configuration changes.
     */
    private void registerEventListener() {
        try {
            observe("config", CONFIG_PATH, events -> ConfigurableVersionConfig.this.reload())
                    .withEventTypesMask(Event.NODE_ADDED | Event.NODE_REMOVED |
                            Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED)
                    .withSubNodes(true)
                    .register();
        } catch (RepositoryException e) {
            log.error("Error adding event listener for version configuration", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMaxVersionAllowed() {
        return maxVersions;
    }

}
