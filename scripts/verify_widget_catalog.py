#!/usr/bin/env python3
"""Static consistency checks for Kairo's Android home-screen widget catalog."""
from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ANDROID = "{http://schemas.android.com/apk/res/android}"
WIDGETS_DIR = ROOT / "app/src/main/java/com/altayhr/kairo/widgets"
RES_XML = ROOT / "app/src/main/res/xml"
MANIFEST_PATH = ROOT / "app/src/main/AndroidManifest.xml"
STRINGS_PATH = ROOT / "app/src/main/res/values/strings.xml"


def fail(message: str) -> None:
    print(f"FAIL: {message}", file=sys.stderr)
    raise SystemExit(1)


catalog_path = WIDGETS_DIR / "KairoAgentWidgetProvider.kt"
catalog_text = catalog_path.read_text(encoding="utf-8")
keys = re.findall(r'KairoWidgetSpec\("([a-z0-9_]+)"', catalog_text)
if len(keys) != 22:
    fail(f"Expected 22 distinct AI widget keys, found {len(keys)}")
if len(set(keys)) != len(keys):
    fail("Widget catalog contains duplicate keys")

manifest_tree = ET.parse(MANIFEST_PATH)
manifest_root = manifest_tree.getroot()
app = manifest_root.find("application")
if app is None:
    fail("AndroidManifest.xml has no application element")

providers = {}
metadata_keys = set()
for receiver in app.findall("receiver"):
    name = receiver.get(ANDROID + "name", "")
    if ".widgets." not in name or name.endswith("KairoWidgetProvider"):
        continue
    provider = name.rsplit(".", 1)[-1]
    meta = receiver.find("meta-data")
    if meta is None:
        fail(f"{provider} has no appwidget.provider metadata")
    resource = meta.get(ANDROID + "resource", "")
    match = re.fullmatch(r"@xml/kairo_widget_([a-z0-9_]+)", resource)
    if not match:
        fail(f"{provider} references unexpected widget metadata: {resource}")
    key = match.group(1)
    providers[key] = provider
    metadata_keys.add(key)

if set(providers) != set(keys):
    missing = sorted(set(keys) - set(providers))
    extra = sorted(set(providers) - set(keys))
    fail(f"Manifest/catalog mismatch; missing={missing}, extra={extra}")

for key, provider in providers.items():
    source = WIDGETS_DIR / f"{provider}.kt"
    if not source.exists():
        fail(f"Missing provider source file for {key}: {source.name}")
    source_text = source.read_text(encoding="utf-8")
    if f'widgetKey: String = "{key}"' not in source_text:
        fail(f"{source.name} does not bind to widget key {key}")
    xml_path = RES_XML / f"kairo_widget_{key}.xml"
    if not xml_path.exists():
        fail(f"Missing widget metadata XML: {xml_path.name}")
    widget_root = ET.parse(xml_path).getroot()
    if widget_root.get(ANDROID + "initialLayout") != "@layout/widget_agent_tile":
        fail(f"{xml_path.name} does not use the shared premium widget layout")
    if widget_root.get(ANDROID + "description") != f"@string/kairo_widget_{key}_description":
        fail(f"{xml_path.name} has no matching description string")

strings_root = ET.parse(STRINGS_PATH).getroot()
string_names = {item.get("name") for item in strings_root.findall("string")}
for key in keys:
    for suffix in ("label", "description"):
        name = f"kairo_widget_{key}_{suffix}"
        if name not in string_names:
            fail(f"Missing resource string {name}")

layout_path = ROOT / "app/src/main/res/layout/widget_agent_tile.xml"
layout_root = ET.parse(layout_path).getroot()
layout_ids = {node.get(ANDROID + "id", "").split("/", 1)[-1] for node in layout_root.iter()}
for view_id in ("agent_widget_root", "agent_widget_open", "agent_widget_ai", "agent_widget_title", "agent_widget_status", "agent_widget_detail"):
    if view_id not in layout_ids:
        fail(f"Shared widget layout missing view id {view_id}")

main_activity = (ROOT / "app/src/main/java/com/altayhr/kairo/MainActivity.kt").read_text(encoding="utf-8")
widget_screen = (ROOT / "app/src/main/java/com/altayhr/kairo/features/AgentWidgetsScreen.kt").read_text(encoding="utf-8")
if 'putExtra("kairo_widget_id", it)' not in main_activity or 'launchWidgetId = pendingWidgetId' not in main_activity:
    fail("Widget key is not propagated through the main activity navigation")
if "KAIRO WIDGET: ${widget.title}" not in widget_screen or "onWidgetLaunchHandled(requestedId)" not in widget_screen:
    fail("AI widget action is not wired to the app's widget-specific prompt flow")

app_gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
if 'applicationId = "com.altayhr.kairo"' not in app_gradle:
    fail("Application ID changed unexpectedly")

print(f"PASS: {len(keys)} unique AI widget providers, metadata XML and labels verified")
print(f"PASS: shared premium layout IDs and widget-to-Gemini action wiring verified")
print("NOTE: static source check only; this does not replace Android Gradle build or device testing")
