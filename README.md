# OneCall

🔐 Step 1: Set Up Firebase Project
Create a Firebase project at Firebase Console

Enable Authentication (Email/Password or Phone)

Set up Firestore or Realtime Database (used for signaling)

Enable Firebase Cloud Messaging (FCM) for push notifications

📲 Step 2: Build the Android Project
Create an Android Studio project

Add Firebase SDKs: Authentication, Firestore/Realtime Database, FCM

Add WebRTC dependency (like org.webrtc:google-webrtc)

Request runtime permissions for Microphone and Internet

🔁 Step 3: Handle Signaling via Firebase
This involves exchanging connection details between the caller and callee.

Caller Side:

Initiate a call: create a document in Firestore with an "offer" (SDP)

Notify the receiver via FCM

Callee Side:

Receives the FCM push and opens the call UI

Fetches the offer from Firestore and responds with an "answer" (SDP)

📞 Step 4: Set Up WebRTC for Audio Streaming
Initialize PeerConnectionFactory

Create a PeerConnection and add AudioTrack from local MediaStream

On receiving an "answer", set it as the remote description

You’ll also need:

ICE Candidate exchange (shared via Firestore during connection setup)

STUN/TURN servers to help with NAT traversal and establish a connection

🎨 Step 5: Build the Call UI
Design a simple in-call screen with:

Call status ("Connecting...", "In Call")

Mute, End Call buttons

Show incoming call dialog when FCM is received

📡 Step 6: Test & Polish
Test on multiple devices to ensure peer connections work reliably

Handle call interruptions and lifecycle states

Add audio quality improvements (e.g., echo cancellation)
