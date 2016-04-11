# ImageSocket_Android
[![Packagist](https://img.shields.io/packagist/l/doctrine/orm.svg?maxAge=2592000)]()</br>
The Android plugin that can used to send the image rapidly.

Abstract
---------------------
This project provide a new class `ImageSocket`. As the usual, you can easily transfer the string.
But It's not easy to transfer the image with a few code. On the Android platform, Google provide 
TCP & UDP two WiFi method to pass information. This project build a basic API that the developer
can transfer the image in a easy way!

Usage
---------------------
TCP:
```java
  ImageSocket imageSocket = new ImageSocket(oppositeHost, 12345);
  imageSocket.setProtocol(ImageSocket.TCP)
      .getSocket(10)
      .connect()
      .getInputStream()
      .send(getImage())
      .close();
```
UDP:
```java
  ImageSocket imageSocket = new ImageSocket(oppositeHost, 12345);
  imageSocket.setProtocol(ImageSocket.UDP)   
      .getSocket(true)             
      .setOppoPort(12345);     
      .send(getImage());       
      .close(); 
```
