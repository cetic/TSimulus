Implies
-------

Two binary time series can be combined by a logical "implies" (=>). The values of the resulting time series will be true
if, the values of the first time series *implies* the values of the second time series.
If the value of at least one of the baseline time series is not defined, then the value of the resulting time series is not defined.

A => B is equivalent to not(A) OR B

**Representation in the configuration document:**

name
    The name given to the generator describing the time series. This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘implies’.

a
    Mandatory. A description of one of the binary generators used for generating the binary time series.

b
    Mandatory. A description of the other binary generators used for generating the binary time series.

**Example**::

    {
      "name": "implies-generator",
      "type": "implies",
      "a": "binary-generator-A",
      "b": "binary-generator-B"
    }

