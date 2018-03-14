# EmgFramework
The project EmgFramework is a Java application which enables the end user to retrieve EMG muscle signals from different sources
and provide handy tools to get insights in the captured data. EMG data indicates muscle contraction and different
sensors can sense this as an analog value. The application is mainly used to capture the sensed data from different
data sources (called clients) and later on analyze the data.
Supported clients are:
* SerialClient,
* SimulationClient,
* NetworkClient,
* BluetoothClient (for Android and for Desktop).

This project is part of my master thesis about smart EMG-enabled textiles. 

## Features

* Different supported EmgClients
* Multi Channel sensing
* Multiple filters per channel
* Frequency analysis
* Simulation data playback
* Store recorded data in csv-file
* Conconi Test Tool

## Modules

### client
TODO...

### clientdriver
TODO...

### core
TODO...

### desktop
TODO...

### messaging
The *messaging* module is the base layer of communication. It contains the interface
for *MessageParser*, which defines the basic message flow between client and server. 
Because of its interface characteristic and its generic data type approach, it is 
theoretically possible to change the message format and its message wrapper class.
Practically the framework uses the concrete implementation *EmgMessageParser*, which
utilizes the class *EmgPacket* as the data container, which is transferred between the 
two sides. EmgPacket enables the transmission of a list of Emg data (introduced in protocol
version V1), a timestamp (V2) and the heart rate (V3) of the test subject. Nevertheless of the
data format, all concrete implementations of *MessageParser* must adhere to the protocol
versioning.

## Versions

### 1.0.0 
- [ ] Implement muscle fatigue detection algorithms
- [x] Handle heart rate messages and relay it to Tools

### 0.9.9
* Various bug fixes
* Improve message handling
* introduce v3 message in order transfer heart rate
* Refactor client specific parts from implementation project to client module (EmgSensor, EmgConnection)
 
### 0.9.8
* Core stability improvements (EmgData, EmgPresenter, EmgClientDriver)
* Support for Peak detection
* VisualView Bug when reconnecting

### 0.9.7
* Bluetooth Client
* Introduction of working Client module
* Store sampling rate in file name of simulation data
* Backwards compatibility to Java 1.6
* Refactor connection logic
* Various code improvements

### 0.8.4
* First build with working client module
* View encapsulation for peak detection
* Introduction of MuscleFatigueTool (entry point for later work)

## 0.7.1
* VisualView disabling
* Desktop Kotlin fixes

## Open points

### Major
* Support **Muscle Fatigue detection** as a Tool
* ~~Support Peak detection~~
* ~~Support **BluetoothClient**~~
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
* ~~VisualView Bug when reconnecting to client (View not emptied on Desktop)~~
* ~~Store sampling rate in file name of simulation data~~
* ~~Write communication library for clients (Arduino)~~
* ~~Remove .jars in /libs and provide dependencies via Gradle~~

### Experimental
* Support .ARFF files as data storage (for machine learning approaches)
* Compile module **EmgClient** with Kotlin Native to run on all client platform
* Detect serial driver and install files automatically, if not installed
