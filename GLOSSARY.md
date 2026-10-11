# Better Client

Client-side Minecraft mod. These terms name concepts the features share.

## Chat history

**History boundary**:
The point where the current restore separates newly received chat from the chat loaded for that restore. It records when that session disconnected, and it sits above the anchor message.
_Avoid_: Separator, separator line, restored-history separator, marker

**Anchor message**:
The newest real message still in the restored span.

**Restored span**:
The real chat messages loaded by the current restore.
_Avoid_: Restored history, old chat
