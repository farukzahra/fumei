#!/usr/bin/env python3
"""Validate UTF-8 and synchronization of the About release history."""

from __future__ import annotations

import json
import sys
from pathlib import Path

MOJIBAKE_MARKERS = ("Ã", "Â", "\ufffd")
REPO_ROOT = Path(__file__).resolve().parents[1]
DOCS_HISTORY = REPO_ROOT / "docs" / "release-history.json"
ASSET_HISTORY = (
    REPO_ROOT
    / "app"
    / "src"
    / "main"
    / "assets"
    / "release-history.json"
)


def _read_json(path: Path) -> tuple[object | None, str | None]:
    try:
        text = path.read_bytes().decode("utf-8")
    except UnicodeDecodeError as error:
        return None, f"{path}: invalid UTF-8 at byte {error.start}"
    except OSError as error:
        return None, f"{path}: {error}"

    try:
        return json.loads(text), None
    except json.JSONDecodeError as error:
        return None, f"{path}: invalid JSON at line {error.lineno}"


def _contains_mojibake(value: object) -> bool:
    if isinstance(value, str):
        return any(marker in value for marker in MOJIBAKE_MARKERS)
    if isinstance(value, list):
        return any(_contains_mojibake(item) for item in value)
    if isinstance(value, dict):
        return any(
            _contains_mojibake(key) or _contains_mojibake(item)
            for key, item in value.items()
        )
    return False


def _validate_history_shape(path: Path, data: object) -> list[str]:
    if not isinstance(data, dict):
        return [f"{path}: release history must be a JSON object"]

    entries = data.get("entries")
    if not isinstance(entries, list) or not entries:
        return [f"{path}: release history must contain at least one entry"]

    errors = []
    for index, entry in enumerate(entries):
        if not isinstance(entry, dict):
            errors.append(f"{path}: entry {index} must be a JSON object")
            continue
        for field in ("title", "summary"):
            if not isinstance(entry.get(field), str):
                errors.append(f"{path}: entry {index} is missing text field {field}")
    return errors


def validate_history_pair(docs_path: Path, asset_path: Path) -> list[str]:
    docs, docs_error = _read_json(docs_path)
    asset, asset_error = _read_json(asset_path)
    errors = [error for error in (docs_error, asset_error) if error]

    for path, data in ((docs_path, docs), (asset_path, asset)):
        if data is not None:
            errors.extend(_validate_history_shape(path, data))
            if _contains_mojibake(data):
                errors.append(f"{path}: release text contains potential mojibake")

    if docs_error is None and asset_error is None and docs != asset:
        errors.append(f"{docs_path} and {asset_path} differ")

    return errors


def main() -> int:
    errors = validate_history_pair(DOCS_HISTORY, ASSET_HISTORY)
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1

    print("Release history UTF-8 and asset sync OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
