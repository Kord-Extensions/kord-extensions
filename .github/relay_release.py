import json
import os
from http.client import HTTPResponse
from typing import Dict, Any
from urllib import request

tag = os.environ["GITHUB_REF"]
webhook_url = os.environ["WEBHOOK_URL"]

url = f"https://api.github.com/repos/Kotlin-Discord/kord-extensions/releases/tags/{tag}"

print(f"Tag: {tag}")
print(f"URL: {url}")

r = request.urlopen(url)

release: Dict[str, Any] = json.load(r)

release_url = release["html_url"]
release_name = release["name"]
release_body = release["body"].replace("\n* ", "\n**»** ")
release_time = release["published_at"]

author_name = release["author"]["login"]
author_avatar = release["author"]["avatar_url"]
author_url = release["author"]["html_url"]

while release_body[-1] in ["\n", " "]:
    release_body = release_body.strip("\n").strip()

if len(release_body) > 2000:
    release_body = release_body[:1997] + "..."

webhook = {
    "embeds": [
        {
            "color": 7506394,
            "description": release_body.strip(),
            "timestamp": release_time.replace("Z", ".000Z"),
            "title": release_name,
            "url": release_url,

            "author": {
                "name": author_name,
                "url": author_url,
                "icon_url": author_avatar
            }
        }
    ]
}

json_webhook = json.dumps(webhook).encode("utf-8")

discord_r = request.Request(webhook_url)
discord_r.add_header("Content-Type", "application/json; charset=utf-8")
discord_r.add_header("Content-Length", str(len(json_webhook)))
discord_r.add_header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 "
                                   "(KHTML, like Gecko) Chrome/35.0.1916.47 Safari/537.36")

response = request.urlopen(discord_r, json_webhook)
