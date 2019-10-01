Use Cases
*********

A sensor for Colectme
=====================

Colectme is a set of tools developed in the `EAM-SDI project <https://www.cetic.be/EAM-SDI-2301>`_ for collecting, storing,
and processing log data from data centers.

The TSimulus library has been used in this project as a data source exploited by `a gateway <https://gitlab.com/colectme/gateway>`_
in order to simulate log events representing the history of resources consumption.

Sensors simulation for FADI
=====================

`FADI <https://fadi.cetic.be/>`_ is a Cloud Native platform for Big Data based on mature open source tools. The FADI project is dedicated to making the deployment of Big Data tools simple, portable and scalable. The goal is to provide a straightforward way to deploy open-source systems for Big Data to various infrastructures (private and public clouds). Anywhere you can run Kubernetes, you should be able to run FADI.

The TSimulus library has been used to simulate various sensors from industrial partners in the context of these `many research projects <https://github.com/cetic/fadi#thanks>`_. The simulated data from TSimulus allows to test all the FADI platform from the data ingestion, the data storage, the data analysis to the dashboarding. It is usefull when the data are not ready to be ingested by the FADI platform, allowing to work already on the analysis part and making some tests. 
