Stochastic Generator
--------------------

In order to improve the realism of time series, a generator can be used to describe a random walk, based on an
`ARMA model <https://en.wikipedia.org/wiki/Autoregressive%E2%80%93moving-average_model>`_. This model is made of four modules:

* The ‘auto-regression’ module, represented by φ a parameter vector which represents the auto-correlation of the model.
* The ‘mobile average’ module, represented by θ a parameter vector which represents the evolution of the average over time.
* The ‘noise’ module, represented by σ the standard deviation and μ the expectation of a normal distribution.
  This distribution is used for generating a white noise : from this distribution is generated an independent variable which is added to the model.
* A constant c.

From these four modules, a series of values is generated according to the following formula :

.. image:: images/arma-formula.png
    :width: 400 px
    :align: center
    :alt: Formula of the ARMA model

(image from Wikipedia)

Where ε represents the noise module.

In practice, only a few parameters are enough for obtaining a convincing noise : the standard deviation and a
1D autoregression vector provide, in most cases, a satisfying pseudo-random time series.

The random walk proposed by an ARMA model being discrete by nature, the user has to precise the time interval
separating two successive steps. The value of the time series between two successive steps is computed by a
linearly interpolation of these steps.

**Representation in the configuration document:**

name
    The name associated to the generator describing a constant time series. This name must be unique among all
    generators in the configuration document.

type
    Mandatory. Must be ‘arma’.

model
    Mandatory. Must contain the parameter of the ARMA model used in this generator.

phi
    A list representing the parameter vector of the ‘autoregression’ module.

theta
    A list representing the parameter vector of the ‘mobile average’ module.

std
    The standard deviation of the normal distribution associated to the model.

c
    The constant value associated to the model.

seed
    An arbitrary integer value, which will be used in order to ensure the deterministic nature of the generated time series.

timestep
    Mandatory. The duration, in milliseconds, between two successive steps.

origin
    Mandatory. The moment at which the time series is starting.

**Example**::

    {
      "name": "g3",
      "type": "arma",
      "model": {
        "phi": [1,2,3],
        "theta": [4,3,2,1],
        "std": 0.5,
        "c": 4.2,
        "seed": 1809
      },
      "timestep": 180000,
      "origin": "2016-01-01 12:34:56.789"
    }
