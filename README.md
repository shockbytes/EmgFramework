# EmgFramework
The project EmgFramework is a Java application which enables the end user to retrieve EMG muscle signals from different sources
and provide handy tools to get insights in the captured data. EMG data indicates muscle contraction and different
sensors can sense this as an analog value. The application is mainly used to capture the sensed data from different
data sources (called clients) and later on analyze the data.
Supported clients are:
* SerialClient,
* SimulationClient,
* NetworkClient.

A Bluetooth(LE)Client for Android is planned at a later point

This project is part of my master thesis about smart EMG-enabled textiles. 

## Features

* Different supported EmgClients
* Multi Channel sensing
* Multiple filters per channel
* Frequency analysis
* Simulation data playback
* Store recorded data in csv-file
* Conconi Test

## Open points

### Major
* Support Peak detection
* Support **BluetoothClient**
* ~~Refactor Analysis views like Tool views~~
* ~~Refactor with **MVC/MVP pattern** to separate logic from views~~
* ~~Refactor Config views for Clients~~
* ~~Support **NetworkClient**~~
* ~~Replace interfaces with RxJava~~
* ~~Support Conconi Test~~
* ~~Refactor all the logic into a **EmgController** class~~
* ~~Introduce a common interface for simulator and serial communicator (tighter integration of recorded data)~~
* ~~Improve performance by replacing/rewriting chart library (decouple chart with interface for easy replacement)~~

### Minor
* VisualView Bug when reconnecting to client (View not emptied on Desktop)
* Pack Analysis tools also in list with a setup() method in EmgView
* Store sampling rate in file name of simulation data
* Support .ARFF files as data storage (for machine learning approaches)
* ~~Write communication library for clients (Arduino)~~
* ~~Remove .jars in /libs and provide dependencies via Gradle~~

### Experimental
* Compile module **EmgMessaging** with Kotlin Native to run on all client platform
* Detect serial driver and install files automatically, if not installed
