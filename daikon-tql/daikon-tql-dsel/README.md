# Talend Daikon TQL to DSEL

## Description

This library contains converters to support two-ways conversions of the Talend Query Language (TQL) and the Data Shaping Language (DSEL).


## Particularities around the Between function between TQL and DSEL

Like in the SQL language, the `between` function in DSEL has an inclusive default behavior, whereas TQL has exclusive.

Since the 1.7.0 release version of MapLang, the Between function of DSEL has options to force exclusion of the beginning value and one for the end value.
The converters TQL <> DSEL in Daikon and theirs unit tests have been updated to match to these new options.