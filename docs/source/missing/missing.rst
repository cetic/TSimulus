Missing values
==============

A time series essentially produces a sequence of values associated to equality spaced points in time.
However, in practice, some values may be missing, because the measured values do not exist before or after a given date.
When simulating the measure of a phenomenon, one could also desire to simulate the fact that the simulated sensor
misses a measure, so that ‘holes’ appear in the time series.

The generator presented in this document support missing values by generating time series that can associate
undefined value to any submitted timestamp. :ref:`Composite <composition>` time series take into account the fact that
the baseline time series may produce such undefined values in a conservative way: a transformation time series produce
an undefined value if its baseline time series produces an undefined value. The transformation applied may transform a
defined value into an undefined one.

:ref:`aggregation` time series discard all the undefined values produced by the baseline time series, so that
the aggregation only applies on defined values. If no defined values are produced by the underlying time series,
an undefined value is retrieved, regardless the aggregation function used on the time series.

While any time series can produce undefined values, some of them are specifically designed to transform
a defined value into an undefined one, and *vice versa*.

.. include:: /missing/undefined.rst
.. include:: /missing/limited.rst
.. include:: /missing/partial.rst
.. include:: /missing/default.rst
