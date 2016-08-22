Default Time Series
-------------------

A common behavior time series pattern consists in retrieving the first defined value among those produced by time series.
The default time series is based on an ordered list of time series, and produces the first defined value of these time series.

If the values of all time series are undefined, an undefined value is produced.

**Representation in the configuration document:**

name
    The name given to the generator describing a time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘first-of’.

generators
    Mandatory. A list of generators describing the underlying time series.


**Example**::

    {
      "name": "default-generator",
      "type": "first-of",
      "generators": ["daily-generator", "random-generator"]
    }

