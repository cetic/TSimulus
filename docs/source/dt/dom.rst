.. _dom:

DayOfMonth Generator
--------------------

A time series generator whose values correspond to the day of month of the associated datetime.

**Representation in the configuration document:**

name
    The name associated to the generator describing a DayOfMonth time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘dom’.

base
    Mandatory. The underlying DateTime generator that must for extracting days of month.

**Example**::

    {
      "name": "my-generator",
      "type": "dom",
      "base": {"type": "now"}
    }

