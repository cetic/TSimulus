Purpose
=======

The purpose of this project is to generate random time series. While the randomness aspect implies, by definition,
generated values can not be easily predicted, these values must respect the following properties:

* Continuous or discrete (and, in particular, binary) time series must be generated.

* The user should have the ability to constraint their values, as well as the variation of theses values.

* It must be possible to specify a correlation between time series.

* The user should be able to specify an autocorrelated time series. An autocorrelated time series has a cross-correlation
  with itself (in other words, it has a periodic pattern).

* The user should be able to specify (weak) stationary time series. The mean and/or the variance of a (weak) stationary
  time series do not change over time and do not follow any pattern.