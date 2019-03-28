.. _doy:

DayOfYear Generator
--------------------

A time series generator whose values correspond to the day of year of the associated datetime.

**Representation in the configuration document:**

name
    The name associated to the generator describing a DayOfYear time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘doy’.

base
    Mandatory. The underlying DateTime generator that must for extracting days of year.

**Example**::

    {
      "name": "my-generator",
      "type": "doy",
      "base": {"type": "now"}
    }

