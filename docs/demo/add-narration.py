"""Add the approved Indian-English male narration to the MuniAI demo video."""

from __future__ import annotations

import asyncio
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DEMO = ROOT / "docs" / "demo"
TOOLS = ROOT / "local-data" / "demo-tools"
sys.path.insert(0, str(TOOLS))

import edge_tts  # noqa: E402
import imageio_ffmpeg  # noqa: E402


VOICE = "en-IN-PrabhatNeural"


async def synthesize() -> Path:
    narration = (DEMO / "narration.txt").read_text(encoding="utf-8")
    output = DEMO / "narration.mp3"
    await edge_tts.Communicate(narration, VOICE, rate="+30%").save(output)
    return output


async def main() -> None:
    audio = await synthesize()
    video = DEMO / "muniai-demo.mp4"
    narrated = DEMO / "muniai-demo-narrated.mp4"
    subprocess.run(
        [
            imageio_ffmpeg.get_ffmpeg_exe(),
            "-y",
            "-i",
            str(video),
            "-i",
            str(audio),
            "-filter_complex",
            "[1:a]apad[a]",
            "-map",
            "0:v:0",
            "-map",
            "[a]",
            "-c:v",
            "copy",
            "-c:a",
            "aac",
            "-b:a",
            "160k",
            "-shortest",
            "-movflags",
            "+faststart",
            str(narrated),
        ],
        check=True,
    )
    narrated.replace(video)
    print(f"Added {VOICE} narration to {video}")


if __name__ == "__main__":
    asyncio.run(main())
