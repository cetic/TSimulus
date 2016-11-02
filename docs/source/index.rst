.. ts-generator documentation master file, created by
   sphinx-quickstart on Mon Aug 22 10:53:11 2016.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

RTS-Gen - A realistic time series generator
===========================================

About
-----

RTS-Gen is a tool for generating random, yet realistic, time series. In this project, a time series is nothing but a orderly sequence of
points in times, each of them being associated to at most a value. Time series are used in a wide variety of areas,
including finance, weather forecasting, and signal processing.

While random-number generators can easily be used for producing sequences of unrelated (or, at least, hardly predictable) numbers,
generating sequences of numbers that seem to respect some *obvious* patterns is also interesting in many circumstances,
including the simulation of data acquisition in the aforementioned areas.

In order to make realistic time series, a *convincing* noise must generally be added to some specified patterns.
In addition, the values of a time series may be related to those of an other time series.

The RTS-Gen project provides tools for specifying the shape of a time series (general patterns, cycles, importance of the added noise, etc.)
and for converting this specification into time series values.

Features
--------

Generators:
    The RTS-Gen library provides a domain specific language (DSL) that can be used for specifying **generators**
    that describe the shape of the desired time series (evolutionary patterns, cycles, noise, etc.). Alternatively, these
    generators can be programmatically specified using a Java/Scala API. The generators can be converted into time series values
    for a considered time period.

Generator Combination:
    Generators can be **combined** in order to produce higher-level generators. Such generators can describe conditional and time-based
    time series, such that "if the value of this time series is higher than the value of that time series, then this value must be generated. Otherwise, that value must be generated instead."

Numeric and Binary Values:
    Generated time series values may be either numeric or binary.
    Bool operations can be applied on binary values, that can be used for describing conditional time series.
    Numeric values can be combined and compared in different ways, in order to create complex time series by combining simple ones.

Time Series Evaluation:
    Time series can be evaluated for any point of time. This evaluation is fast, side effect-free, and referentially transparent
    (in particular, the evaluation of a time series always provides the same value for a given timestamp).

    Furthermore, the library supports the generation of time series values as a (potentially illimited) number stream.
    With such a structure,

Missing Values:
    A time series may not have a value to provide for any possible timestamp. Such cases are managed by the library as "missing" values.
    Missing values may be conditionally replaced by "default" values and can be discarded from a collection of values before operating an aggregation.


How to use it
-------------

The  library is written in Scala, and is packaged as a jar archive that can be used as any JVM-based library.

An application based on this library is proposed for reading time series configuration from a plain old text file and generating random values.
A **microservice** is also proposed for generating time series values as the answer of a HTTP query.

Have a look at the Quick starter in order to learn how to generated personalized time series values in five minutes.

Licence and Source Code
-----------------------

RTS-Gen is released publicly under the `Apache Licence (version 2.0)<http://www.apache.org/licenses/LICENSE-2.0>`_ and
has been initiated as part of `EAM-SDI<https://www.cetic.be/EAM-SDI-2300>`_, a CWALity research project of the Walloon region.


Contents:

.. toctree::
    :caption: Overview
    :maxdepth: 1

    get_started
    principles
    deployment
