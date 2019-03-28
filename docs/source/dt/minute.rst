.. _minute:

Minute Generator
----------------

A time series generator whose values correspond to the minute (in hour) of the associated datetime.

**Representation in the configuration document:**

name
    The name associated to the generator describing a Minute time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘minute’.

base
    Mandatory. The underlying DateTime generator that must for extracting minutes of hour.

**Example**::

    {
      "name": "my-generator",
      "type": "minute",
      "base": {"type": "now"}
    }

