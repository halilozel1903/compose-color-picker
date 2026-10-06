#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running emulator.
# Taps can't be timed reliably through adb, so the sample opens each screen from the `scene` extra: the color
# picker dialog on a given tab (phone) or the two pane theme builder (tablet), so every capture is the same.
# Every capture is checked for the expected text and for a blank image.
#
#   bash scripts/screenshots.sh phone    # pixel_7: the dialog on the Wheel, Sliders and Swatches tabs, light and dark
#   bash scripts/screenshots.sh tablet   # pixel_tablet in landscape: picker and live preview side by side, light and dark
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

device="${1:-phone}"

# The text each scene must show; the capture fails without it.
# "Brightness" is only on the Wheel tab, "RGB" only on the Sliders tab, "Tints and shades" only on the Swatches
# tab and "Live preview" next to "Brand color" only in the two pane builder.
expected_text() {
  case "$1" in
    wheel) echo "Brightness" ;;
    sliders) echo "RGB" ;;
    swatches) echo "Tints and shades" ;;
    tablet) echo "Brand color" ;;
  esac
}

suffix() {
  if [ "$1" = dark ]; then echo "-dark"; else echo ""; fi
}

install_sample
if [ "$device" = tablet ]; then
  ensure_landscape
  for mode in light dark; do
    set_night_mode "$mode"
    fresh_launch --es scene tablet
    capture "tablet-builder$(suffix "$mode")" "$(expected_text tablet)"
  done
else
  for mode in light dark; do
    set_night_mode "$mode"
    for scene in wheel sliders swatches; do
      fresh_launch --es scene "$scene"
      capture "phone-$scene$(suffix "$mode")" "$(expected_text "$scene")"
    done
  done
fi
