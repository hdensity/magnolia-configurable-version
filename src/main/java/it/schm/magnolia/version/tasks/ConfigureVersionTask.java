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
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CreateNodePathTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import lombok.extern.log4j.Log4j2;

import static com.google.common.base.Preconditions.checkArgument;
import static info.magnolia.cms.beans.config.VersionConfig.ACTIVE;
import static info.magnolia.cms.beans.config.VersionConfig.MAX_VERSION_INDEX;
import static info.magnolia.repository.RepositoryConstants.CONFIG;
import static it.schm.magnolia.version.beans.ConfigurableVersionConfig.CONFIG_PATH;

/**
 * This class is a task you can use to configure versioning during bootstrapping in your module version handler.
 *
 * @see info.magnolia.module.DefaultModuleVersionHandler
 */
@Log4j2
public class ConfigureVersionTask extends AbstractTask {

    private final boolean enabled;
    private final long maxVersions;

    /**
     * Creates a new instance of this task.
     *
     * @param enabled     Whether to enable or disable versioning
     * @param maxVersions The number of versions to keep, must be bigger than 0
     */
    public ConfigureVersionTask(boolean enabled, long maxVersions) {
        super(
                "Configure versioning",
                String.format("Configure versioning to be %s and keep %d versions",
                        enabled ? "enabled" : "disabled", maxVersions));

        checkArgument(maxVersions > 0, "maxVersions must be bigger than 0");

        this.enabled = enabled;
        this.maxVersions = maxVersions;
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        Task task = new ArrayDelegateTask("",
                new NodeExistsDelegateTask(
                        "", CONFIG_PATH, null, new CreateNodePathTask("", "", CONFIG, CONFIG_PATH)),
                new SetPropertyTask("", CONFIG, CONFIG_PATH, ACTIVE, enabled),
                new SetPropertyTask("", CONFIG, CONFIG_PATH, MAX_VERSION_INDEX, maxVersions));

        task.execute(installContext);
    }

}
