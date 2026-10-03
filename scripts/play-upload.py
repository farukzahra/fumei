#!/usr/bin/env python3
"""Inspect Google Play tracks or upload the release AAB built by play-release.ps1.

The service account key lives outside the repository, in
../secrets/google-play/service-account.json. The AAB path, version code and
version name come from docs/play-store/last-release.json, written by
scripts/play-release.ps1.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError
from googleapiclient.http import MediaFileUpload

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]
REPO_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_KEY = REPO_ROOT.parent / "secrets" / "google-play" / "service-account.json"
PACKAGE_NAME = "fumei.faruk.dev.br"
MANIFEST_PATH = REPO_ROOT / "docs" / "play-store" / "last-release.json"
RELEASE_NOTES_PATH = REPO_ROOT / "docs" / "play-store" / "release-notes-pt-BR.txt"
RELEASE_NOTES_LANGUAGE = "pt-BR"


def build_service(key_path: Path):
    credentials = service_account.Credentials.from_service_account_file(
        str(key_path),
        scopes=SCOPES,
    )
    return build("androidpublisher", "v3", credentials=credentials, cache_discovery=False)


def read_manifest(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def inspect_tracks(service, package_name: str) -> int:
    edit = service.edits().insert(body={}, packageName=package_name).execute()
    edit_id = edit["id"]
    try:
        result = service.edits().tracks().list(packageName=package_name, editId=edit_id).execute()
        for track in result.get("tracks", []):
            print(f"track: {track['track']}")
            for release in track.get("releases", []):
                codes = ",".join(release.get("versionCodes", []))
                print(f"  status={release.get('status')} versionCodes={codes}")
    finally:
        service.edits().delete(packageName=package_name, editId=edit_id).execute()
    return 0


def upload(
    service,
    package_name: str,
    track: str,
    aab_path: Path,
    version_code: int,
    notes: str,
    status: str,
) -> int:
    edit = service.edits().insert(body={}, packageName=package_name).execute()
    edit_id = edit["id"]
    try:
        media = MediaFileUpload(str(aab_path), mimetype="application/octet-stream", resumable=True)
        bundle = (
            service.edits()
            .bundles()
            .upload(packageName=package_name, editId=edit_id, media_body=media)
            .execute()
        )
        print(f"uploaded versionCode {bundle['versionCode']}")

        release = {
            "versionCodes": [str(version_code)],
            "status": status,
            "releaseNotes": [{"language": RELEASE_NOTES_LANGUAGE, "text": notes}],
        }
        result = (
            service.edits()
            .tracks()
            .update(
                packageName=package_name,
                editId=edit_id,
                track=track,
                body={"releases": [release]},
            )
            .execute()
        )
        for entry in result.get("releases", []):
            codes = ",".join(entry.get("versionCodes", []))
            print(f"track {result['track']}: status={entry.get('status')} versionCodes={codes}")

        commit = service.edits().commit(packageName=package_name, editId=edit_id).execute()
        print(f"committed edit {commit['id']}")
        return 0
    except HttpError as error:
        print(error, file=sys.stderr)
        service.edits().delete(packageName=package_name, editId=edit_id).execute()
        return 1


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--key", type=Path, default=DEFAULT_KEY, help="service account JSON")
    parser.add_argument("--package", default=PACKAGE_NAME)
    parser.add_argument("--track", default="production", help="internal, alpha, beta or production")
    parser.add_argument(
        "--status",
        default="completed",
        choices=["completed", "draft", "halted", "inProgress"],
        help="release status after upload",
    )
    parser.add_argument("--inspect", action="store_true", help="list tracks and releases, then exit")
    parser.add_argument("--aab", type=Path, help="override the AAB path from last-release.json")
    args = parser.parse_args()

    if not args.key.is_file():
        print(f"service account not found: {args.key}", file=sys.stderr)
        return 1

    service = build_service(args.key)

    if args.inspect:
        return inspect_tracks(service, args.package)

    manifest = read_manifest(MANIFEST_PATH)
    aab_path = args.aab or Path(manifest["aabPath"])
    version_code = int(manifest["versionCode"])
    if not aab_path.is_file():
        print(f"AAB not found: {aab_path}", file=sys.stderr)
        return 1

    notes = RELEASE_NOTES_PATH.read_text(encoding="utf-8").strip()
    print(f"uploading {aab_path.name} as versionCode {version_code} to {args.track} ({args.status})")
    return upload(service, args.package, args.track, aab_path, version_code, notes, args.status)


if __name__ == "__main__":
    raise SystemExit(main())
