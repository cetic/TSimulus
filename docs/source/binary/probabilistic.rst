Probabilistic binarization
--------------------------

A logistic distribution model may be used for generating a binary time series.
This model associates to each value of a continuous time series a probability that the produced binary value is true.

The logistic distribution model is characterized by two parameters:

* **location**: represents the position of the model on the continuous variable axis. Its value corresponds
  to the median of the continuous values. In other words, it corresponds to the value associated to a probability
  of 50% that the binary variable is true.

* **scale**: A strictly positive value representing the scale of the model, ie the « speed » at which
  the binary values switch from the state ‘false’ to the state ‘true’ when the associated continuous
  values increase. Common values for this parameter are between 1 and 5.

**Representation in the configuration document:**

name
    The name given to the generator describing a time series.
    This name must be unique among all generators in the configuration document.

type
    Mandatory. Must be "threshold".

generator
    Mandatory. The generator describing the underlying continuous time series.

location
    Mandatory. Represents the model location.

scale
    Mandatory. Represents the model scale.

seed
    Optional. An arbitrary integer used in order to get a deterministic time series.


**Example**::

    {
      "name": "logistic-generator",
      "type": "logistic",
      "generator": "g1",
      "location": 6,
      "scale": 2.4,
      "seed": 1809
    }

