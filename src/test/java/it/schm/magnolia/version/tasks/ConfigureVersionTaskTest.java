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

package it.schm.magnolia.version.tasks;

import info.magnolia.module.InstallContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import org.junit.jupiter.api.Test;

import static info.magnolia.cms.beans.config.VersionConfig.ACTIVE;
import static info.magnolia.cms.beans.config.VersionConfig.MAX_VERSION_INDEX;
import static it.schm.magnolia.version.beans.ConfigurableVersionConfig.CONFIG_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigureVersionTaskTest {

    @Test
    void given_smallerThanZeroMaxVersions_when_createTask_then_exceptionIsThrown() {
        assertThatThrownBy(() -> new ConfigureVersionTask(true, -42L))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void given_validParameters_when_createTask_then_descriptionIsGenerated() {
        assertThat(new ConfigureVersionTask(true, 42L).getDescription())
                .isEqualTo("Configure versioning to be enabled and keep 42 versions");
        assertThat(new ConfigureVersionTask(false, 43L).getDescription())
                .isEqualTo("Configure versioning to be disabled and keep 43 versions");
    }

    @Test
    void given_nonExistingConfigPath_when_execute_then_noErrorIsGenerated() throws Exception {
        runTaskTest(SessionTestUtil.createSession("config", "/server"));
    }

    @Test
    void given_existingPath_when_execute_then_parametersAreSetAsExpected() throws Exception {
        runTaskTest(SessionTestUtil.createSession("config", CONFIG_PATH));
    }

    private void runTaskTest(MockSession mockSession) throws Exception {
        InstallContext installContextMock = mock(InstallContext.class);
        when(installContextMock.getJCRSession("config")).thenReturn(mockSession);

        // WHEN
        new ConfigureVersionTask(true, 42L).execute(installContextMock);

        // THEN
        assertThat(mockSession.getNode(CONFIG_PATH).getProperty(ACTIVE).getBoolean()).isTrue();
        assertThat(mockSession.getNode(CONFIG_PATH).getProperty(MAX_VERSION_INDEX).getLong()).isEqualTo(42L);
    }

}