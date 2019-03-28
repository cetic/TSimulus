.. _diff:

DateTimeDifference Generator
----------------------------

A time series generator whose values correspond to the difference between two datetimes.

**Representation in the configuration document:**

name
    The name associated to the generator describing a DateTimeDifference time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘dt::diff’.

a
    Mandatory. One of the underlying datetime generators.
b
    Mandatory. The other underlyinf datetime generator.

**Example**::

    {
      "name": "my-generator",
      "type": "dt::diff",
      "a": {"type": "now"},
      "b": {"type": "now"}
    }

