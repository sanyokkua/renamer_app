import os
import subprocess
import shutil
import datetime

# Configuration
OUTPUT_DIR = "tools/test-data"
FFMPEG = "ffmpeg"
EXIFTOOL = "exiftool"
SIPS = "sips" # macOS specific

# Metadata Constants
DATE_CURRENT_OBJ = datetime.datetime.now()
DATE_CURRENT = DATE_CURRENT_OBJ.strftime("%Y:%m:%d %H:%M:%S")
DATE_PAST = "2000:01:01 12:00:00"
DATE_FUTURE = "2050:01:01 12:00:00"
GPS_PARIS = {"lat": 48.8566, "lon": 2.3522}

# Matrix Definitions
FILE_TYPES = {
    "image": {
        "jpg": {"ext": "jpg", "mime": "image/jpeg"},
        "png": {"ext": "png", "mime": "image/png"},
        "heic": {"ext": "heic", "mime": "image/heic", "mac_only": True},
        "webp": {"ext": "webp", "mime": "image/webp"},
    },
    "video": {
        "mp4": {"ext": "mp4", "mime": "video/mp4"},
        "mov": {"ext": "mov", "mime": "video/quicktime"},
        "mkv": {"ext": "mkv", "mime": "video/x-matroska"},
        "avi": {"ext": "avi", "mime": "video/x-msvideo"},
    },
    "audio": {
        "mp3": {"ext": "mp3", "mime": "audio/mpeg"},
        "wav": {"ext": "wav", "mime": "audio/wav"},
        "flac": {"ext": "flac", "mime": "audio/flac"},
        "ogg": {"ext": "ogg", "mime": "audio/ogg"},
    }
}

# Define Scenario Data
def get_scenario_data(scenario):
    suffix = ""
    tags = []

    if scenario == "clean":
        suffix = "_clean"
        tags = ["-all="]

    elif scenario == "std":
        d = DATE_CURRENT
        suffix = f"_std_{d.replace(':','-').replace(' ','_')}"
        tags = [f"-AllDates='{d}'"]

    elif scenario == "past":
        d = DATE_PAST
        suffix = f"_past_{d.replace(':','-').replace(' ','_')}"
        tags = [f"-AllDates='{d}'"]

    elif scenario == "future":
        d = DATE_FUTURE
        suffix = f"_future_{d.replace(':','-').replace(' ','_')}"
        tags = [f"-AllDates='{d}'"]

    elif scenario == "std_tz":
        d = DATE_CURRENT + "+02:00"
        suffix = f"_std_tz_{d.replace(':','-').replace(' ','_').replace('+','p')}"
        tags = [f"-AllDates='{d}'"]

    elif scenario == "std_no_tz":
         d = DATE_CURRENT
         suffix = f"_std_no_tz_{d.replace(':','-').replace(' ','_')}"
         tags = [f"-AllDates='{d}'"]

    elif scenario == "gps":
        d = DATE_CURRENT
        lat = GPS_PARIS['lat']
        lon = GPS_PARIS['lon']
        # Format: gps_DATE_latX_lonY
        suffix = f"_gps_{d.replace(':','-').replace(' ','_')}_lat{lat}_lon{lon}"
        tags = [
            f"-AllDates='{d}'",
            f"-GPSLatitude={lat}",
            f"-GPSLongitude={lon}",
            "-GPSLatitudeRef=N",
            "-GPSLongitudeRef=E"
        ]

    return suffix, tags

SCENARIOS_ALL = ["clean", "std", "past", "future", "std_tz", "std_no_tz", "gps"]

def run(cmd):
    # print(f"Running: {cmd}")
    subprocess.run(cmd, shell=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

def check_tools():
    for tool in [FFMPEG, EXIFTOOL]:
        if shutil.which(tool) is None:
            print(f"Error: {tool} not found.")
            return False
    if shutil.which(SIPS) is None:
        print("Warning: sips not found (HEIC generation skipped).")
    return True

def generate_base_file(category, fmt, path):
    if category == "image":
        if fmt == "heic":
            # Intermediate jpg then sips
            temp_jpg = path + ".temp.jpg"
            run(f"{FFMPEG} -f lavfi -i color=c=red:s=320x240:d=1 -frames:v 1 '{temp_jpg}'")
            run(f"{SIPS} -s format heic '{temp_jpg}' --out '{path}'")
            if os.path.exists(temp_jpg): os.remove(temp_jpg)
        else:
            run(f"{FFMPEG} -f lavfi -i color=c=blue:s=320x240:d=1 -frames:v 1 '{path}'")

    elif category == "video":
        run(f"{FFMPEG} -f lavfi -i testsrc=duration=1:size=320x240:rate=10 -c:v libx264 -pix_fmt yuv420p '{path}'")

    elif category == "audio":
        run(f"{FFMPEG} -f lavfi -i sine=frequency=1000:duration=1 '{path}'")

def apply_metadata(path, tags):
    if not tags: return

    cmd_base = f"{EXIFTOOL} -overwrite_original "
    cmd = f"{cmd_base} {' '.join(tags)} '{path}'"
    run(cmd)

def main():
    if not check_tools(): return

    # Clean up old test data if exists
    if os.path.exists(OUTPUT_DIR):
        shutil.rmtree(OUTPUT_DIR)
    os.makedirs(OUTPUT_DIR)

    print(f"Generating test data in {OUTPUT_DIR}...")

    count = 0
    for category, formats in FILE_TYPES.items():
        cat_dir = os.path.join(OUTPUT_DIR, category)
        os.makedirs(cat_dir, exist_ok=True)

        for fmt_name, fmt_props in formats.items():
            if fmt_name == "heic" and shutil.which(SIPS) is None:
                continue

            fmt_dir = os.path.join(cat_dir, fmt_name)
            os.makedirs(fmt_dir, exist_ok=True)

            for scenario in SCENARIOS_ALL:
                if category == "audio" and scenario == "gps": continue

                suffix, tags = get_scenario_data(scenario)

                filename = f"test_{fmt_name}{suffix}.{fmt_props['ext']}"
                file_path = os.path.join(fmt_dir, filename)

                generate_base_file(category, fmt_name, file_path)

                if os.path.exists(file_path):
                    apply_metadata(file_path, tags)
                    count += 1
                else:
                    print(f"Failed to create base file for {file_path}")

    print(f"Done! Generated {count} files.")

if __name__ == "__main__":
    main()
