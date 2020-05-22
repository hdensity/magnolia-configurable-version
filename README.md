# Configurable versions for Magnolia CMS 6.2 CE

[![GitHub](https://img.shields.io/github/license/hdensity/magnolia-configurable-version)](https://github.com/hdensity/magnolia-configurable-version/blob/master/LICENSE)
[![Build Status](https://travis-ci.com/hdensity/magnolia-configurable-version.svg?branch=master)](https://travis-ci.com/hdensity/magnolia-configurable-version)
[![Coverage Status](https://coveralls.io/repos/github/hdensity/magnolia-configurable-version/badge.svg?branch=master)](https://coveralls.io/github/hdensity/magnolia-configurable-version?branch=master)
[![Code Climate maintainability](https://img.shields.io/codeclimate/maintainability/hdensity/magnolia-configurable-version)](https://codeclimate.com/github/hdensity/magnolia-configurable-version)
[![Code Climate issues](https://img.shields.io/codeclimate/issues/hdensity/magnolia-configurable-version)](https://codeclimate.com/github/hdensity/magnolia-configurable-version/issues)
![Maven Central](https://img.shields.io/maven-central/v/it.schm.magnolia/magnolia-configurable-version)
[![Active](http://img.shields.io/badge/Status-Active-green.svg)](https://github.com/hdensity/magnolia-configurable-version)

[Magnolia versions](https://documentation.magnolia-cms.com/display/DOCS62/Versioning) pages, assets and contacts by default when they are published and unpublished. You can also configure versioning for any [other content type](https://documentation.magnolia-cms.com/display/DOCS62/Enabling+versioning+in+content+apps). The Community Edition of Magnolia CMS, by default, [limits the number](https://documentation.magnolia-cms.com/display/DOCS62/Enabling+versioning+in+content+apps#Enablingversioningincontentapps-Numberofversions) of versions kept to 3 and does not allow you to change it. With this module, that number becomes easily configurable.

## Installation

To install the Magnolia Configurable Version module, add a maven dependency to your [webapp bundle](https://documentation.magnolia-cms.com/display/DOCS62/Creating+a+custom+webapp+with+Maven):

```XML
<properties>
    <version.magnolia.configurable.version>1.0.0</version.magnolia.configurable.version>
</properties>

<dependency>
    <groupId>it.schm.magnolia</groupId>
    <artifactId>magnolia-configurable-version</artifactId>
    <version>${version.magnolia.configurable.version}</version>
</dependency>
```

Additionally, to ensure your custom configuration is applied after module defaults have already been loaded, add the following to your [module descriptor](https://documentation.magnolia-cms.com/display/DOCS62/How+to+create+and+use+a+custom+Magnolia+Maven+module+for+custom+Java+components#HowtocreateanduseacustomMagnoliaMavenmoduleforcustomJavacomponents-anc-runtime-dependenciesRuntimedependenciesinthemoduledescriptor), which is usually found under ```src/main/resources/META-INF/magnolia/<module-name>.xml```

```XML
<dependencies>
    <dependency>
        <name>configurable-version</name>
        <version>1.0.0/*</version>
    </dependency>
</dependencies>
```

## Usage

Without further configuration this module recreates the defaults of the base community edition versioning, i.e. versioning is active and at most 3 versions are kept. You can change the default configuration in 3 ways:

1. using a task in a module version handler
1. bootstrapping the necessary configuration
1. manually setting the values at runtime

### Module Version Handler

We provide the ```ConfigureVersionTask``` class, a custom [Task](https://nexus.magnolia-cms.com/content/sites/magnolia.public.sites/ref/6.2/apidocs/index.html?info/magnolia/module/delta/AbstractTask.html) implementation, which you can use in your [module version handler](https://documentation.magnolia-cms.com/display/DOCS62/How+to+create+and+use+a+custom+Magnolia+Maven+module+for+custom+Java+components) to set the configuration of this module:

```Java
public class MyVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new ConfigureVersionTask(true, 42L));

        return tasks;
    }

}
```

Line 6 shows the task in use, which in this case (```true```) enables versioning and (```42L```) tells it to keep 42 versions.

### Bootstrapping

Alternatively you can also [bootstrap](https://documentation.magnolia-cms.com/display/DOCS62/Importing+and+exporting+JCR+data+for+bootstrapping?src=contextnavpagetreemode) the configuration. Add a file named ```config.server.version.xml``` or ```config.server.version.yaml``` in your bootstrap folder and add the following content, replacing ```?active?``` and ```?maxVersions?``` with your required values:

#### [XML](https://documentation.magnolia-cms.com/display/DOCS62/Bootstrapping+in+Maven+modules?src=contextnavpagetreemode)

```XML
<?xml version="1.0" encoding="UTF-8"?>
<sv:node xmlns:sv="http://www.jcp.org/jcr/sv/1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" sv:name="version">
  <sv:property sv:name="jcr:primaryType" sv:type="Name">
    <sv:value>mgnl:content</sv:value>
  </sv:property>
  <sv:property sv:name="jcr:mixinTypes" sv:type="Name">
    <sv:value>mix:versionable</sv:value>
  </sv:property>
  <sv:property sv:name="active" sv:type="String">
    <sv:value>?active?</sv:value>
  </sv:property>
  <sv:property sv:name="maxVersionIndex" sv:type="String">
    <sv:value>?maxVersions?</sv:value>
  </sv:property>
</sv:node>
```

#### [YAML](https://documentation.magnolia-cms.com/display/DOCS62/Bootstrapping+in+light+modules?src=contextnavpagetreemode)

```YAML
'version':
  'active': ?active?
  'maxVersionIndex': ?maxVersions?
```

### Manual

Lastly, you can also [manually change the configuration](https://documentation.magnolia-cms.com/display/DOCS62/Module+configuration#Moduleconfiguration-Intheconfigworkspace) on a running author instance of Magnolia CMS. To achieve this, open the Configuration app and navigate to ```/server/version```. You can adjust the properties ```active``` and ```maxVersionIndex``` to your needs.

Please note, we recommend the former two approaches, as they allow you to put your configuration under source control.

## License

This project is licensed under the MIT License; see the [LICENSE](https://github.com/hdensity/magnolia-configurable-version/blob/master/LICENSE) file for details.

Copyright 2020 &copy; Sam Schmit-Van Werweke
