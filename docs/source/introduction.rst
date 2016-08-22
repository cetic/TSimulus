Introduction
============

About
-----

This document presents a constrained time series generator. Models used for generating time series are discussed,
as well as the mean the tool can be exploited for carrying out such a generation.

Generated time series can help the experts in various domains, such as meteorology, Internet of Things,
and data scientists, to simulate the variation of values by combining rules constraining this variation with
the randomness being inherently associated to the actual observed variables.

The use of a time series generator makes sense when the considered series can not be easily collected,
or if the values of these series are produced at a frequency that does not allow their reliable storage
in real operating conditions.

Expected use cases of this generator include

* Hardware-specific metric values (CPU load, memory usage, etc.)
* Meteorological observations.
* Stock market values.

Features
--------

