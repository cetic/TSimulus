.. _month:

Month Generator
---------------

A time series generator whose values correspond to the month of the associated datetime.

**Representation in the configuration document:**

name
    The name associated to the generator describing a Month time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘month’.

base
    Mandatory. The underlying DateTime generator that must for extracting months.

**Example**::

    {
      "name": "my-generator",
      "type": "month",
      "base": {"type": "now"}
    }

