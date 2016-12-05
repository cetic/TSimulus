Primary Generators
==================

Primary time series generate values having no relation with any other time series.

A user can constrain the general shape of a primary time series by specifying some data points (which correspond to
points in times associated to specific values), and by specifying that the generated time series must contain these data points.

The values for the points in time that don’t correspond to a declared data point are interpolated by using a cubic spline.
The approach followed for computing this interpolation implies that the user must specify at least three points for constraining the
time series.

.. include:: /primary/daily.rst
.. include:: /primary/weekly.rst
.. include:: /primary/monthly.rst
.. include:: /primary/yearly.rst
.. include:: /primary/constant.rst
.. include:: /primary/stochastic.rst
.. include:: /primary/gaussian.rst