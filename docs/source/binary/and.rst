And
---

Two binary time series can be combined by a logical AND. The values of the resulting time series will be true
if, and only if, the values of both base time series are true. If the value of at least one of the base time series
is not defined, then the value of the resulting time series is not defined.

**Representation in the configuration document:**

name
    The name given to the generator describing the time series. This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘and’.

a
    Mandatory. A description of one of the binary generators used for generating the binary time series.

b
    Mandatory. A description of the other binary generators used for generating the binary time series.

**Example**::

    {
      "name": "and-generator",
      "type": "and",
      "a": "binary-generator-A",
      "b": "binary-generator-B"
    }

