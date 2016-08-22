XOR
---

Two binary time series can be combined by a logical XOR. The values of the resulting time series will be true
if, and only if, the values of the base time series are different.

If the value of at least one of the base time series is not defined, then the value of the resulting time series
is not defined.

**Representation in the configuration document:**

name
The name given to the generator describing the time series. This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be "xor".

a
    Mandatory. A description of one of the binary generators used for generating the binary time series.

b
    Mandatory. A description of the other binary generators used for generating the binary time series.


**Example**::

    {
      "name": "xor-generator",
      "type": "xor",
      "a": "binary-generator-A",
      "b": "binary-generator-B"
    }

