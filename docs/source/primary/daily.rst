Daily Generator
---------------

A daily time series is based on a daily cycle. The user specifies the values the time series must generated for some
times in the day, and these points are replicated in  any day for which the user could desire a value.

In the meteorological domain, a typical daily generator would consist in a set of times (for instance, 11AM) associated
to a temperature of 6°. The time series generated based on this constraint will be such that every day,
the temperature associated to 11AM will be 6°.

**Representation in the configuration document:**

name
    The name associated to the generator describing a daily time series. This name must be unique among all generators
    in the configuration document.

type
    Mandatory. Must be ‘daily’.

points
    Mandatory. Contains the instants in the day, as well as the values associated to these instants.
    At least three points must be specified. If an instant is present several times in the points,
    only the last declaration of the point associated to this instant will be retained.

**Example**::

   {
     "name": "daily-generator",
     "type": "daily",
     "points": {"11:00:00" : 6, "17:00:00" : 8, "07:00:00": 2}
   }
