Not
---

A binary time series can be defined as the negation of an other binary time series.
The values of the resulting time series will be true if the values of the base time series are false, and vice versa.

If the value of the base time series is not defined, then the value of the resulting time series is not defined.

**Representation in the configuration document:**

name
    The name given to the generator describing the time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be "not".

generator
    Mandatory. A description of the binary generator used for generating the new binary time series.


**Example**::

    {
      "name": "not-generator",
      "type": "not",
      "generator": "binary-generator"
    }


