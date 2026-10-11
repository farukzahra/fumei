# Credential vault — store and retry

Secrets live **outside** git repos. From a project root under the shared parent:

```
../secrets/
  github/pat.txt
  vps/ssh/
  projects/<app>.env
  <service>/<purpose>.txt    ← create as needed
```

## When the user gives a credential

1. Confirm format (API key, cookie export, username/password file, OAuth refresh token doc).
2. Write to `../secrets/<service>/<purpose>.txt` or the path the project documents.
3. Do **not** echo the secret back in chat; refer to it by path (relative only in skills/docs).
4. Retry automation immediately — do not ask the user to paste it again next session if the file exists.

## Naming

| Pattern | Example |
|---------|---------|
| Service + account | `../secrets/linkedin/farukz.txt` |
| Service + purpose | `../secrets/openai/project-key.txt` |
| Per-app env | `../secrets/projects/nfe-bot.env` |

## Never

- Commit files under `../secrets/`
- Put absolute paths (`C:\repo\secrets\...`) in skills or committed docs — use `../secrets/` from project root
- Ask for the same credential every run when it is already on disk (read and use it)

## If vault path differs

Check project `AGENTS.md` or `secrets.local.md`. Still prefer **relative** paths in instructions.
