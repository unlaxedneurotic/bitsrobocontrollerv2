# RobocontrollerV2
This is an android application that is used to send movement commands to a robot using MQTT protocol. We are using the paho-mqtt library [https://github.com/eclipse/paho.mqtt.android] for our communication with the MQTT server. The application supports 3 kinds of controllers:
1. Button - using a button based interface to send commands
2. Gyro - Move the robot using the accelerometer of the phone
3. Joystick - Move the robot using a joystick

## Architecture
The application has two major components:
- Connector
- Controller

#### A bird's eye view
![flowdiagram](/screenshots/flowdiagram.png)
### Connector
The connector is the object that connects to the MQTT server and publishes all the messages in json format. Any client can subscribe to the topic and convert the received message into a command message of any type. In our case we are directly using the structure of a standard `Twist` message used in ROS.
#### Methods and Attributes
The only two attributes of this class are:
- `mqttClient` - This is the object that will actually connect to the server and perform all communication tasks.
- `publishtopic` - Stores the topic to which we want to publish our messages

The methods present in this class:
- `connectToBroker` - Connects to the MQTT server. It initializes the `mqttClient` and sets all the callbacks.
- `disconnectFromBroker` - Disconnects from the server and destroys the `mqttClient`.
- `sendData` - Publishes a message on `publishtopic` on the server.

### Controller
The controller generates the movement commands that are sent to the `connector` to be published. Each type of controller takes inputs in its own way to generate these messages, but have 2 common methods
- `changeSpeedAndDirection` - This methods takes in various arguments and then sets the local `Twist` message structure to conform to the commands provided by the user.
- `generateJSONMessage` - This methods takes the local `Twist` attribute and converts it into a JSON message which can be sent to the `connector`. The data that is published on the server is in the following format:
```json
{
    "twist_message":{
        "linear":{
            "x":0.2,
            "y":0.0,
            "z":0.0
        },
        "angular":{
            "x":0.0,
            "y":0.0,
            "z":0.2
        }
    }
}
```
You can also control the maximum speed of each controller.

### Screenshots
#### Main Screen
The activity where you input the host url and topic as well as select the type of controller you wish to use.

<img src="/screenshots/main_screen.jpg" alt="mainscreen"  height="700">

#### Controllers
The controller activities

<img src="/screenshots/gyro.jpg" alt="gyro"  height="700">
<img src="/screenshots/button.jpg" alt="button"  height="700">
<img src="/screenshots/joystick.jpg" alt="joystick"  height="700">