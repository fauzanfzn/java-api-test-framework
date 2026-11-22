"""Turns Surefire XML into a readiness summary (Markdown) and an optional CI gate.

Usage:
  python3 scripts/summary.py            # print Markdown (append to $GITHUB_STEP_SUMMARY)
  python3 scripts/summary.py --gate     # exit 1 if any test failed
"""
import glob
import sys
import xml.etree.ElementTree as ET

suites = []
for path in sorted(glob.glob("target/surefire-reports/TEST-*.xml")):
    root = ET.parse(path).getroot()
    name = root.get("name").rsplit(".", 1)[-1]
    total = int(root.get("tests"))
    failed = int(root.get("failures")) + int(root.get("errors"))
    skipped = int(root.get("skipped"))
    flaky = len(root.findall(".//flakyFailure")) + len(root.findall(".//flakyError"))
    suites.append((name, total, failed, skipped, flaky, float(root.get("time"))))

total = sum(s[1] for s in suites)
failed = sum(s[2] for s in suites)
skipped = sum(s[3] for s in suites)
flaky = sum(s[4] for s in suites)
passed = total - failed - skipped
readiness = round(100 * passed / max(total - skipped, 1), 1)

if "--gate" in sys.argv:
    sys.exit(1 if failed or total == 0 else 0)

status = "READY" if failed == 0 and total else "NOT READY"
print(f"## API readiness: {readiness}% ({status})\n")
print(f"**{passed}/{total - skipped}** tests passed · {failed} failed · {skipped} skipped · {flaky} passed only on retry (flaky)\n")
print("| Suite | Tests | Failed | Flaky | Time (s) |")
print("| --- | ---: | ---: | ---: | ---: |")
for name, t, f, _s, fl, sec in suites:
    print(f"| {'✅' if f == 0 else '❌'} {name} | {t} | {f} | {fl} | {sec:.1f} |")
