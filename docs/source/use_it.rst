How to Use the Library?
***********************

.. _use_it:

Time Series Generation
======================

Time series can be generated according to the rules described in a configuration document. You may consider to use
our `CLI application <https://github.com/cetic/rts-gen-cli>`_ that reads configuration documents from a regular JSON file.

Alternatively, you could consider to use `our microservice <https://github.com/cetic/rts-gen-ms>`_ that listen for HTTP requests. In that case, the configuration document
is submitted as a parameter of a POST request.

A First Example
===============

Examples of complete configuration documents, as well as the way to convert them in order to generate time series values,
are available in the :ref:`get_started` section of this documentation.

Use Case: A Colectme sensor
===========================

Colectme is a set of tools developed in the `EAM-SDI project <https://www.cetic.be/EAM-SDI-2301>`_ for collecting, storing,
and processing log data from data centers.

The RTS-Gen library has been used in this project as a data source exploited by `a gateway <https://gitlab.com/colectme/gateway>`_
in order to simulate log events representing the history of resources consumption.