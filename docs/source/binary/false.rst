False
-----

A False time series is a binary time series that always produces the value ‘false’.

**Representation in the configuration document:**

name
    The name given to the generator describing the time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be "false".

**Example**::

    {
      "name": "false-generator",
      "type" : "false"
    }

