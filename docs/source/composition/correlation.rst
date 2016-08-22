Correlation
-----------

When a time series correlated to an other one is desired, but the exact relation between the series is not required,
one can use a generator based on a correlation coefficient.

The user must provide a Pearson coefficient (a value between 0 and 1, such that 0 indicates the total lack
of correlation and 1 indicates a perfect correlation), plus the generator of the time series that must be correlated.

The correlated time series tries to generate values in such a way the Pearson coefficient between this series
and the underlying one is as close as possible to the provided one. However, a strict equality between
these two values cannot be guaranteed.

**Representation in the configuration document:**

name
    The name associated to the generator describing a time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘correlated’.

generator
    Mandatory. Describes the generator used for representing the underlying time series that must be
    correlated to the new one.

coef
    Mandatory. The value of the Pearson coefficient that must be approached.

**Example**::

    {
      "name": "corr-generator",
      "type": "correlated",
      "generator": "daily-generator",
      "coef": 0.8
    }

