# CHANGELOG
## 2.7.0
- Change conference api for improving usability in each services
- Modify Websocket handler and 
- Refactor init sequence to create a peer connection
- Update gradle version for examples
- Fix a callback to call after handshaking in websocket
- Fix a issue for video only stream

## 2.6.8
- Modify gradle version for android studio 3.6.1
- Modify conference call initialization
- Fix a creating peer connection

## 2.6.7
- Fix a volume issue using p2p call 

## 2.6.6
- Integrate a setting properties to the Config
- Fix a issue HW volume key is not working on RemonCast
- Fix a setting Config using Builder
- Migrate error handling of websocket to OnError command
- Fix the error code issue to assign specific number
- Add a command for Service OP  

## 2.6.5
- Fix to set up error code with RemonException
- Add a checking routine for peer connection status
- Fix monor bugs


## 2.6.4
- added a feature for selective candidate
- modified a explicit closing about error 
- fixed an illegal process for error, state
- fixed minor bugs

## 2.6.3
- added a new feature to send video through simulcast on RemonCast, RemonConference
- updated the WebRTC library to 1.0.30039
- downgraded an okhttp version to 3.12.x for supporting android api under 20
- deprecated the legacy audio routine
- fixed a puaseLocalVideo( boolean )


## 2.6.2
- added a new feature and functionality for conference call
- fixed an issues for local video capture
- fixed an issue what the sound output device is changed after phone call
- fixed minor bugs

## 2.6.1
- fixed a problem for rest api req/res
- fixed a flag setting for starting capture

## 2.6.0
- modified minor version number in order to synchronize ios sdk
- added local camera capture manager
- fixed problemes with using multiple connection 
- added example for connecting multiple users. examples/SimpleDualCall


## 2.4.15
- added codes for sending log to server
- added exception code for connection


## 2.4.14
- fixed a problem that is not initialized surface views(local,remote) on Remon.createObjects()
- added some comments
- added check routine in willOutputBuffer functions
- updated WebRTC 1.0.26885
- changed static media objects to local
- migrated media functions in PeerConnection to MediaManager
- refactored connection, media codes
- added a null check for startCapture
- fixed some issues the connection to be disconnected when some user has joined
- modified callback thread. Client callbacks of remonCall, remonCast will be called on UI Thread

## 2.4.12
- added a interface method with VideoFrame arguemnt

## 2.4.10
- fixed log url property in makeConfigFromClient()
- modified a duplication sentence in RemonClient, RemonClientBuilder

## 2.4.8
- added a property of customVideoCapturerCreator to RemonCall, RemonCast and Builders

## 2.4.7
- edit simply changes and some unnecessary codes for RemonCall,RemonCast
- migrate to AndroidX

## 2.4.5
- edit createBroadcastChannel msg
- added a external sample function and updated example project
- added a sample project for external capturing

## 2.4.4
- camera choicing from CameraDevices

## 2.4.2
- edit firstFront Camera

## 2.4.1
- add audio release for BT

## 2.2.24
- Set initial camera orientation
- add SwitchCamera Callback
- remove "READ_PHONE_STATE" permission

## 2.2.23
- Front & back side Select only one camera

## 2.2.22
- add close exception
- edit channel close process

## 2.2.21
- edit LoggerFactory in WebsocketClient.java , netty update 4.1.32 -> 4.1.36

## 2.2.20
- should be calling close in runOnUiThread

## 2.2.19
- add Connection pool

## 2.2.18
- edit config and modify

## 2.2.17
- check for isFront camera

## 2.2.16
- add setAudioEnabled Interface

## 2.2.15
- add flag for isUseAutoProgressbar
- edit sample source for server change

## 2.2.14
- remove networkCallback
- edit for Android Pie

## 2.2.12
- In case of Error, unpack is finish
- protect Ilegal Argument
- edit unregisted receiver
- catch for illegal Exception

## 2.2.11
- Added processing for abnormal aecDump files

## 2.2.10
- Prevent duplicate complete callback

