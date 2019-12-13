---
version: 7.2.1
module: https://talend.poolparty.biz/coretaxonomy/42
product:
- https://talend.poolparty.biz/coretaxonomy/23
---

# TPS-3630

| Info             | Value |
| ---------------- | ---------------- |
| Patch Name       | Patch\_20191213\_TPS-3630\_v1-7.2.1 |
| Release Date     | 2019-12-13 |
| Target Version   | 20190620\_1446-V7.2.1 |
| Product affected | Talend Studio |

## Introduction

This is a self-contained patch.

**NOTE**: For information on how to obtain this patch, reach out to your Support contact at Talend.

## Fixed issues

This patch contains the following fixes:

- [7.2.1] Code generated when uncheck 'Use PreparedStatement' for tJDBCInput and tJDBCRow (TDI-42756)

## Prerequisites

Consider the following requirements for your system:

- Talend Studio 7.2.1 must be installed.

## Installation

### Installing the patch using Software update

1) Logon TAC and switch to Configuration->Software Update, then enter the correct values and save referring to the documentation: https://help.talend.com/reader/f7Em9WV_cPm2RRywucSN0Q/j9x5iXV~vyxMlUafnDejaQ

2) Switch to Software update page, where the new patch will be listed. The patch can be downloaded from here into the nexus repository.

3) On Studio Side: Logon Studio with remote mode, on the logon page the Update button is displayed: click this button to install the patch.

### Installing the patch using Talend Studio

1) Shut down Talend studio if it is opened.

2) Extract the zip.

3) Merge the folder "plugins" & "configuration" and its content to "{studio}/plugins" & "{studio}/configuration" and overwrite the existing files.

4) remove the folder "{studio}/configuration/org.eclipse.osgi".

5) Start the Talend studio.

6) Rebuild your jobs.

### Installing the patch using Commandline

Execute the following commands:

1. Talend-Studio-win-x86_64.exe -nosplash -application org.talend.commandline.CommandLine -consoleLog -data commandline-workspace startServer -p 8002 --talendDebug
2. initRemote {tac_url} -ul {TAC login username} -up {TAC login password}
3. checkAndUpdate -tu {TAC login username} -tup {TAC login password}

## Affected files for this patch <!-- if applicable -->

The following files are installed by this patch:

- {Talend\_Studio\_path}/plugins/plugins/org.talend.daikon\_0.31.7.SNAPSHOT.jar
- {Talend\_Studio\_path}/configuration/.m2/repository/org/talend/daikon/daikon/0.31.7/daikon-0.31.7.jar