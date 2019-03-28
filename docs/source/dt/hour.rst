.. _hour:

Hour Generator
--------------

A time series generator whose values correspond to the hour of the associated datetime.

**Representation in the configuration document:**

name
    The name associated to the generator describing an Hour time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘hour’.

base
    Mandatory. The underlying DateTime generator that must for extracting hours.

**Example**::

    {
      "name": "my-generator",
      "type": "hour",
      "base": {"type": "now"}
    }

