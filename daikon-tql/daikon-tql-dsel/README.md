# Talend Daikon TQL to DSEL

# Table of contents
1. [Description](#description)
2. [Specificities of the converters](#specificities-of-the-converters)
   1. [Usage of comparison operators with numerics](#1-usage-of-the-four-comparison-operators----and--with-numerics)
   2. [DQ Rules, TRR and legacy TQL](#2-dq-rules-trr-and-legacy-tql)
   3. [Wildcard filter in conjunction with the TQL functions 'empty' and 'invalid'](#3-wildcard-filter-in-conjunction-with-the-tql-functions-empty-and-invalid)
   4. [DSEL custom functions](#4-dsel-custom-functions)
   5. [Between function](#5-between-function)


## Description

This library contains converters to support two-ways conversions of the Talend Query Language (TQL) and the Data Shaping Language (DSEL).


## Specificities of the converters

There are some specificities for the converters, in order to handle the constraints of the two modes of Data Preparation :
- The **Runtime Convergence mode** uses the **DSEL** language to handle filters,
- The **legacy mode** uses the **TQL** language to handle filters.


### 1. Usage of the four comparison operators <, >, <= and >= with numerics

In DSEL, all the datatype are comparable, whereas in TQL only the numeric data types are comparable.

So, to have a DSEL expression that contains a comparison operator translated from a TQL expression and functionally equivalent, a prior check that the datatype is a numeric must be added in the DSEL expression.

DSEL has a built-in function named `matches` that permit us to do this prior check by using a complete regular expression to cover all the use cases.
So, we systematically add upstream to the concerned expressions a function call to this `matches` function.

Example of a TQL expression :
 ```
field1 >= 182
```
And the converted DSEL expression :
```
matches(field1,"[ ]*\(?[-\+]?(?:(?:(?:\d*[\.,]?\d+)?[eE](:?[-\+]\d+)?(:?\d*[\.,]?\d+)?)|(?:\d*[\.,]?\d+))%?\)?[ ]*") && field1 >= 182
```


### 2. DQ Rules, TRR and legacy TQL

_TODO 'This paragraph is {WIP}. Need here to talk about : the DQ Rules, about the use of TRR for the DSEL, and about the fact that we could have delays related to the fact that we use DSEL without TRR in the filters. And about the fact this is mandatory to match with the legacy TQL which works in interactive mode._


### 3. Wildcard filter in conjunction with the TQL functions `empty` and `invalid`. 

In TQL, the wildcard character ('*' in grammar) permits to apply a condition to all fields. It's usefull for the "all columns filters" in conjunction with the TQL functions 'empty' and 'invalid'. 

But, in the DSEL language, there's no equivalent of the wildcard character.
And, there's also no equivalent in the native DSEL for the `invalid` TQL function used with the wildcard character whereas the `empty` TQL has DSEL equivalent : it's out of scope for this current paragraph, see more in the paragraph _DSEL custom functions_.

To execute DSEL for filtering, we need to support the TQL to DSEL conversion of the wildcard for the two TQL functions `empty` and `invalid`.

The conversion to be performed for one of this function depends on the call context :
- In runtime : the converter convert the function with the wildcard to a list of unitary and equivalent DSEL functions (one by field) joined by "OR" conditions.
- For database persistence : it converts the function to a non-existent (=fake) DSEL function with the wildcard character. The goal is to permit to re-convert more later this DSEL fake-expression to the original TQL expression.

To understand, here is a concrete example :
- Original TQL expression :
    ```
    * is empty
    ```
    where three fields 'firstName', 'lastName' and 'city' exists and is declared to contain data as 'STRING' data type.
- And the converted DSEL expression in a call context for runtime :
    ```
    * isEmpty(firstName) || isEmpty(lastName) || isEmpty(city)
    ```
- And the converted fake-DSEL expression in a call context for database persistence :
    ```
    * hasEmpty('*')
    ```
- And the converted TQL expression from the fake-DSEL expression in a call context for database persistence :
    ```
    * is empty
    ```
    where we can see this TQL expression is equals to its original.


### 4. DSEL custom functions

Some TQL functions used by the Runtime Convergence mode have no equivalent in the DSEL language or have non-adapted equivalent :

1. `invalid` has no equivalent.
 
2. `complies` and `wordComplies` TQL functions were converted to the 'matches' DSEL built-function :
    It requires to transform the value of the 'pattern' parameter (chars/word pattern) of these TQL methods to a regular expression for the DSEL `matches` function.
    it causes a permanent loss of this pattern in the context of calling for database persistence, because the TQL expression converted to DSEL and stored in db cannot be converted back to TQL to be restored.

Then, we needed to create some DSEL custom functions to overcome these problems, then declare them to the DSEL interpreter, then ship them inside the remote-engine.
For this purpose, a new Daikon module named `Daikon DSEL` was built.
As of October 2022, five DSEL custom functions have been created : `isOfType`, `isValid`, `isInvalid`, `complies`, and `wordComplies`.

Here is a concrete example with the `invalid` TQL function, in conjunction with the wildcard character :
- Original TQL expression :
    ```
    * is invalid
    ```
    where three fields 'name', 'people' and 'code' exists and is declared to contain data as respectively 'STRING' data type, 'INTEGER' data type and 'POSTAL_CODE' semantic type.

- And the converted DSEL expression in a call context for runtime :
    ```
    * isInvalid(name, 'STRING') || isInvalid(people, 'INTEGER') || isInvalid(code, 'POSTAL_CODE')
    ```
- And the converted DSEL expression in a call context for database persistence :
    ```
    * hasInvalid('*')
    ```
- And the converted TQL expression from the fake-DSEL converted expression in a call context for database persistence :
    ```
    * is invalid
    ```
    where we can see this TQL expression is equals to its original.

Here is another concrete example, with the `complies` TQL function :
- Original TQL expression :
    ```
    sentence complies 'Aa aaaa'
    ```
    where the field 'sentence' is declared to contain data as 'STRING' data type.

- And the converted DSEL expression :
    ```
    complies(sentence, 'Aa aaaa')
    ```
- And the converted TQL expression from the DSEL converted expression :
    ```
    sentence complies 'Aa aaaa'
    ```
    where we can see this TQL expression is equals to its original.


### 5. Between function

_{[WIP - see more in JIRA TDP-11506](https://jira.talendforge.org/browse/TDP-11506)}_

We found a functional difference of the `between` function from the TQL language and the one from the DSEL.
It's about the inclusion/exclusion of the beginning and the end values : In DSEL, like in TSQL, the BETWEEN is inclusive and is not configurable, so these values are always included in the values range.
Whereas in TQL, the `between` is configurable with an exclusive default behavior.

So, we've requested to the MapLang team an extension of the existing `between` DSEL function to support the exclusion and the inclusion.
This extension is not breaking change and would consist of the addition of two optional boolean parameters : one to force exclusion of the beginning value and one for the end value.
When the extension will be done, the converters and theirs unit tests will be updated for that.
