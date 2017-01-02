# TSimulus

[![Build Status](https://travis-ci.org/cetic/TSimulus.svg?branch=master)](https://travis-ci.org/cetic/rts-gen) 
[![Coverage Status](https://coveralls.io/repos/github/cetic/TSimulus/badge.svg?branch=master)](https://coveralls.io/github/cetic/TSimulus?branch=master)
[![Documentation Status](https://img.shields.io/badge/docs-latest-brightgreen.svg?style=flat)](http://tsimulus.readthedocs.io/en/latest/?badge=latest)
[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)


TSimulus (formally, RTS-Gen) is a tool for generating random, yet realistic, time series values. In this project, a time series is an orderly sequence of points in times, each of them being associated to at most a value. Time series are used in a wide variety of areas,
including finance, weather forecasting, and signal processing.

While random-number generators can easily be used for producing sequences of unrelated (or, at least, hardly predictable) numbers,
generating sequences of numbers that seem to respect some *obvious* patterns is also interesting in many circumstances,
including the simulation of data acquisition in the aforementioned areas.

In order to make realistic time series, a *convincing* noise must generally be added to some specified patterns.
In addition, the values of a time series may be related to those of an other time series.

The TSimulus project provides tools for specifying the shape of a time series (general patterns, cycles, importance of the added noise, etc.)
and for converting this specification into time series values.

More specifically, the project proposes:

- A way to express time series constraints using JSON documents, as well as a Scala API for programmatically expressing these constraints.
- A convenient way to combine constraint expressions in order to express higher-level constraints. 
- An engine that generates time series values based on the described constraints.
- A command line tool that relies on the engine to generate time series.
- A stateless microservice that provides time series generation services.   


# Installation

TSimulus can be imported in your project by adding the following instruction in your build.sbt file:

```
libraryDependencies += "be.cetic" %% "rts-gen" % "0.1.13"
```

TSimulus requires a standard Java Runtime Environment, as well as the Scala library. 
If this dependency is not locally available the first time you compile your project, the dependency manager will automatically download it for you.
 
# Documentation

The documentation of the latest release is available on [rts-gen.readthedocs.io](https://rts-gen.readthedocs.io).

The sources of the documentation are available in the docs directory and can be compiled using Sphinx.

# Credits

TSimulus is release under the [Apache license](http://www.apache.org/licenses/) (version 2). 

This library is part of the [EAM-SDI](https://www.cetic.be/EAM-SDI-2301) research project, founded by the Walloon Region.
