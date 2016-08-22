True
----

A True time series is a binary time series that always produces the value ‘true’.

**Representation in the configuration document:**

name
    The name given to the generator describing the time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be "true".

**Example**::

    {
      "name": "true-generator",
      "type" : "true"
    }

