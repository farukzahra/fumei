---
name: google-calendar-mcp
description: >-
  Create, list, update, and delete Google Calendar events via the Cursor Google
  Calendar MCP plugin. Use when the user asks for a calendar reminder, event,
  appointment, or to schedule something on Google Calendar.
disable-model-invocation: false
---

# Google Calendar (MCP)

Manage Faruk's Google Calendar through the **Cursor Google Calendar plugin MCP** — no Python scripts or extra OAuth setup when the plugin is already connected.

## MCP namespace

| Item | Value |
|------|-------|
| Namespace | `plugin-google-calendar-google-calendar` |
| Auth | OAuth via Cursor plugin (user signs in once in Cursor) |
| Default calendar | `primary` (`farukz@gmail.com`) |

**Discover tools first:** `GetDynamicTools` with `namespace: plugin-google-calendar-google-calendar`.

## Available tools

| Tool | Use |
|------|-----|
| `list_calendars` | List calendars and IDs |
| `list_events` | Events in a time range |
| `get_event` | Verify or read one event by ID |
| `create_event` | New event or reminder |
| `update_event` | Change title, time, description |
| `delete_event` | Remove event |
| `suggest_time` | Find free slots |
| `respond_to_event` | Accept / decline invitations |

## Workflow — reminder or event

1. **Parse intent** — title, date/time, all-day vs timed, timezone, optional agent prompt in description.
2. **Call `GetDynamicTools`** for `create_event` schema (required: `summary`, `startTime`, `endTime`).
3. **Invoke** via `CallDynamicTool`:
   - Namespace: `plugin-google-calendar-google-calendar`
   - Tool: `create_event`
4. **Verify** with `get_event` using returned `id`.
5. **Report** `summary`, date, `htmlLink`, and reminder settings to the user.

## `create_event` patterns

### All-day reminder (check something on a date)

```json
{
  "summary": "Short title",
  "description": "Context + optional Cursor prompt for that day.",
  "allDay": true,
  "startTime": "2026-09-29",
  "endTime": "2026-09-30",
  "timeZone": "America/Los_Angeles",
  "overrideReminders": [
    { "method": "popup", "minutes": 1440 },
    { "method": "popup", "minutes": 0 }
  ]
}
```

- All-day `endTime` is the **next calendar day** (exclusive end).
- `overrideReminders`: `minutes` = how many minutes **before** the event. `0` = at start; `1440` = 24h before.

### Timed meeting

```json
{
  "summary": "Meeting title",
  "startTime": "2026-09-29T10:00:00-03:00",
  "endTime": "2026-09-29T11:00:00-03:00",
  "timeZone": "America/Sao_Paulo",
  "addGoogleMeetUrl": true
}
```

### Description with agent prompt

Put a copy-paste Cursor prompt in `description` so the user can reopen the chat task on that day:

```text
**Prompt para o agente (cole no Cursor):**
> Verifica o status do meu plano que expirou em 28/09/2026...
```

## Timezones

- User local: `America/Sao_Paulo` (UTC-3).
- US Pacific expiry times: `America/Los_Angeles` (GMT-07:00).
- Prefer `timeZone` + ISO 8601 `startTime`/`endTime` over guessing offsets.

## Auth failures

If MCP returns auth errors:

1. User must connect **Google Calendar** in Cursor Settings → MCP / Plugins.
2. Do not store OAuth tokens in the repo or `../secrets/`.
3. Escalate once for OAuth consent or 2FA — then stop.

## Alternative (not default)

[odyssey4me/google-calendar](https://skills.sh/odyssey4me/agent-skills/google-calendar) on skills.sh uses a Python CLI + local OAuth. Prefer **this MCP skill** when the Cursor plugin is available; use odyssey4me only when MCP is unavailable.

## Do not

- Commit calendar event IDs as project config (ephemeral per user).
- Ask the user to create events manually if MCP is connected and authenticated.
- Use browser automation for Calendar when MCP works.
