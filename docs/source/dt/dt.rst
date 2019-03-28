DateTime Generators
===================

DateTime Generators transform DateTime related time series, in order to produce derivated DateTime series or to
extractor useful properties from a base DateTime time series.

In particular, date and time based constraints can be expressed by transforming the timestamp associated with a timeseries
value into a DateTime, and then to extract the hour or the day of the week out of this DateTime. Afterwards,
the extracted property can be included in a comparison generator for instance.

.. include:: /dt/year.rst
.. include:: /dt/month.rst
.. include:: /dt/week.rst
.. include:: /dt/dom.rst
.. include:: /dt/hour.rst
.. include:: /dt/minute.rst
.. include:: /dt/second.rst
.. include:: /dt/ms.rst
.. include:: /dt/doy.rst
.. include:: /dt/dow.rst