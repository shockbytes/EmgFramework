# EmgFramework
The project EmgFramework is a Java application which enables the end user to retrieve EMG muscle signals from different sources
and provide handy tools to get insights in the captured data. EMG data indicates muscle contraction and different
sensors can sense this as an analog value. The application is mainly used to capture the sensed data from different
data sources (called clients) and later on analyze the data.
Supported clients are:
* SerialClient,
* SimulationClient,
* NetworkClient,
* MqttClient
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
This part of the framework is designed to run exclusively on the client platform. This module
only contains abstract classes or interface definitions, due to the simple fact, that most 
code running on the client is device specific. But why is it necessary then to encapsulate
this in an own module? Because the scaffold is the same for every client device. The code
for connection (whether it is a Bluetooth, Wifi or Serial connection), the actual 
data sensing (depending on the used sensing hardware) and the heart rate provider (usually
not on-device, either smart watch or chest belt) are dependant on the actual target platform.
For example a client using Bluetooth, running on Android Things, connected with a BLE chest
belt, sensing with a specific EMG sensor has different implementation details, than a Wifi
client running on an Arduino platform.

### clientdriver
Counter part of the *client* module. While the client module is deployed on the client platform,
this module is essential in order to communicate with the connected client. The utilized
MessageParser version is crucial, as the protocol versions are backwards compatible, but not the
other way round. A V3 client driver cannot parse V1 message, but a V3 client can send data which
a V1 driver can interpret. Each driver will have some parameters to configure. This configuration
is done via the *ClientDriverConfigView*. So each implementing platform can offer a UI in order
to adjust those parameters. These parameters are usually ports,data sources, ip addresses
or MAC addresses. Client driver must be implemented according to their target platform. 
For example: the Desktop implementation needs an own Bluetooth implementation, while the Android
implementation needs an own as well. ClientDriver which are not platform dependent come already
packed in the *core* module.

### core
The code that holds everything together. The module which connects all other modules. In a perfect
case UI implementations (Android/Desktop) only depend on this module. All code is completely
platform independent, in order to assure it is safe to run it on any target platform. The core
implementation comes with two out-of-the-box supported driver: a *network driver* and a 
*simulation driver*. The simulation driver is a special case of driver, because there is no real
client on the other side. It simulates the client by reading from a file. It provides frequency
analysis methods, namely a Fast-Fourier-Transform and a Spectrum Analysis. The supported filter
are implemented here as well. Tools provide a unique interface to do whatever developer wants
to do with the data. They are a very powerful component. For now the core module offers full
support of the Conconi Test as a tool, a peak detection tool and in near future the muscle fatigue
detection as a tool. All of these tools are completely implemented, but without a corresponding
view. UI modules only have to create the views in order to be capable to use these tools. The
core also provides a way to store recorded data in different formats (for now only csv is possible),
and a way to store other data onto the file system. All of this functionality is very slim and
only needs some hundred of kB as a library.

### desktop
Next to an Android user interface implementation there is also exists a 
Desktop implementation. Usually the Android application is the preferred way to use
the framework as it offers the support to be fully mobile while testing the system. 
But for debugging and testing purposes the Desktop version offers an easier approach.
Especially when it comes down to bug tracking and system testing, it is easier to search
for those systematic bugs on the Desktop. It also provides a more capable hardware to run on.
The *desktop* module basically contains no business logic code. It mostly contains UI specific
code, which is due to the fact, that the architectural concept behind is a Model-View-Presenter
pattern. MVP completely encapsulates views from the business logic. This made a port to Android
really easy. The only business logic is driver logic. The desktop module comes with an own
driver for serial communication and for Bluetooth on Desktop devices (Android comes with an own
bluetooth implementation).

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
- [ ] Introduce the Acquisition Case Designer
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

#### Test class (Experimental)
During a meeting there came the need for a test class. This is just an experimental feature.
An abstract base class, which provides name of the test subject with a date as well (maybe
some more advanced details of the subject). A pipes-and-filters architecture is exerted.
People should be capable to easily create new test cases by just drag and drop items of a 
predefined category (filtering (different filter in row), transformation (also in the 
frequency domain, resample item), data sink (send the data somewhere, 
plot or file, or algorithm)).
The UI is not mandatory, but it would be an impressive detail.

### Major
* Support for **Acquisition Case Designer**
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
