.. _year:

Year Generator
--------------

A time series generator whose values correspond to the year of the associated datetime.

**Representation in the configuration document:**

name
    The name associated to the generator describing a Year time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘year’.

base
    Mandatory. The underlying DateTime generator that must for extracting years.

**Example**::

    {
      "name": "my-generator",
      "type": "year",
      "base": {"type": "now"}
    }

