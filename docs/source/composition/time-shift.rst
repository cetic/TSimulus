Time Shift
----------

This transformation time series causes a time shift in an underlying series: a constant time shift is applied
to each time point of a time series, so that the original time series seems to be ‘shifted’ on the time axis.

**Representation in the configuration document:**

name
    The name given to the generator describing a time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘time-shift’.

generator
    Mandatory. A description of the generator used for describing the underlying time series.

shift
    the time, in milliseconds, to add to each time point belonging to the original time series in order
    to evaluated to the time series.

**Example**::

    {
      "name": "time-shift-generator",
      "type": "time-shift",
      "generator": "g1",
      "shift": 60000
    }

