import json
import tempfile
import unittest
from pathlib import Path

from scripts.check_release_history_encoding import validate_history_pair


class ReleaseHistoryEncodingTest(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp_dir.cleanup)
        self.docs_path = Path(self.temp_dir.name) / "release-history.json"
        self.asset_path = Path(self.temp_dir.name) / "release-history-asset.json"

    def write_pair(self, docs_history, asset_history=None):
        asset_history = docs_history if asset_history is None else asset_history
        self.docs_path.write_text(
            json.dumps(docs_history, ensure_ascii=False),
            encoding="utf-8",
        )
        self.asset_path.write_text(
            json.dumps(asset_history, ensure_ascii=False),
            encoding="utf-8",
        )

    @staticmethod
    def history(title):
        return {
            "currentVersion": "1.0.2",
            "versionCode": 16,
            "entries": [{"title": title, "summary": "Resumo de teste"}],
        }

    def test_accepts_matching_utf8_history_with_accents(self):
        self.write_pair(self.history("Sessões em gramas"))

        self.assertEqual([], validate_history_pair(self.docs_path, self.asset_path))

    def test_rejects_mojibake_in_release_text(self):
        self.write_pair(self.history("SessÃµes em gramas"))

        errors = validate_history_pair(self.docs_path, self.asset_path)

        self.assertTrue(any("mojibake" in error.lower() for error in errors))

    def test_rejects_mismatched_docs_and_asset(self):
        self.write_pair(
            self.history("Sessões em gramas"),
            self.history("Outro título"),
        )

        errors = validate_history_pair(self.docs_path, self.asset_path)

        self.assertTrue(any("differ" in error.lower() for error in errors))

    def test_rejects_invalid_utf8(self):
        self.docs_path.write_bytes(
            b'{"currentVersion":"1.0.2","versionCode":16,"entries":[{"title":"\xff"}]}'
        )
        self.asset_path.write_text(
            json.dumps(self.history("Sessões em gramas"), ensure_ascii=False),
            encoding="utf-8",
        )

        errors = validate_history_pair(self.docs_path, self.asset_path)

        self.assertTrue(any("utf-8" in error.lower() for error in errors))


if __name__ == "__main__":
    unittest.main()
