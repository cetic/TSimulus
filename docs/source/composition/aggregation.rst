.. _aggregation:

Aggregation
-----------

A new time series can be obtained by aggregating several time series. More precisely, the values of an aggregate
time series are computed by aggregating the values of the values of the underlying time series.

While the generator is design to apply any arbitrary aggregation function to the provided values,
only some predefined aggregation functions can be specified in the configuration document.

The following aggregation functions are available:

sum
    The sum of all the elements.

product
    The product of all the elements.

min
    The minimal value of all the elements.

max
    The maximal value of all the elements.

mean
    The mean value of all the elements.

Median
    The median value of all the elements.

**Representation in the configuration document:**

name
    The name associated to the generator describing a time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘aggregate’.

aggregator
    Mandatory. Must be either ‘sum’, ‘product’, ‘min’, ‘max’, ‘mean’, or ‘median’.

generators
    Mandatory. A list of the generators describing the underlying time series.


**Example**::

    {
      "name": "aggregate-generator",
      "type": "aggregate",
      "aggregator": "sum",
      "generators": ["daily-generator", "monthly-generator"]
    }

