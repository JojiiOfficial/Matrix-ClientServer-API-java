

# Matrix-ClientServer-API-java
A small and simple java API for the Matrix ClientServer Protocol (see [clientServer api](https://matrix.org/docs/spec/client_server/latest))
The API is still in Beta and known for bugs. If you found or missing a feature one you can create a new issue.


Fork of https://github.com/JojiiOfficial/Matrix-ClientServer-API-java with multiple adaptations

* Add Client#getOrCreateDirectChatRoomSync
* Add Client#getDirectChatRoomsMapSync
* Add Client#resolveRoomAliasSync
* Add Client#loginWithJWTSync
* HttpHelper do not store token, fetch it via Supplier
* Do not copy info (e.g. derive `isLoggedin` via `loginData`)
* HttpHelper pass Authorization via HTTP header not as query parameter
* ...


## Usage

### Login
With credentials
```java
//https not supported yet
Client c = new Client("http://matrix.your.server.xyz:8008");  
c.login("examplebot", "wordpass123", loginData -> {  
	if (loginData.isSuccess()) {    
		//Do sth with the bot
	} else {  
		System.err.println("error logging in");  
	}
});
```
With Usertoken
```java
//https not supported yet
Client c = new Client("http://matrix.your.server.xyz:8008");  
c.login("Y0ur70ken", loginData -> {  
	if (loginData.isSuccess()) {    
		//Do sth with the bot
	} else {  
		System.err.println("error logging in");  
	}
});
```
For an examplebot you can have a look at my [Grep Bot](https://github.com/JojiiOfficial/Matrix-Grep-Bot/)

## Features

- Login
	-	[x] UserID/Password
	-	[x] Usertoken
	
- Events
	-	[x] Receive&Send roomevents (join, messages, typing, ....)
	-	[x] Send files to matrix (thanks to @tsearle)
	-	[x] Get eventdata by EventID
	-	[x] Multiple eventlistener
	-	[x] Receive events happend when bot was offline
	-	[ ] Custom sync filter
- User
    -	[x] Presence
	-	[x] Typing
	-	[x] Receipts
	-	[x] Send text/messages (formatted and raw)
	-	[x] Login/Logout/Logout all
	-	[x] Join/leave room
	-	[x] Get roommembers
	-	[x] Kick
	-	[x] Ban
	-	[x] Unban
	-	[x] Create new room
