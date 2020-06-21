.. _sinus:

Sinus Generator
---------------

A time series generator whose values evolve as a sinus function of time.

**Representation in the configuration document:**

name
    The name associated to the generator describing a sinus time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘sinus’.

origin
    Mandatory. Specifies the moment corresponding to the origin of the time frame associated with the time series.
    By definition, the value associated with the origin will be almost 0. Must be a time expressed using ISO 8601.

period
    Mandatory. The duration of a sinus period, expressed in milliseconds.
    By definition, the value associated with the origin + k/2 period will be almost 0, for any integer k.


**Example**::

    {
      "name": "sin",
      "type": "sinus",
      "origin":  "2020-06-07 01:02:03",
      "period": 1000
    }

