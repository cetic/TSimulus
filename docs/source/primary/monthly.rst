Monthly Generator
-----------------

A Monthly time series is based on a monthly cycle. The user specifies the values of the time series for some months
in the year.

In the meteorological domain, a typical monthly generator would consist in a set of months (for instance, January),
associated to a temperature of -6.3°. The time series generated based on this constraint will be such that every year,
the temperature associated to mid-January will be -6.3°. The selected instant is exactly the mid-term of the month,
so that the actual time may depend on the fact that the considered year is a leap year or not.

**Representation in the configuration document:**

name
    The name associated to the generator describing a monthly time series. This name must be unique among all
    generators in the configuration document.

type
    Mandatory. Must be ‘monthly’.

points
    Mandatory. Contains the months of the year, as well as the values associated to these months. At least three points
    must be specified. If a month is present several times in the points, only the last declaration of the point
    associated to this month will be retained. The months must be expressed in English, with lowercase letters.

**Example**::

   {
     "name": "monthly-generator",
     "type": "monthly",
     "points":  {"january": -6.3, "february": -6.9, "june" : -2.7, "october" : -3.4}
   }
