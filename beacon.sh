#!/bin/sh

sudo hciconfig hci0 up
sudo hciconfig hci0 leadv 3
sudo hciconfig hci0 noscanc

# uuidInput="906bbd3d-f326-4669-b146-6c225a04d936"


uuid="90 6b bd 3d f3 26 46 69 b1 46 6c 22 5a 04 d9 "
# uuid=echo $uuidInput | sed 's/\-//g'

if [ "$1" != "" ]; then
        uuid="$uuid $1"
else
        uuid="$uuid 35"
fi
major="00 01"
minor="00 01"

header="1E 02 01 1A 1A FF 4C 00 02 15"
txPower="C2 B4"

sudo hcitool -i hci0 cmd 0x08 0x0008 $header $uuid $major $minor $txPower
