.. _composition:

Composite Generators
====================

Time series generators can be composed in order to form composite generators. Two types of composite generators
 can be distinguished:

* **Transformation generators**, which are based on an other generator and transform the values of its time series.
* **Combined generators**, which are based on multiple generators and combine the values of their time series
  in order to produce new values.

Two methods are available for mentioning the generators describing a composed time series : **references** and
**inline definitions**.

A reference is a textual label corresponding to the name of the referred generator. There must be a generator having
this name in the configuration document, otherwise an error occurs. A generator does not need to be defined
before being referred, but referred generators need to be first-level generators, i.e., they cannot be defined as a part
of a composite generator.

The inline definition of a generator consists in writing a JSON document representing this generator.

.. include:: /composition/affine.rst
.. include:: /composition/aggregation.rst
.. include:: /composition/division.rst
.. include:: /composition/correlation.rst
.. include:: /composition/time-shift.rst
.. include:: /composition/transition.rst
.. include:: /composition/sliding-window.rst
