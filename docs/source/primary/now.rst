Now Generator
-------------

This time series associated any submitted timestamp into this timestamp. In other words, this time series generates
LocalDateTime values, and these values are equal to the timestamp associated with the value. This may be useful,
among others, for specifying time series, the behaviour of which is based on timestamp related properties, such as
the day of the week or the hour of the day.

**Representation in the configuration document:**

name
    The name associated to the generator describing a now time series. This name must be unique among all
    generators in the configuration document.

type
    Mandatory. Must be ‘now’.

**Example**::

   {
     "name": "now-generator",
     "type": "now"
   }

