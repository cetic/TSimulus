.. _dow:

DayOfWeek Generator
-------------------

A time series generator whose values correspond to the day of week of the associated datetime.

**Representation in the configuration document:**

name
    The name associated to the generator describing a DayOfWeek time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘dow’.

base
    Mandatory. The underlying DateTime generator that must for extracting days of week.

**Example**::

    {
      "name": "my-generator",
      "type": "dow",
      "base": {"type": "now"}
    }

