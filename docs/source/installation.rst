Installation
************

How to use this library?
========================

The simplest way to use the RTS-Gen library is to add it as a dependency in your SBT file:

.. parsed-literal::

    libraryDependencies += "be.cetic" %% "rts-gen" % "\ |ProjectRelease|\ "


Alternatively, you can use add the following in your pom document:

.. parsed-literal::

    <dependency>
        <groupId>be.cetic</groupId>
        <artifactId>rts-gen_2.11</artifactId>
        <version>\ |ProjectRelease|\ </version>
    </dependency>

You can also clone its `source code repository <https://github.com/cetic/rts-gen>`_ from Github and package it using SBT:

.. parsed-literal::
    > git clone https://github.com/cetic/rts-gen.git
    > cd rts-gen
    > sbt package



