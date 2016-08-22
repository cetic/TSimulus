Yearly Generator
----------------

A yearly time series is based on a yearly cycle. The user specifies the values of the time series for some.

Contrary to daily, weekly, and monthly time series, a yearly time series cannot present any natural cycle,
since the number of the year is on an open scale. Consequently, a cycle is artificially created by considering
that the value following the next mentioned year will be equal to the value associated to the first mentioned year.

Similarly, the value of the year preceding the first mentioned year will be equal to the value associated
to the last mentioned year.


**Representation in the configuration document:**

name
    The name associated to the generator describing a yearly time series. This name must be unique among all generators
    in the configuration document.

type
    Mandatory. Must be ‘yearly’.

points
    Mandatory. Contains some years, as well as the values associated to these years. At least three points must
    be specified. If a year is present several times in the points, only the last declaration of the point
    associated to this year will be retained.

**Example**::

    {
        "name": "yearly-generator",
        "type": "yearly",
        "points": {"2015": 42.12, "2016": 13.37, "2017": 6.022}
    }

