# History boundary is not a chat message

Restored chat needs a visible break between newly received messages and the restored span. Putting that break into the transcript meant marking those lines in the text itself, and the mark rendered as a missing glyph.

The history boundary is drawn above the anchor message for the current restore only. It is not a chat message, so it is not saved, copied, or written to the chat log. Older boundaries are not kept. The banner is a template of the disconnect time. The session name and the session key are not part of the banner.

## Considered Options

- A private-use character at the start of each banner line. The transcript can be searched for it, and the character draws as a missing glyph.
- A style or font sentinel on the banner component. Recognition no longer shows a glyph, and it is still inferred from the message.
- A side flag on a real banner message. The line stays in the transcript, the copy buffer, the chat log, and the message cap.
- A saved stack of boundaries. Each restore would leave another banner in later sessions. The product keeps only the current restore.

## Consequences

- The chat renderer opens a gap above the anchor message and fades the banner with that message.
- Stored lines whose text still starts with the legacy private-use prefix are dropped on read and are not written back.
- There is no setting for keeping older boundaries.
- A stored template that is still the old four-line default is replaced with the one-line default. Other templates are left as written, and the removed placeholders are not filled in.
