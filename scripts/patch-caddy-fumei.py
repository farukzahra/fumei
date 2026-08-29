from pathlib import Path

path = Path("/etc/caddy/Caddyfile")
content = path.read_text(encoding="utf-8")
old = """faruk.dev.br, www.faruk.dev.br {
\tencode gzip zstd
\treverse_proxy 127.0.0.1:3000
}"""
new = """faruk.dev.br, www.faruk.dev.br {
\thandle /fumei/* {
\t\turi strip_prefix /fumei
\t\troot * /opt/fumei-site
\t\tfile_server
\t}
\thandle {
\t\tencode gzip zstd
\t\treverse_proxy 127.0.0.1:3000
\t}
}"""
if old not in content:
    raise SystemExit("block not found")
path.write_text(content.replace(old, new, 1), encoding="utf-8")
print("ok")
