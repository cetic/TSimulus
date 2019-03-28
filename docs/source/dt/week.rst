.. _week:

Week Generator
--------------

A time series generator whose values correspond to the week (of year) of the associated datetime.

**Representation in the configuration document:**

name
    The name associated to the generator describing a Week time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘week’.

base
    Mandatory. The underlying DateTime generator that must for extracting weeks of year.

**Example**::

    {
      "name": "my-generator",
      "type": "week",
      "base": {"type": "now"}
    }

