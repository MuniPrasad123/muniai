"""Build the captioned MuniAI demo video from verified browser captures."""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[2]
DEMO = ROOT / "docs" / "demo"
FRAMES = DEMO / "frames"
PUBLISH_FRAMES = DEMO / "publish-frames"
RENDERED = DEMO / "rendered-frames"
TOOLS = ROOT / "local-data" / "demo-tools"
sys.path.insert(0, str(TOOLS))

import imageio_ffmpeg  # noqa: E402


SCENES = [
    ("00-memory-proposal.png", 5.5, "1. Explicit request creates an approval-gated memory proposal"),
    ("02-document-indexed.png", 6.0, "2. Synthetic Markdown extracted and indexed locally into two chunks"),
    ("02b-vector-search.png", 6.5, "3. Ollama embeddings and Qdrant semantic search return inspectable matches"),
    ("03-grounded-answer.png", 7.0, "4. Ask Documents returns a grounded local answer with two citations"),
    ("04-retrieved-evidence.png", 7.0, "5. Retrieved passages and similarity scores remain inspectable"),
    ("05-memory-used.png", 6.5, "6. The response discloses the approved memory that influenced it"),
    ("06-memory-control.png", 7.5, "7. Memory stays reviewable, editable, disableable, and deletable"),
]


def stamp(seconds: float) -> str:
    milliseconds = round(seconds * 1000)
    hours, remainder = divmod(milliseconds, 3_600_000)
    minutes, remainder = divmod(remainder, 60_000)
    secs, millis = divmod(remainder, 1000)
    return f"{hours:02}:{minutes:02}:{secs:02}.{millis:03}"


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    name = "segoeuib.ttf" if bold else "segoeui.ttf"
    path = Path("C:/Windows/Fonts") / name
    return ImageFont.truetype(str(path), size) if path.exists() else ImageFont.load_default()


def render(source: Path, destination: Path, caption: str) -> None:
    canvas = Image.new("RGB", (1280, 720), "#f3f0e7")
    image = Image.open(source).convert("RGB")
    image.thumbnail((1240, 630), Image.Resampling.LANCZOS)
    x = (1280 - image.width) // 2
    y = 18 + (630 - image.height) // 2
    canvas.paste(image, (x, y))
    draw = ImageDraw.Draw(canvas)
    draw.rectangle((0, 654, 1280, 720), fill="#173f32")
    draw.text((28, 670), caption, fill="white", font=font(23, bold=True))
    destination.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(destination, quality=94)


def sanitize(source: Path, destination: Path) -> None:
    """Remove unrelated local conversation and document names before publishing."""
    image = Image.open(source).convert("RGB")
    draw = ImageDraw.Draw(image)
    sidebar_width = round(image.width * 0.225)
    draw.rectangle((0, round(image.height * 0.34), sidebar_width, round(image.height * 0.86)), fill="#ebe8de")
    if source.name == "02-document-indexed.png":
        draw.rectangle((sidebar_width, round(image.height * 0.79), image.width, image.height), fill="#f7f4ec")
    if source.name == "02b-vector-search.png":
        draw.rectangle((sidebar_width, round(image.height * 0.23), image.width, round(image.height * 0.61)), fill="#f7f4ec")
    destination.parent.mkdir(parents=True, exist_ok=True)
    image.save(destination)


def main() -> None:
    RENDERED.mkdir(parents=True, exist_ok=True)
    PUBLISH_FRAMES.mkdir(parents=True, exist_ok=True)
    concat = ["ffconcat version 1.0"]
    vtt = ["WEBVTT", ""]
    elapsed = 0.0
    last_frame = None

    for index, (filename, duration, caption) in enumerate(SCENES, start=1):
        publish_source = PUBLISH_FRAMES / filename
        sanitize(FRAMES / filename, publish_source)
        target = RENDERED / f"scene-{index:02}.jpg"
        render(publish_source, target, caption)
        concat.extend([f"file '{target.as_posix()}'", f"duration {duration:.3f}"])
        vtt.extend([f"{stamp(elapsed)} --> {stamp(elapsed + duration)}", caption, ""])
        elapsed += duration
        last_frame = target

    if last_frame is not None:
        concat.append(f"file '{last_frame.as_posix()}'")

    concat_path = DEMO / "muniai-demo.ffconcat"
    concat_path.write_text("\n".join(concat) + "\n", encoding="utf-8")
    (DEMO / "muniai-demo.vtt").write_text("\n".join(vtt), encoding="utf-8")

    ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()
    output = DEMO / "muniai-demo.mp4"
    subprocess.run(
        [
            ffmpeg,
            "-y",
            "-safe",
            "0",
            "-f",
            "concat",
            "-i",
            str(concat_path),
            "-vf",
            "fps=30,format=yuv420p",
            "-c:v",
            "libx264",
            "-movflags",
            "+faststart",
            str(output),
        ],
        check=True,
    )
    print(f"Created {output} ({elapsed:.1f} seconds) with accessible captions.")


if __name__ == "__main__":
    main()
