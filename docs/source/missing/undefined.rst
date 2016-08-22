.. _undefined:

Undefined time series
---------------------

This time series always produces undefined values.

**Representation in the configuration document:**

name
    The name given to the generator describing a time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be "undefined".


**Example**::

    {
      "name": "undefined-generator",
      "type": "undefined"
    }

