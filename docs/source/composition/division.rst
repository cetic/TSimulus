Division
--------

A special kind of aggregation consists on the division of the values of a time series by the values of an
other time series. Contrary to the other proposed aggregation, time series division can only be based on exactly
two time series.

If the value of the numerator or the denominator is not defined, the result of the division is not defined.

**Representation in the configuration document:**

name
    The name associated to the generator describing a time series.
    This name must be unique among all generators in the configuration document.
type
    Mandatory. Must be ‘divide’.

numerator
    Mandatory. The generator that must be used as numerator.

denominator
    Mandatory. The generator that must be used as denominator.

**Example**::

    {
      "name": "division-generator",
      "type": "divide",
      "numerator": "daily-generator",
      "denominator": "another-generator"
    }

