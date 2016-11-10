# rts-gen

[![Build Status](https://travis-ci.org/cetic/rts-gen.svg?branch=master)](https://travis-ci.org/cetic/rts-gen)

RTS-Gen is a tool for generating random, yet realistic, time series values. In this project, a time series is an orderly sequence of
points in times, each of them being associated to at most a value. Time series are used in a wide variety of areas,
including finance, weather forecasting, and signal processing.

While random-number generators can easily be used for producing sequences of unrelated (or, at least, hardly predictable) numbers,
generating sequences of numbers that seem to respect some *obvious* patterns is also interesting in many circumstances,
including the simulation of data acquisition in the aforementioned areas.

In order to make realistic time series, a *convincing* noise must generally be added to some specified patterns.
In addition, the values of a time series may be related to those of an other time series.

The RTS-Gen project provides tools for specifying the shape of a time series (general patterns, cycles, importance of the added noise, etc.)
and for converting this specification into time series values.
