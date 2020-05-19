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
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockRepositoryAcquiringStrategy;
import info.magnolia.test.mock.jcr.MockEventListenerIterator;
import info.magnolia.test.mock.jcr.MockWorkspace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import static info.magnolia.repository.RepositoryConstants.CONFIG;
import static info.magnolia.test.TestUtil.delayedAssert;
import static it.schm.magnolia.version.beans.ConfigurableVersionConfig.CONFIG_PATH;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurableVersionConfigTest extends RepositoryTestCase {

    @InjectMocks
    private ConfigurableVersionConfig configurableVersionConfig;

    private Node configNode;

    @BeforeEach
    void setup() throws Exception {
        super.setUp();

        ComponentsTestUtil.setInstance(VersionConfig.class, configurableVersionConfig);

        Session session = MgnlContext.getSystemContext().getJCRSession(CONFIG);
        configNode = NodeUtil.createPath(session.getRootNode(), CONFIG_PATH, NodeTypes.ContentNode.NAME);
        session.save();
    }

    @AfterEach
    void teardown() throws Exception {
        super.tearDown();
    }

    @Test
    void when_init_then_configIsLoadedAndEventListenerIsRegistered() throws Exception {
        configurableVersionConfig.init();

        assertThat(configurableVersionConfig.isActive()).isTrue();
        assertThat(configurableVersionConfig.getMaxVersionAllowed()).isEqualTo(3L);

        PropertyUtil.setProperty(configNode, ConfigurableVersionConfig.ACTIVE, false);
        PropertyUtil.setProperty(configNode, ConfigurableVersionConfig.MAX_VERSION_INDEX, 42L);
        configNode.getSession().save();

        delayedAssert(() -> {
            assertThat(configurableVersionConfig.isActive()).isFalse();
            assertThat(configurableVersionConfig.getMaxVersionAllowed()).isEqualTo(42L);
        });
    }

    @Test
    void given_exception_when_init_then_exceptionIsCaught() throws Exception {
        Session sessionMock = mock(Session.class);
        when(sessionMock.getWorkspace()).thenReturn(new MockWorkspace(CONFIG));

        MockRepositoryAcquiringStrategy strategy = new MockRepositoryAcquiringStrategy();
        ((MockContext)MgnlContext.getSystemContext()).setRepositoryStrategy(strategy);
        strategy.addSession(CONFIG, sessionMock);

        ObservationManager observationManagerMock = mock(ObservationManager.class);
        ((MockWorkspace) MgnlContext.getSystemContext().getJCRSession(CONFIG).getWorkspace())
                .setObservationManager(observationManagerMock);

        when(observationManagerMock.getRegisteredEventListeners())
                .thenReturn(new MockEventListenerIterator(emptyList()));
        doThrow(new RepositoryException()).when(observationManagerMock)
                .addEventListener(
                        any(EventListener.class),
                        anyInt(),
                        anyString(),
                        anyBoolean(),
                        eq(null),
                        eq(null),
                        anyBoolean());

        configurableVersionConfig.init();

        assertThat(configurableVersionConfig.isActive()).isTrue();
        assertThat(configurableVersionConfig.getMaxVersionAllowed()).isEqualTo(3L);

        PropertyUtil.setProperty(configNode, ConfigurableVersionConfig.ACTIVE, false);
        PropertyUtil.setProperty(configNode, ConfigurableVersionConfig.MAX_VERSION_INDEX, 42L);
        configNode.getSession().save();

        delayedAssert(3000, 10000, () -> {
            assertThat(configurableVersionConfig.isActive()).isTrue();
            assertThat(configurableVersionConfig.getMaxVersionAllowed()).isEqualTo(3L);
        });
    }

    @Test
    void given_nonInitialisedConfig_when_isActive_then_defaultValueIsReturned() {
        assertThat(configurableVersionConfig.isActive()).isEqualTo(true);
    }

    @Test
    void given_nonInitialisedConfig_when_getMaxVersionAllowed_then_defaultValueIsReturned() {
        assertThat(configurableVersionConfig.getMaxVersionAllowed()).isEqualTo(3L);
    }

}
