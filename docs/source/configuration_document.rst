Configuration Document
======================

The shape of the generated time series are defined using **generators**. These generators can be specified in a declarative fashion in
a so-called **configuration document**, which is a JSON document respecting a given structure. This document is made of a *generators* section,
in which generators are declared, a *exported* section containing the list of generators that must be converted into time series, as well as a pair of date
delimiting the time period for which time series values must be generated.

generators
----------

The use of the *generators* section in a configuration document, although optional, is highly recommended in order to describe the
specify the generators describing the time series to generate. The *generators* section is therefore essentially a list
of generators that could be converted into time series. In this section, **composite generators** (i.e: generators based on other generators)
can be specified in two different ways:

- By writing nested objects in the document. When the specification of a generator requires a generator attribute, a JSON object describing
  this attribute can simply be used. There is no limits to such nested, **inline description**, in such a way specified generators can be as complex as desired.
- By using a reference to a top-level generator. When some generator attributes must be used in several places, or when the generator
  designer prefer to explicitly declare intermediate generators in order to obtain configuration documents that can be more easily be read,
  intermediate generators can be declared in the *generators* section with a unique reference id. Everywhere in the document,
  when a generator is required, the unique id can be used instead of an explicit JSON object representing the generator. This description by reference
  only works if referenced generators are defined at the top level of the generators section. In other words, **generators with online description in a nested object
  cannot be used by reference**.

exported
--------

This section of the configuration document lists the generators that must be converted into time series. Again, this is essentially a list of objects
containing the following attributes:

- **name**: the name that must be associated to the time series.
- **generator**: a description of the generator representing the time series to generate. This may be an online description.
- **frequency**: the period, in milliseconds, at which time series values must be generated.


'from' and 'to'
---------------

While the entire library can generate values for any valid moment, and despite the fact that time series are internally
considered as potentially unlimited streams of values, such a boundless generation cannot be processed in a limited time.
Consequently, two extra fields, expressing the beginning and the end of the time period for which values must be generated,
are required in the configuration document in order to be able to generate time series.

However this specificity can be expected to change in the future.

