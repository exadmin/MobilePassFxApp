# About MobilePassFxApp
## Short info
MobilePassFxApp is a standalone javaFX-based application which automates VPN connection establishing with "Cisco AnyConnect Secure Mobility" application and 2FA enabled.
The application recognizes second factor provided by "MobilePASS+" mobile application using computer vision and calls Cisco CLI tool to start VPN.

## How it works
### Classic flow when you need to establish VPN connection
- You open Cisco AnyConnect Secure Mobility application
- Click "Connect" button
- As soon as handshake phase completed - the dialog window is opened where you need to provide: Password and Second Factor
- To get second factor - you open your "MobilePASS+" mobile application, unblock it and see second factor
- You click "Ok" and wait until VPN connection is established successfully
- If there was a mistake when entering any secret - you need to repeat whole procedure from the very beginning

### New flow when you need to establish VPN connection
- You open "MobilePassFxApp"
- You open "MobilePASS+" mobile application, unblock it and see second factor
- You show screen of your mobile device into the web-camera of your laptop
- As soon as digits are recognized successfully - automatic establishing process will be started

# How to install
## Requisites
To start using the application you will need:
- Windows OS (currently only windows specific is supported and tested)
- Cisco AnyConnect Secure Mobility application installed on your PC (you should already use it in manual mode)
- JDK, version 1.0.8_202 (minimal version successfully passed tests. There are issues with cryptography, see https://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters)
- MobilePASS+ mobile application (currently only Android version 2.2.1-2022032201 is tested)
- GIT and Apache Maven (optional - is needed to make distributive from sources)

## Secure flow (recommended)
* Download source code of the MobilePassFxApp from repository: `git clone https://github.com/exadmin/MobilePassFxApp.git`
* Ensure you are ok with implementation
* Compile it using Apache Maven: `mvn clean compile assembly:single install [-Pmyprofile]`
* Find new folder "dist"
* Create new folder for the application somewhere on your disk, for instance "C:\MobilePassFxApp" and copy content of the "dist" folder into it
* Modify content of the "run-app.cmd" file to fit your environment
* `cd c:\MobilePassApp_v1.2`
* `start "" "c:\Program Files (x86)\Java\jdk1.8.0_202\bin\javaw.exe" -jar MobilePassFxApp-1.2.jar`
* Create a shortcut on the desktop to the run-app.cmd of the application
* (optional) set "Run as administrator" for the shortcut
* Application is ready for usage

## Less secure flow (but still safe)
* Download already built binary distributive of the application
* Complete all steps from the previous topic starting from "Create new folder for the application somewhere..."


# Security aspects
- The application stores your Password in the Java KeyStore file which you will provide. The password to the KeyStore is stored in the properties file in plain-text format. So this only guarantees that no one accidentally will see your password on the screen. But is still possible to steel your password form keystore if there is acceess to the file.
- No logging of your password is implemented, neither into log files nor into console
- No network connections are done (or implemented) by the application itself


# Installation process (step by step from scratch)
* Download and install Oracle JDK : https://www.oracle.com/cis/java/technologies/javase/javase8-archive-downloads.html
* Download and install Git: https://git-scm.com/downloads
* Download and install Apache Maven: https://dlcdn.apache.org/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.zip

Following maven setting were used:
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <localRepository>${user.home}/.m2/repository</localRepository>
    <interactiveMode>true</interactiveMode>
    <offline>false</offline>
	
	 <profiles>
        <profile>
            <id>default</id>
            <repositories>
				<repository>
					<id>central</id>
					<name>Maven Central</name>
					<url>https://repo1.maven.org/maven2/</url>
					<releases>
						<enabled>true</enabled>
						<updatePolicy>never</updatePolicy>
					</releases>
					<snapshots>
						<enabled>false</enabled>
					</snapshots>
				</repository>
			</repositories>
        </profile>
    </profiles>
</settings>
```

All next build steps were done in the `c:\Temp` working dir
* Clone opencv4j repository. This is a java-wrapper over opencv libraries `c:\Temp>git clone https://github.com/exadmin/opencv4j.git`
* Build and install it into local repository: c:\Temp\opencv4j>mvn clean install
* Clone MobilePassFxApp repository: `git clone https://github.com/exadmin/MobilePassFxApp.git`
* Build it: `mvn clean compile assembly:single install -Pdefault`
* Create new folder: "c:\Program Files\MobilePassFxApp" 
* Copy content of "dist" folder into "c:\Program Files\MobilePassFxApp" 
* Edit "c:\Program Files\MobilePassFxApp\run-app.cmd"
Following content was used in example:
`cd "c:\Program Files\MobilePassFxApp"
start "" "c:\Program Files (x86)\Java\jdk1.8.0_202\bin\javaw.exe" -jar MobilePassFxApp-1.2.jar`
* Create a shortcut for the run-app.cmd placing it on desktop
