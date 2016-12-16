Installation
************

The simplest way to install the TSimulus library is to add it as a dependency in your SBT file:

.. parsed-literal::

    libraryDependencies += "be.cetic" %% "rts-gen" % "\ |ProjectRelease|\ "


Alternatively, you can use add the following in your pom document:

.. parsed-literal::

    <dependency>
        <groupId>be.cetic</groupId>
        <artifactId>rts-gen_2.11</artifactId>
        <version>\ |ProjectRelease|\ </version>
    </dependency>

You can also clone its `source code repository <https://github.com/cetic/tsimulus>`_ from Github and package it using SBT:

.. parsed-literal::
    > git clone https://github.com/cetic/tsimulus.git
    > cd tsimulus
    > sbt package

If you are not interested in the development of this library, you more likely want to know :ref:`use_it`.


