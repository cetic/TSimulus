.. _ms:

Millisecond Generator
---------------------

A time series generator whose values correspond to the milliseconds (of minute) the associated datetime.

**Representation in the configuration document:**

name
    The name associated to the generator describing a Millisecond time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘ms’.

base
    Mandatory. The underlying DateTime generator that must for extracting milliseconds of minute.

**Example**::

    {
      "name": "my-generator",
      "type": "ms",
      "base": {"type": "now"}
    }

