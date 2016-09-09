# play-audio-twilio

1. Import the project in the Android Studio and compile the project
2. Assuming mobile device is connected to the system. Open Terminal window and run this command to deploy the widget on mobile device
./gradlew InstallDebug
3. Switch ON the widget.
4. Make a call to other mobile.
5. App redirect the call to our special SIP/VOIP server (eg.http://twilio.com) (it should be some voip-as-a-service online service).
6. A result outgoing call from one to another go through the server.
7. Server add the sound voice.
