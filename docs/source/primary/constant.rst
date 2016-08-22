.. _constant:

Constant Generator
------------------

A time series generator whose values are constant over time can be described.

**Representation in the configuration document:**

name
    The name associated to the generator describing a constant time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘constant’.

value
    Mandatory. Specifies the value of the time series.


**Example**::

    {
      "name": "cst",
      "type": "constant",
      "value":  17.5
    }

