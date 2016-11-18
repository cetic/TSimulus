Lesser than
-----------

This time series is similar to the precious one, except that the generated binary values are true
if the values of the first numeric time series are lesser than those of the second numeric time series (and false otherwise).
If the value of at least one of the numeric time series is not defined, then the value of the resulting time series
is not defined.

Similarly to the "greater-than" time series, this time series is based on a more generic one that allows the use of
an arbitrary comparator. However, this generic time series can not be expressed by using a configuration document,
and can therefore be only expressed programmatically.

**Representation in the configuration document:**

name
    The name given to the generator describing the time series. This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘lesser-than’.

a
    Mandatory. A description of one of the numeric generators used for generating the binary time series.

b
    Mandatory. A description of the other numeric generators used for generating the binary time series.

strict
    Optional. Specifies of the inequality is strict or not. If not specified, the inequality will be considered as
    non strict.

**Example**::

    {
      "name": "lt-generator",
      "type": "lesser-than",
      "a": "binary-generator-A",
      "b": "binary-generator-B",
      "strict": true
    }