## 2.2.9
- edit HealthRating
- add log sending for receive Frame
- edit version
- Automatic list synchronization at 3 second intervals

## 2.2.8
- add Activity check
- add metadata in log

## 2.2.6
- remove AUDIOFOCUS_LOSS in close()

## 2.2.4
- edit audioBitrate values

## 2.2.3
- get ch Id

## 2.2.2
- edit AudioControl module (Music,Call)

## 2.2.1
- edit health report

## 2.2.0
- webrtc Exception handling when the aecdump file is corrupted due to an abnormal termination cause

## 2.1.9
- update for lib
- add value on isCaster
- update webrtc lib
- edit CreateVideoTrack (capture & sink)
- add service id

## 2.1.8
- fixed bluetooth
- change audioManager source
- Modify CloseType Star State Handling
- Add an end callback event when the other party hangs up
- change log server and release
- api18Level Tested on s3

## 2.1.5
- add isChangeAudioMode pram and change record modules

## 2.1.4
- Separate audio mode between call mode and broadcasting mode, supplement bt sco abnormal operation
- edit Typing Noise, OpenSLES value

## 2.1.3
- edit NS, AEC default value
- edit reconnect process
- add new reconnect process
- refactoring for connect process
- change connect module (test complete)
- Fixed to terminate normally when there is no network
- Implement simulcast function and complete test
- feat for simulcast

## 2.1.2
- edit default value

## 2.1.1
- edit chore for sdk update

## 2.1.0
- edit AudioFilter Type, add simulcast function
- edit chore for WebrtcAudio

## 2.0.24
- Fixed volume control bug
- remove volume control type

## 2.0.22
- change Webrtc Audio Track attribute usage
- log sending to Kafka (topic health and log completion)
- add sendLog interface
- add AudioRecord Interface
- 16000 'resampling' processing and efficient algorithm application
- Unpacking efficiency increased by 70%
- Because webrtc dump file is in Little-endian format, it must be in Little-endian format, and clipping is added


## 2.0.21
- Modify the connect, create, and join functions so that they are available at any time if remon is created
- Video codec setting, default h264 to vp8
- Cleanup api for creating aec_dump file For RemonCall and RemonCast, if saveInputAudioToFile is set to true,
- useOpenSLES = false, and isAecDump = true
- Add audio logic to create aec_dump file after Audio Record
- It works according to the value of saveInputAudioToFile, useOpenSLES must be false, and isAecDump must be true. (Applies to v1.0 config only)
- Implement screen and mic recording function using MediaProjection
- add WebViewCall test, edit Optional docs


## 2.0.19
- stop using proximity sensor and modify speakerphone mode to turn it off and on manually
- Fixed the problem that Bluetooth connection was not reconnected, and BluetoothManager stop should be managed by AudioManager
- Modify Proximity Sensor Handling of Bluetooth and Modify Example Project

## 2.0.18
- change the AudioType default value for remonCall and remonCast
- apply settings for audio device settings

## 2.0.17
- apply settings for audio device settings
- set audioType through config setting
- cast is "music", call is "voice" default

## 2.0.16
- setting for audioConstraints madatory
- added for audio performance measurements
- add audio setting interface

## 2.0.12
- docs translated into English
- change the name of existing close and softclose( close->hardClose, softClose->close)
- analyze close and softclose and fix unnecessary resource release when softclose
- soft close audio dispose processing
- fix code and disconnect issues for close processing
- onClose in onDeisconnectChannel
- in the onCreateChannel, Caster comes only with the channelId, and the viewer has the peerId as well
- close on remonCast when onDisconnect
- add softClose()
- edit defaultconfig method
- modified to voice call
- braodcast without surfaceview
- modify callback function name to suit purpose
- update WebRTC 1.0.23430
- edit onCreate, onConnect callback event
- add jcenter build
- edit docs
- remove Lombox Lib
- minor bug fixed
- rename package
- produce broadcasts using remon 2.0 version
- produce samples and configure views
- implement search and create examples
