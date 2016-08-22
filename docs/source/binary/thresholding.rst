Thresholding
------------

A binary time series may be described on the basis of a continuous time series, the values of which are transformed
according to a given threshold: if the continuous value is greater than a given threshold, the binary value will be true.
Otherwise, the binary value will be false.

**Representation in the configuration document:**

name
    The name given to the generator describing a time series. This name must be unique among all generators in
    the configuration document.

type
    Mandatory. Must be « threshold ».

generator
    Mandatory. The generator describing the underlying continuous time series.

threshold
    Mandatory. The value so that, any value greater or equal to this threshold will be transformed into a true.

including
    Optional. Determines if an equality between the value and the given threshold must results in a binary
    value set to true. If this parameter is set to true, the threshold value will be accepted.
    Otherwise, the threshold value will be rejected. By default, this parameter is set to true.

**Example**::

    {
      "name": "threshold-generator",
      "type": "threshold",
      "generator": "g1",
      "threshold": 6,
      "including" : "false"
    }
