#!/usr/bin/env python3
"""Publish Play Store listing assets via Android Publisher API."""

from __future__ import annotations

import json
import mimetypes
import sys
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

PACKAGE = "fumei.faruk.dev.br"
SCOPE = "https://www.googleapis.com/auth/androidpublisher"
LOCALE = "pt-BR"

REPO = Path(__file__).resolve().parents[1]
ASSETS = REPO / "docs" / "play-store" / "assets"
META = REPO / "docs" / "play-store" / "metadata" / LOCALE
SERVICE_ACCOUNT = Path(r"C:\repo\secrets\google-play\service-account.json")


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8").strip()


def upload_image(service, edit_id: str, image_type: str, path: Path) -> None:
    mime, _ = mimetypes.guess_type(path.name)
    media = MediaFileUpload(str(path), mimetype=mime or "image/png", resumable=False)
    service.edits().images().upload(
        packageName=PACKAGE,
        editId=edit_id,
        language=LOCALE,
        imageType=image_type,
        media_body=media,
    ).execute()
    print(f"uploaded {image_type}: {path.name}")


def delete_existing_images(service, edit_id: str, image_type: str) -> None:
    response = (
        service.edits()
        .images()
        .list(
            packageName=PACKAGE,
            editId=edit_id,
            language=LOCALE,
            imageType=image_type,
        )
        .execute()
    )
    for image in response.get("images", []):
        service.edits().images().delete(
            packageName=PACKAGE,
            editId=edit_id,
            language=LOCALE,
            imageType=image_type,
            imageId=image["id"],
        ).execute()
        print(f"deleted old {image_type}: {image['id']}")


def main() -> int:
    if not SERVICE_ACCOUNT.exists():
        print(f"Missing service account: {SERVICE_ACCOUNT}")
        print("Create a JSON key in Google Cloud, invite it in Play Console, save here.")
        return 1

    credentials = service_account.Credentials.from_service_account_file(
        str(SERVICE_ACCOUNT),
        scopes=[SCOPE],
    )
    service = build("androidpublisher", "v3", credentials=credentials, cache_discovery=False)

    edit = service.edits().insert(packageName=PACKAGE, body={}).execute()
    edit_id = edit["id"]
    print(f"editId={edit_id}")

    listing_body = {
        "title": read_text(META / "title.txt"),
        "shortDescription": read_text(META / "short-description.txt"),
        "fullDescription": read_text(META / "full-description.txt"),
    }
    service.edits().listings().update(
        packageName=PACKAGE,
        editId=edit_id,
        language=LOCALE,
        body=listing_body,
    ).execute()
    print("listing text updated")

    image_jobs = [
        ("icon", ASSETS / "icon-512.png"),
        ("featureGraphic", ASSETS / "feature-graphic.png"),
        ("phoneScreenshots", ASSETS / "screenshot-01-home.png"),
        ("phoneScreenshots", ASSETS / "screenshot-02-stats.png"),
        ("phoneScreenshots", ASSETS / "screenshot-03-about.png"),
    ]

    for image_type in {"icon", "featureGraphic", "phoneScreenshots"}:
        delete_existing_images(service, edit_id, image_type)

    for image_type, path in image_jobs:
        if not path.exists():
            print(f"missing asset: {path}")
            return 1
        upload_image(service, edit_id, image_type, path)

    aab_path = REPO / "app" / "build" / "outputs" / "bundle" / "release" / "app-release.aab"
    release_notes_path = REPO / "docs" / "play-store" / "release-notes-pt-BR.txt"
    if aab_path.exists():
        media = MediaFileUpload(str(aab_path), mimetype="application/octet-stream", resumable=True)
        bundle = (
            service.edits()
            .bundles()
            .upload(
                packageName=PACKAGE,
                editId=edit_id,
                media_body=media,
            )
            .execute()
        )
        version_code = bundle["versionCode"]
        release_notes = read_text(release_notes_path)
        track_body = {
            "releases": [
                {
                    "status": "completed",
                    "versionCodes": [version_code],
                    "releaseNotes": [
                        {"language": LOCALE, "text": release_notes},
                    ],
                }
            ]
        }
        service.edits().tracks().update(
            packageName=PACKAGE,
            editId=edit_id,
            track="production",
            body=track_body,
        ).execute()
        print(f"production track updated with versionCode {version_code}")
    else:
        print(f"skip AAB upload, missing: {aab_path}")

    commit = service.edits().commit(packageName=PACKAGE, editId=edit_id).execute()
    print(json.dumps(commit, indent=2))
    print("Play Store listing published.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
