Weekly Generator
----------------

A weekly time series is based on a weekly cycle. The user specifies the values of the time series for
some days of the week.

In the meteorological domain, a typical weekly generator would consist in a set of days (for instance, Monday)
associated to a temperature of 6°. The corresponding time series will be such that every week,
the temperature associated to Monday, 12AM will be 6°.

**Representation in the configuration document:**

name
    The name associated to the generator describing a weekly time series. This name must be unique among all
    generators in the configuration document.

type
    Mandatory. Must be ‘weekly’.

points
    Mandatory. Contains the days of the week, as well as the values associated to these days. At least three points
    must be specified. If an day is present several times in the points, only the last declaration of the point
    associated to this day will be retained. The days must be expressed in English, with lowercase letters.

**Example**::

   {
     "name": "weekly-generator",
     "type": "weekly",
     "points": {"monday": 6.0, "friday": -3.6, "sunday" : 10.9}
   }

