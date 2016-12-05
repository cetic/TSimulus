Sliding Window
--------------

Time series values can be aggregated by using a sliding window. At any time, the value of a sliding window time series
is defined as the aggregation of the *recent* values of a baseline time series. A typical use case of this time series
is a mobile average.

If no values are defined by the underlying time series for the considered time window, the produced value is undefined.

While the generator is designed to process any arbitrary aggregation function to the elements belonging to the time window,
only some predefined aggregation functions can be specified in a configuration document.

The following aggregation functions are available :

* **sum**: the sum of all the elements.
* **product**: the product of all the elements.
* **min**: the minimal value of all the elements.
* **max**: the maximal value of all the elements.
* **mean**: the mean value of all the elements.
* **median**: the median value of all the elements.

**Representation in the configuration document:**

name
    The name associated to the generator describing a time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be ‘window’.

aggregator
    Mandatory. Must be either ‘sum’, ‘product’, ‘min’, ‘max’, ‘mean’, or ‘median’.

generator
    Mandatory. The generator describing the underlying time series.

window-length
    The length, in milliseconds, of the considered time window.

n
    The number of evenly spaced moments in the time window to take into account.

**Example**::

    {
      "name": "window-generator",
      "type": "window",
      "aggregator": "sum",
      "window-length" : 5000,
      "generator": "daily-generator",
      "n": 5
    }
