# rtlSpectrum [![Build Status](https://app.travis-ci.com/dernasherbrezon/rtlSpectrum.svg?branch=master)](https://app.travis-ci.com/github/dernasherbrezon/rtlSpectrum) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ru.r2cloud%3ArtlSpectrum&metric=alert_status)](https://sonarcloud.io/dashboard?id=ru.r2cloud%3ArtlSpectrum)

Analyze spectrograms created by rtl_power

# Screenshots

![screen1](/screenshots/1.png?raw=true)
![screen2](/screenshots/2.png?raw=true)

# Features

* load from .csv file produced by rtl_power
* run rtl_power directly. it should be available in the $PATH
* add multiple graphs for analysis
* substract one graph from another
* save/export graph in the rtl_power based format

# Installation

* Ensure you have java installed. Required version is Java 11+
* Go to [Releases](https://github.com/dernasherbrezon/rtlSpectrum/releases) tab
* Download the latest .jar file for your operating system. Supported operating systems are:
  * Linux 64bit
  * Windows 64bit
  * MacOS Intel 64bit
  * MacOS M1
* Run it using the command ```java -jar rtlSpectrum_win.jar```