Primary Generators
==================

Primary time series generate values having no relation with any other time series.

The user can constraint the general shape of a primary time series by specifying some points (which correspond to
datetimes associated to specific values), and specify that the generated time series must contain these points.
The values for the instants that don’t belong to any point are interpolated using a cubic spline. The approach followed
for computing this interpolation implies that the user must specify at least three points for constraining the
time series.

.. include:: primary/daily.rst
.. include:: primary/weekly.rst
.. include:: primary/monthly.rst
.. include:: primary/yearly.rst
.. include:: primary/constant.rst
.. include:: primary/stochastic.rst