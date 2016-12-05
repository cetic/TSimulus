Gaussian Noise Generator
------------------------

The gaussian noise generator is an alternative to the stochastic generator. It produces, for any time, a random value the
distribution of which is close to the gaussian, or normal, distribution.

Contrary to the stochastic generator, this generator produces independent values. The gaussian noise generator is therefore
less realistic than the stochastic generator, but in return the generator of gaussian values is in constant time, while
the stochastic generator must produce all the intermediate values between the generator origin to the requested time.

**Representation in the configuration document:**

name
    The name associated to the generator describing a constant time series. This name must be unique among all
    generators in the configuration document.

type
    Mandatory. Must be ‘gaussian’.

seed
    Mandatory. The seed of the random number generator.

std
    Mandatory. The standard deviation of the Gaussian distribution followed by the generated values.

**Example**::

    {
      "name": "generator",
      "type": "gaussian",
      "seed": 42,
      "std": 0.5
    }
