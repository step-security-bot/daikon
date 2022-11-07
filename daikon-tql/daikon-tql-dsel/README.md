# Talend Daikon TQL to DSEL

## Description

This library contains converters to support two-ways conversions of the Talend Query Language (TQL) and the Data Shaping Language (DSEL).


## Differences around the Between function between TQL and DSEL

_{[WIP - see more in JIRA TDP-11506](https://jira.talendforge.org/browse/TDP-11506)}_

We found a functional difference of the `between` function from the TQL language and the one from the DSEL.
It's about the inclusion/exclusion of the beginning and the end values : In DSEL, like in TSQL, the BETWEEN is inclusive and is not configurable, so these values are always included in the values range.
Whereas in TQL, the `between` is configurable with an exclusive default behavior.

So, we've requested to the MapLang team an extension of the existing `between` DSEL function to support the exclusion and the inclusion.
This extension is not breaking change and would consist of the addition of two optional boolean parameters : one to force exclusion of the beginning value and one for the end value.
When the extension will be done, the converters and theirs unit tests will be updated for that.
